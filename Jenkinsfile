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
    command:
    - cat
    tty: true
  - name: docker
    image: docker:28.5.1-cli-alpine3.22
    command:
    - cat
    tty: true
    volumeMounts:
    - mountPath: "/var/run/docker.sock"
      name: docker-socket
  volumes:
  - name: docker-socket
    hostPath:
      path: "/var/run/docker.sock"
'''
        }
    }

    environment {
        FRONT_IMAGE = 'myang12/subees-frontend'
        BACK_IMAGE = 'myang12/subees-backend'
        DOCKER_CREDENTIALS_ID = 'dockerhub-access'
        DISCORD_WEBHOOK_CREDENTIALS_ID = 'discord-webhook'
    }

    stages {
        stage('Detect Changes') {
            steps {
                script {
                    def changedText = sh(
                        script: 'git diff --name-only HEAD~1',
                        returnStdout: true
                    ).trim()

                    def changedFiles = changedText ? changedText.split("\\n") : []

                    echo "Changed files:\n${changedFiles.join('\n')}"

                    env.SHOULD_BUILD_FRONT = changedFiles.any {
                        it.startsWith("fronted/")
                    } ? "true" : "false"

                    env.SHOULD_BUILD_BACK = changedFiles.any {
                        it.startsWith("backend/")
                    } ? "true" : "false"

                    echo "SHOULD_BUILD_FRONT : ${env.SHOULD_BUILD_FRONT}"
                    echo "SHOULD_BUILD_BACK : ${env.SHOULD_BUILD_BACK}"
                }
            }
        }

        stage('Docker Login') {
            when {
                expression {
                    return env.SHOULD_BUILD_FRONT == "true" || env.SHOULD_BUILD_BACK == "true"
                }
            }

            steps {
                container('docker') {
                    withCredentials([usernamePassword(
                        credentialsId: DOCKER_CREDENTIALS_ID,
                        usernameVariable: 'DOCKER_USERNAME',
                        passwordVariable: 'DOCKER_PASSWORD'
                    )]) {
                        sh 'echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin'
                    }
                }
            }
        }

        stage('Backend Image Build & Push') {
            when {
                expression {
                    return env.SHOULD_BUILD_BACK == "true"
                }
            }

            steps {
                container('maven') {
                    dir('backend/subscription') {
                        sh 'mvn -B clean package -DskipTests'
                    }
                }

                container('docker') {
                    dir('backend/subscription') {
                        script {
                            def buildNumber = "${env.BUILD_NUMBER}"

                            withEnv(["DOCKER_IMAGE_VERSION=${buildNumber}"]) {
                                sh 'docker -v'
                                sh 'echo $BACK_IMAGE:$DOCKER_IMAGE_VERSION'
                                sh 'docker build -t $BACK_IMAGE:$DOCKER_IMAGE_VERSION ./'
                                sh 'docker image inspect $BACK_IMAGE:$DOCKER_IMAGE_VERSION'
                                sh 'docker push $BACK_IMAGE:$DOCKER_IMAGE_VERSION'
                            }
                        }
                    }
                }
            }
        }

        stage('Frontend Image Build & Push') {
            when {
                expression {
                    return env.SHOULD_BUILD_FRONT == "true"
                }
            }

            steps {
                container('docker') {
                    dir('fronted') {
                        script {
                            def buildNumber = "${env.BUILD_NUMBER}"

                            withEnv(["DOCKER_IMAGE_VERSION=${buildNumber}"]) {
                                sh 'docker -v'
                                sh 'echo $FRONT_IMAGE:$DOCKER_IMAGE_VERSION'
                                sh 'docker build -t $FRONT_IMAGE:$DOCKER_IMAGE_VERSION ./'
                                sh 'docker image inspect $FRONT_IMAGE:$DOCKER_IMAGE_VERSION'
                                sh 'docker push $FRONT_IMAGE:$DOCKER_IMAGE_VERSION'
                            }
                        }
                    }
                }
            }
        }

        stage('Trigger k8s-manifests Job') {
            when {
                expression {
                    return env.SHOULD_BUILD_FRONT == "true" || env.SHOULD_BUILD_BACK == "true"
                }
            }

            steps {
                script {
                    def buildNumber = "${env.BUILD_NUMBER}"

                    build job: 'subees-k8s-manifests',
                        parameters: [
                            string(name: 'DOCKER_IMAGE_VERSION', value: "${buildNumber}"),
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
                credentialsId: DISCORD_WEBHOOK_CREDENTIALS_ID,
                variable: 'DISCORD_WEBHOOK_URL'
            )]) {
                sh '''
                    curl --max-time 10 --connect-timeout 5 \
                      -H "Content-Type: application/json" \
                      -d "{\\"content\\":\\"✅ Subees App Build 성공 - Build #$BUILD_NUMBER | Backend: $SHOULD_BUILD_BACK | Frontend: $SHOULD_BUILD_FRONT | Image Tag: $BUILD_NUMBER\\"}" \
                      "$DISCORD_WEBHOOK_URL" || echo "Discord notification failed."
                '''
            }
        }

        failure {
            withCredentials([string(
                credentialsId: DISCORD_WEBHOOK_CREDENTIALS_ID,
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