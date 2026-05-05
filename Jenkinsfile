pipeline {
    agent any

    environment {
        FRONT_IMAGE = 'myang12/subees-frontend'
        BACK_IMAGE  = 'myang12/subees-backend'

        DOCKER_CREDENTIALS_ID = 'dockerhub-access'
        GIT_CREDENTIALS_ID = 'github-credentials'
        DISCORD_WEBHOOK_CREDENTIALS_ID = 'discord-webhook'

        IMAGE_TAG = "${BUILD_NUMBER}"

        GIT_BRANCH = 'main'
        GIT_REPO = 'github.com/myang09/be25-4th-Subees-Sub-Manager-Test.git'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Detect Changes') {
            steps {
                script {
                    def changedText = bat(
                        script: 'git diff --name-only HEAD~1 HEAD',
                        returnStdout: true
                    ).trim()

                    echo "Changed files:\n${changedText}"

                    env.BUILD_BACK = changedText.contains('backend/') ? 'true' : 'false'
                    env.BUILD_FRONT = changedText.contains('fronted/') ? 'true' : 'false'

                    echo "BUILD_BACK=${env.BUILD_BACK}"
                    echo "BUILD_FRONT=${env.BUILD_FRONT}"
                }
            }
        }

        stage('Backend Build') {
            when {
                expression { env.BUILD_BACK == 'true' }
            }
            steps {
                dir('backend/subscription') {
                    bat '''
                        echo Building backend...
                        mvnw.cmd clean package -DskipTests
                    '''
                }
            }
        }

        stage('Docker Login') {
            when {
                expression { env.BUILD_BACK == 'true' || env.BUILD_FRONT == 'true' }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${DOCKER_CREDENTIALS_ID}",
                    usernameVariable: 'DOCKER_USERNAME',
                    passwordVariable: 'DOCKER_PASSWORD'
                )]) {
                    bat '''
                        echo %DOCKER_PASSWORD% | docker login -u %DOCKER_USERNAME% --password-stdin
                    '''
                }
            }
        }

        stage('Build & Push Backend Image') {
            when {
                expression { env.BUILD_BACK == 'true' }
            }
            steps {
                dir('backend/subscription') {
                    bat '''
                        echo Building backend Docker image...
                        docker build -t %BACK_IMAGE%:%IMAGE_TAG% .
                        docker push %BACK_IMAGE%:%IMAGE_TAG%
                    '''
                }
            }
        }

        stage('Build & Push Frontend Image') {
            when {
                expression { env.BUILD_FRONT == 'true' }
            }
            steps {
                dir('fronted') {
                    bat '''
                        echo Building frontend Docker image...
                        docker build -t %FRONT_IMAGE%:%IMAGE_TAG% .
                        docker push %FRONT_IMAGE%:%IMAGE_TAG%
                    '''
                }
            }
        }

        stage('Update Kubernetes Manifests') {
            when {
                expression { env.BUILD_BACK == 'true' || env.BUILD_FRONT == 'true' }
            }
            steps {
                script {
                    if (env.BUILD_BACK == 'true') {
                        bat '''
                            powershell -Command "(Get-Content k8s/backend/deployment-local.yaml) -replace 'image: myang12/subees-backend:.*', 'image: myang12/subees-backend:%IMAGE_TAG%' | Set-Content k8s/backend/deployment-local.yaml"
                        '''
                    }

                    if (env.BUILD_FRONT == 'true') {
                        bat '''
                            powershell -Command "(Get-Content k8s/frontend/deployment.yaml) -replace 'image: myang12/subees-frontend:.*', 'image: myang12/subees-frontend:%IMAGE_TAG%' | Set-Content k8s/frontend/deployment.yaml"
                        '''
                    }
                }
            }
        }

        stage('Push Manifest Changes') {
            when {
                expression { env.BUILD_BACK == 'true' || env.BUILD_FRONT == 'true' }
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: "${GIT_CREDENTIALS_ID}",
                    usernameVariable: 'GIT_USERNAME',
                    passwordVariable: 'GIT_PASSWORD'
                )]) {
                    bat '''
                        git config user.name "jenkins-bot"
                        git config user.email "jenkins-bot@example.com"

                        git add k8s/backend/deployment-local.yaml k8s/frontend/deployment.yaml
                        git commit -m "Update image tag to %IMAGE_TAG%" || exit /b 0

                        git push https://%GIT_USERNAME%:%GIT_PASSWORD%@%GIT_REPO% %GIT_BRANCH%
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
                bat '''
                    powershell -Command "$body = @{content='Subees 배포 성공 - Build #%BUILD_NUMBER% | Backend: %BUILD_BACK% | Frontend: %BUILD_FRONT%'} | ConvertTo-Json; Invoke-RestMethod -Uri $env:DISCORD_WEBHOOK_URL -Method Post -Body $body -ContentType 'application/json'"
                '''
            }
        }

        failure {
            withCredentials([string(
                credentialsId: "${DISCORD_WEBHOOK_CREDENTIALS_ID}",
                variable: 'DISCORD_WEBHOOK_URL'
            )]) {
                bat '''
                    powershell -Command "$body = @{content='Subees 배포 실패 - Build #%BUILD_NUMBER%'} | ConvertTo-Json; Invoke-RestMethod -Uri $env:DISCORD_WEBHOOK_URL -Method Post -Body $body -ContentType 'application/json'"
                '''
            }
        }
    }
}