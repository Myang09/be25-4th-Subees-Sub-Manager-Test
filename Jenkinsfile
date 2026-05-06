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
    }

    stages {
        stage('1. Checkout & Detect Changes') {
            steps {
                checkout scm

                container('git') {
                    script {
                        sh 'git config --global --add safe.directory "$WORKSPACE"'

                        def changedText = sh(
                            script: '''
                                if git rev-parse HEAD~1 >/dev/null 2>&1; then
                                  git diff --name-only HEAD~1 HEAD
                                else
                                  git show --name-only --pretty="" HEAD
                                fi
                            ''',
                            returnStdout: true
                        ).trim()

                        echo "Changed files:\n${changedText}"
                        env.BUILD_BACK = changedText.readLines().any { it.startsWith('backend/') } ? 'true' : 'false'
                        env.BUILD_FRONT = changedText.readLines().any { it.startsWith('fronted/') } ? 'true' : 'false'

                        echo "BUILD_BACK=${env.BUILD_BACK}"
                        echo "BUILD_FRONT=${env.BUILD_FRONT}"
                        echo "IMAGE_TAG=${env.IMAGE_TAG}"
                    }
                }
            }
        }

        stage('2. Build Application') {
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
                                cat k8s/backend/deployment-local.yaml
                            '''
                        }

                        if (env.BUILD_FRONT == 'true') {
                            sh '''
                                echo "Updating frontend deployment image..."
                                sed -i "s|image: myang12/subees-frontend:.*|image: myang12/subees-frontend:$IMAGE_TAG|" k8s/frontend/deployment.yaml
                                cat k8s/frontend/deployment.yaml
                            '''
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
                    sh '''
                        git config --global --add safe.directory "$WORKSPACE"
                        git config user.name "jenkins-bot"
                        git config user.email "jenkins-bot@example.com"

                        echo "=== Git diff before commit ==="
                        git diff


                        git add k8s/backend/deployment-local.yaml k8s/frontend/deployment.yaml
                        git commit -m "Update image tag to $IMAGE_TAG" || true
                        git status
                        git push origin $GIT_BRANCH
                    '''
                }
            }
        }
    }

    post {
        success {
            withCredentials([string(
                credentialsId: "${DISCORD_WEBHOOK_CREDENTIALS_ID}",
                variable: 'DISCORD_WEBHOOK_URL'
            )]) {
                sh '''
                    curl -H "Content-Type: application/json" \
                      -d "{\\"content\\":\\"✅ Subees CI/CD 성공 - Build #$BUILD_NUMBER | Backend: $BUILD_BACK | Frontend: $BUILD_FRONT | Image Tag: $IMAGE_TAG\\"}" \
                      "$DISCORD_WEBHOOK_URL"
                '''
            }
        }

        failure {
            withCredentials([string(
                credentialsId: "${DISCORD_WEBHOOK_CREDENTIALS_ID}",
                variable: 'DISCORD_WEBHOOK_URL'
            )]) {
                sh '''
                    curl -H "Content-Type: application/json" \
                      -d "{\\"content\\":\\"❌ Subees CI/CD 실패 - Build #$BUILD_NUMBER\\"}" \
                      "$DISCORD_WEBHOOK_URL"
                '''
            }
        }
    }
}