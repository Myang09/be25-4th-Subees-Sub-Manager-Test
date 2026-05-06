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
  - name: node
    image: node:20-alpine
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
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changes') {
            steps {
                container('git') {
                    script {
                        sh '''
                            git fetch origin main --unshallow || true
                            git fetch origin main || true
                        '''

                        def changedText = sh(
                            script: '''
                                if git rev-parse HEAD~1 >/dev/null 2>&1; then
                                  git diff --name-only HEAD~1 HEAD
                                else
                                  git diff --name-only origin/main HEAD || true
                                fi
                            ''',
                            returnStdout: true
                        ).trim()

                        echo "Changed files:\n${changedText}"

                        env.BUILD_BACK = changedText.contains('backend/') ? 'true' : 'false'
                        env.BUILD_FRONT = changedText.contains('frontend/') ? 'true' : 'false'

                        echo "BUILD_BACK=${env.BUILD_BACK}"
                        echo "BUILD_FRONT=${env.BUILD_FRONT}"
                    }
                }
            }
        }

        stage('Backend Build') {
            when {
                expression { env.BUILD_BACK == 'true' }
            }
            steps {
                container('maven') {
                    dir('backend/subscription') {
                        sh '''
                            echo "Building backend..."
                            mvn -B clean package -DskipTests
                        '''
                    }
                }
            }
        }

        stage('Docker Login') {
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
                }
            }
        }

        stage('Build & Push Backend Image') {
            when {
                expression { env.BUILD_BACK == 'true' }
            }
            steps {
                container('docker') {
                    dir('backend/subscription') {
                        sh '''
                            echo "Building backend Docker image..."
                            docker build --no-cache -t $BACK_IMAGE:$IMAGE_TAG .
                            docker push $BACK_IMAGE:$IMAGE_TAG
                        '''
                    }
                }
            }
        }

        stage('Build & Push Frontend Image') {
            when {
                expression { env.BUILD_FRONT == 'true' }
            }
            steps {
                container('docker') {
                    dir('frontend') {
                        sh '''
                            echo "Checking frontend env..."
                            ls -al
                            cat .env.production || true

                            echo "Building frontend Docker image..."
                            docker build --no-cache -t $FRONT_IMAGE:$IMAGE_TAG .
                            docker push $FRONT_IMAGE:$IMAGE_TAG
                        '''
                    }
                }
            }
        }

        stage('Update Kubernetes Manifests') {
            when {
                expression { env.BUILD_BACK == 'true' || env.BUILD_FRONT == 'true' }
            }
            steps {
                container('git') {
                    script {
                        if (env.BUILD_BACK == 'true') {
                            sh '''
                                echo "Updating backend image tag..."
                                sed -i "s|image: myang12/subees-backend:.*|image: myang12/subees-backend:$IMAGE_TAG|" k8s/backend/deployment-local.yaml
                            '''
                        }

                        if (env.BUILD_FRONT == 'true') {
                            sh '''
                                echo "Updating frontend image tag..."
                                sed -i "s|image: myang12/subees-frontend:.*|image: myang12/subees-frontend:$IMAGE_TAG|" k8s/frontend/deployment.yaml
                            '''
                        }
                    }
                }
            }
        }

        stage('Push Manifest Changes') {
            when {
                expression { env.BUILD_BACK == 'true' || env.BUILD_FRONT == 'true' }
            }
            steps {
                container('git') {
                    sh '''
                        git config user.name "jenkins-bot"
                        git config user.email "jenkins-bot@example.com"

                        git add k8s/backend/deployment-local.yaml k8s/frontend/deployment.yaml
                        git commit -m "Update image tag to $IMAGE_TAG" || true
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
                    apk add --no-cache curl || true
                    curl -H "Content-Type: application/json" \
                      -d "{\\"content\\":\\"✅ Subees CI/CD 성공 - Build #$BUILD_NUMBER | Backend: $BUILD_BACK | Frontend: $BUILD_FRONT\\"}" \
                      "$DISCORD_WEBHOOK_URL" || true
                '''
            }
        }

        failure {
            withCredentials([string(
                credentialsId: "${DISCORD_WEBHOOK_CREDENTIALS_ID}",
                variable: 'DISCORD_WEBHOOK_URL'
            )]) {
                sh '''
                    apk add --no-cache curl || true
                    curl -H "Content-Type: application/json" \
                      -d "{\\"content\\":\\"❌ Subees CI/CD 실패 - Build #$BUILD_NUMBER\\"}" \
                      "$DISCORD_WEBHOOK_URL" || true
                '''
            }
        }
    }
}