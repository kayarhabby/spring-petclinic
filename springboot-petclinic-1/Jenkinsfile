pipeline {
    agent any

    environment {
        DOCKERHUB_USERNAME = "kayarhabby"
        DOCKER_IMAGE_APP = "petclinic-app"
        DOCKER_TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Maven') {
            steps {
                sh 'mvn clean package -Dspring.profiles.active=mysql -DskipTests'
            }
        }

        stage('Unit Tests') {
             steps {
                sh 'mvn test'
             }
        }

        stage('Build Docker Images (docker-compose)') {
            steps {
                sh 'docker compose build'
            }
        }

        stage('Login DockerHub (non-interactive)') {
            steps {
                withCredentials([usernamePassword(
                    credentialsId: 'dockerhubcreds',
                    usernameVariable: 'DOCKER_USER',
                    passwordVariable: 'DOCKER_PASS'
                )]) {
                    sh '''
                    echo "$DOCKER_PASS" | docker login -u "$DOCKER_USER" --password-stdin
                    '''
                }
            }
        }

        stage('Tag Images with Build Number') {
            steps {
                sh """
                docker tag $DOCKER_IMAGE_APP:latest $DOCKERHUB_USERNAME/$DOCKER_IMAGE_APP:latest
                docker tag $DOCKER_IMAGE_APP:latest $DOCKERHUB_USERNAME/$DOCKER_IMAGE_APP:$DOCKER_TAG
                """
            }
        }

        stage('Push to DockerHub') {
            steps {
                sh """
                docker push $DOCKERHUB_USERNAME/$DOCKER_IMAGE_APP:latest
                docker push $DOCKERHUB_USERNAME/$DOCKER_IMAGE_APP:$DOCKER_TAG
                """
            }
        }

        stage('Deploy') {
            steps {
                sh 'docker compose up -d'
            }
        }
    }

    post {
        success {
            echo "Pipeline terminé avec succès"
        }
        failure {
            echo "Pipeline en échec"
        }
    }
}
