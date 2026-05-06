pipeline {
    agent {
        kubernetes {
            yaml '''
apiVersion: v1
kind: Pod
metadata:
  name: subees-app-agent
spec:
  containers:
  - name: maven
    image: maven:3.9.9-eclipse-temurin-21-alpine
    command: ["cat"]
    tty: true
    volumeMounts:
    - name: workspace-volume
      mountPath: /home/jenkins/agent

  - name: docker
    image: docker:28.5.1-cli-alpine3.22
    command: ["cat"]
    tty: true
    volumeMounts:
    - name: docker-socket
      mountPath: /var/run/docker.sock
    - name: workspace-volume
      mountPath: /home/jenkins/agent

  - name: git
    image: alpine/git
    command: ["cat"]
    tty: true
    volumeMounts:
    - name: workspace-volume
      mountPath: /home/jenkins/agent

  volumes:
  - name: workspace-volume
    emptyDir: {}

  - name: docker-socket
    hostPath:
      path: /var/run/docker.sock
'''
        }
    }

    parameters {
        booleanParam(
            name: 'FORCE_BUILD_BACK',
            defaultValue: false,
            description: '백엔드 강제 빌드'
        )

        booleanParam(
            name: 'FORCE_BUILD_FRONT',
            defaultValue: false,
            description: '프론트 강제 빌드'
        )
    }

    environment {
        FRONT_IMAGE = 'myang12/subees-frontend'
        BACK_IMAGE  = 'myang12/subees-backend'

        DOCKER_CREDENTIALS_ID = 'dockerhub-access'
        DISCORD_WEBHOOK_CREDENTIALS_ID = 'discord-webhook'

        IMAGE_TAG = "${BUILD_NUMBER}"

        SHOULD_BUILD_FRONT = 'false'
        SHOULD_BUILD_BACK = 'false'
    }

    stages {
        stage('1. Checkout') {
            steps {
                checkout scm
            }
        }

        stage('2. Detect Changes') {
            steps {
                container('git') {
                    script {
                        sh 'git config --global --add safe.directory "$WORKSPACE"'

                        sh '''
                            echo "=== Git log ==="
                            git log --oneline -5
                        '''

                        def changedText = sh(
                            script: '''
                                {
                                  if git rev-parse HEAD~1 >/dev/null 2>&1; then
                                    git diff --name-only HEAD~1 HEAD
                                  fi

                                  git show --name-only --pretty="" HEAD
                                } | sort -u
                            ''',
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

                        env.SHOULD_BUILD_FRONT = (hasFrontChange || params.FORCE_BUILD_FRONT) ? 'true' : 'false'
                        env.SHOULD_BUILD_BACK = (hasBackChange || params.FORCE_BUILD_BACK) ? 'true' : 'false'

                        echo "FORCE_BUILD_FRONT=${params.FORCE_BUILD_FRONT}"
                        echo "FORCE_BUILD_BACK=${params.FORCE_BUILD_BACK}"
                        echo "SHOULD_BUILD_FRONT=${env.SHOULD_BUILD_FRONT}"
                        echo "SHOULD_BUILD_BACK=${env.SHOULD_BUILD_BACK}"
                        echo "IMAGE_TAG=${env.IMAGE_TAG}"
                    }
                }
            }
        }

        stage('3. Docker Login') {
            when {
                expression {
                    return env.SHOULD_BUILD_FRONT == 'true' || env.SHOULD_BUILD_BACK == 'true'
                }
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
                }
            }
        }

        stage('4. Build Backend App') {
            when {
                expression {
                    return env.SHOULD_BUILD_BACK == 'true'
                }
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

        stage('5. Backend Image Build & Push') {
            when {
                expression {
                    return env.SHOULD_BUILD_BACK == 'true'
                }
            }

            steps {
                container('docker') {
                    dir('backend/subscription') {
                        sh '''
                            echo "Building backend Docker image..."
                            docker build --no-cache -t $BACK_IMAGE:$IMAGE_TAG .
                            docker image inspect $BACK_IMAGE:$IMAGE_TAG
                            docker push $BACK_IMAGE:$IMAGE_TAG
                        '''
                    }
                }
            }
        }

        stage('6. Frontend Image Build & Push') {
            when {
                expression {
                    return env.SHOULD_BUILD_FRONT == 'true'
                }
            }

            steps {
                container('docker') {
                    dir('fronted') {
                        sh '''
                            echo "Building frontend Docker image..."
                            docker build --no-cache -t $FRONT_IMAGE:$IMAGE_TAG .
                            docker image inspect $FRONT_IMAGE:$IMAGE_TAG
                            docker push $FRONT_IMAGE:$IMAGE_TAG
                        '''
                    }
                }
            }
        }

        stage('7. Trigger k8s Manifest Job') {
            when {
                expression {
                    return env.SHOULD_BUILD_FRONT == 'true' || env.SHOULD_BUILD_BACK == 'true'
                }
            }

            steps {
                script {
                    build job: 'subees-k8s-manifests',
                        parameters: [
                            string(name: 'DOCKER_IMAGE_VERSION', value: "${env.IMAGE_TAG}"),
                            string(name: 'DID_BUILD_FRONT', value: "${env.SHOULD_BUILD_FRONT}"),
                            string(name: 'DID_BUILD_BACK', value: "${env.SHOULD_BUILD_BACK}")
                        ],
                        wait: true
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
                    curl --max-time 10 --connect-timeout 5 \
                      -H "Content-Type: application/json" \
                      -d "{\\"content\\":\\"✅ Subees App Build 성공 - Build #$BUILD_NUMBER | Backend: $SHOULD_BUILD_BACK | Frontend: $SHOULD_BUILD_FRONT | Image Tag: $IMAGE_TAG\\"}" \
                      "$DISCORD_WEBHOOK_URL" || echo "Discord notification failed."
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
                      -d "{\\"content\\":\\"❌ Subees App Build 실패 - Build #$BUILD_NUMBER\\"}" \
                      "$DISCORD_WEBHOOK_URL" || echo "Discord notification failed."
                '''
            }
        }
    }
}