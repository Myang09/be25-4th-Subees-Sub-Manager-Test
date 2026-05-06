pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
metadata:
  name: subees-ci-agent
spec:
  containers:
  - name: maven
    image: maven:3.9.9-eclipse-temurin-21-alpine
    command: ["cat"]
    tty: true
  - name: docker
    image: docker:28.5.1-cli-alpine3.22
    command: ["cat"]
    tty: true
    volumeMounts:
    - name: docker-socket
      mountPath: /var/run/docker.sock
  - name: git
    image: alpine/git
    command: ["cat"]
    tty: true
  volumes:
  - name: docker-socket
    hostPath:
      path: /var/run/docker.sock
'''
        }
    }

    environment {
        FRONT_IMAGE = 'myang12/subees-frontend'
        BACK_IMAGE  = 'myang12/subees-backend'

        DOCKER_CREDENTIALS_ID = 'dockerhub-access'
        DISCORD_WEBHOOK_CREDENTIALS_ID = 'discord-webhook'

        IMAGE_TAG = "${BUILD_NUMBER}"
        GIT_BRANCH = 'main'

        BUILD_FRONT = 'false'
        BUILD_BACK = 'false'
        SKIP_NOTIFY = 'false'
    }

    stages {
        stage('1. Checkout & Detect Changes') {
            steps {
                checkout scm

                container('git') {
                    script {
                        sh 'git config --global --add safe.directory "$WORKSPACE"'

                        def currentCommit = sh(
                            script: 'git rev-parse HEAD',
                            returnStdout: true
                        ).trim()

                        def previousCommit = env.GIT_PREVIOUS_SUCCESSFUL_COMMIT ?: env.GIT_PREVIOUS_COMMIT

                        if (!previousCommit?.trim()) {
                            previousCommit = sh(
                                script: '''
                                    if git rev-parse HEAD~1 >/dev/null 2>&1; then
                                      git rev-parse HEAD~1
                                    else
                                      git rev-parse HEAD
                                    fi
                                ''',
                                returnStdout: true
                            ).trim()
                        }

                        echo "Previous commit: ${previousCommit}"
                        echo "Current commit: ${currentCommit}"

                        sh '''
                            echo "=== Git log ==="
                            git log --oneline -5
                        '''

                        def changedText = sh(
                            script: "git diff --name-only ${previousCommit} ${currentCommit}",
                            returnStdout: true
                        ).trim()

                        def changedFiles = changedText.readLines()
                            .collect { it.trim() }
                            .findAll { it }
                            .unique()

                        echo "Changed files:\n${changedFiles.join('\n')}"

                        def hasFrontChange = changedFiles.any {
                            it.startsWith('fronted/')
                        }

                        def hasBackChange = changedFiles.any {
                            it.startsWith('backend/')
                        }

                        def onlyK8sChanged = changedFiles && changedFiles.every {
                            it.startsWith('k8s/')
                        }

                        if (onlyK8sChanged) {
                            echo "Only k8s manifest changed. Skip application build."
                            env.BUILD_FRONT = 'false'
                            env.BUILD_BACK = 'false'
                            env.SKIP_NOTIFY = 'true'
                            return
                        }

                        env.BUILD_FRONT = hasFrontChange ? 'true' : 'false'
                        env.BUILD_BACK = hasBackChange ? 'true' : 'false'

                        echo "BUILD_FRONT=${env.BUILD_FRONT}"
                        echo "BUILD_BACK=${env.BUILD_BACK}"
                        echo "IMAGE_TAG=${env.IMAGE_TAG}"
                    }
                }
            }
        }

        stage('2. Build Backend Application') {
            when {
                expression { env.BUILD_BACK == 'true' }
            }

            steps {
                container('maven') {
                    dir('backend/subscription') {
                        sh '''
                            echo "Building backend application..."
                            mvn -B clean package -DskipTests
                        '''
                    }
                }
            }
        }

        stage('3. Docker Build & Push') {
            when {
                expression { env.BUILD_BACK == 'true' || env.BUILD_FRONT == 'true' }
            }

            steps {
                container('docker') {
                    withCredentials([usernamePassword(
                        credentialsId: "${DOCKER_CREDENTIALS_ID}",
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh '''
                            echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                        '''
                    }

                    script {
                        if (env.BUILD_BACK == 'true') {
                            dir('backend/subscription') {
                                sh '''
                                    echo "Building backend Docker image..."
                                    docker build -t $BACK_IMAGE:$IMAGE_TAG .
                                    docker push $BACK_IMAGE:$IMAGE_TAG
                                '''
                            }
                        }

                        if (env.BUILD_FRONT == 'true') {
                            dir('fronted') {
                                sh '''
                                    echo "Building frontend Docker image..."
                                    docker build -t $FRONT_IMAGE:$IMAGE_TAG .
                                    docker push $FRONT_IMAGE:$IMAGE_TAG
                                '''
                            }
                        }
                    }
                }
            }
        }

        stage('4. Update Kubernetes Manifests') {
            when {
                expression { env.BUILD_BACK == 'true' || env.BUILD_FRONT == 'true' }
            }

            steps {
                container('git') {
                    script {
                        sh 'git config --global --add safe.directory "$WORKSPACE"'

                        if (env.BUILD_BACK == 'true') {
                            sh '''
                                echo "Updating backend deployment image..."
                                sed -i "s|image: myang12/subees-backend:.*|image: myang12/subees-backend:$IMAGE_TAG|" k8s/backend/deployment-local.yaml

                                echo "=== Backend deployment after update ==="
                                grep -n "image:" k8s/backend/deployment-local.yaml
                            '''
                        }

                        if (env.BUILD_FRONT == 'true') {
                            sh '''
                                echo "Updating frontend deployment image..."
                                sed -i "s|image: myang12/subees-frontend:.*|image: myang12/subees-frontend:$IMAGE_TAG|" k8s/frontend/deployment.yaml

                                echo "=== Frontend deployment after update ==="
                                grep -n "image:" k8s/frontend/deployment.yaml
                            '''
                        }
                    }
                }
            }
        }

        stage('5. Commit & Push Manifests') {
            when {
                expression { env.BUILD_BACK == 'true' || env.BUILD_FRONT == 'true' }
            }

            steps {
                container('git') {
                    sshagent(credentials: ['github-subees']) {
                        sh '''
                            git config --global --add safe.directory "$WORKSPACE"
                            git config user.name "jenkins-bot"
                            git config user.email "jenkins-bot@example.com"

                            mkdir -p ~/.ssh
                            ssh-keyscan github.com >> ~/.ssh/known_hosts

                            echo "=== Git diff before commit ==="
                            git diff

                            git add k8s/backend/deployment-local.yaml k8s/frontend/deployment.yaml
                            git commit -m "Update image tag to $IMAGE_TAG" || echo "No changes to commit"

                            echo "=== Git status ==="
                            git status

                            git push origin HEAD:$GIT_BRANCH
                        '''
                    }
                }
            }
        }
    }

    post {
        success {
            script {
                if (env.SKIP_NOTIFY == 'true') {
                    echo "Skip Discord notification for manifest-only commit."
                    return
                }
            }

            withCredentials([string(
                credentialsId: "${DISCORD_WEBHOOK_CREDENTIALS_ID}",
                variable: 'DISCORD_WEBHOOK_URL'
            )]) {
                sh '''
                    curl --max-time 10 --connect-timeout 5 \
                      -H "Content-Type: application/json" \
                      -d "{\\"content\\":\\"✅ Subees CI/CD 성공 - Build #$BUILD_NUMBER | Backend: $BUILD_BACK | Frontend: $BUILD_FRONT | Image Tag: $IMAGE_TAG\\"}" \
                      "$DISCORD_WEBHOOK_URL" || echo "Discord notification failed, but pipeline continues."
                '''
            }
        }

        failure {
            withCredentials([string(
                credentialsId: "${DISCORD_WEBHOOK_CREDENTIALS_ID}",
                variable: 'DISCORD_WEBHOOK_URL'
            )]) {
                sh '''
                    curl --max-time 10 --connect-timeout 5 \
                      -H "Content-Type: application/json" \
                      -d "{\\"content\\":\\"❌ Subees CI/CD 실패 - Build #$BUILD_NUMBER\\"}" \
                      "$DISCORD_WEBHOOK_URL" || echo "Discord notification failed, but pipeline continues."
                '''
            }
        }
    }
}