pipeline {
    agent {
        docker {
            image 'maven:3.8.8-eclipse-temurin-17'
            args '''
                -v /var/run/docker.sock:/var/run/docker.sock
                -v /usr/bin/docker:/usr/bin/docker
                - /var/run/docker.sock:/var/run/docker.sock
                - /usr/bin/docker/cli-plugins:/usr/bin/docker/cli-plugins
                --network samurai_net
            '''
        }
    }
    environment {
        SONAR_PROJECT_KEY = "Hunters_League"
        SONAR_TOKEN = "sqa_4bd153196a6890aaebaa8e23d1e59842a5563a0b"
        SONAR_HOST_URL = "http://host.docker.internal:9001"
        DOCKER_IMAGE_NAME = "hunters_league_img"
        DOCKER_CONTAINER_NAME = "hunters_league_container"
    }
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        stage('Build and SonarQube Analysis') {
            steps {
                echo "Running Maven build and SonarQube analysis..."
                withSonarQubeEnv('MySonarQubeServer') {
                    sh """
                    mvn clean package sonar:sonar \
                      -Dsonar.projectKey=$SONAR_PROJECT_KEY \
                      -Dsonar.host.url=$SONAR_HOST_URL \
                      -Dsonar.login=$SONAR_TOKEN \
                      -Dsonar.ws.timeout=600
                    """
                }
            }
        }
        stage('Quality Gate Check') {
            steps {
                script {
                    echo "Waiting for SonarQube Quality Gate..."
                    def qualityGate = waitForQualityGate()
                    if (qualityGate.status != 'OK') {
                        error "Quality Gate failed! Stopping the build."
                    }
                    echo "Quality Gate passed! Proceeding..."
                }
            }
        }
        stage('Build') {
            steps {
                script {
                    echo "Deploying Docker container..."
                    sh """
                    docker stop ${DOCKER_CONTAINER_NAME} || true
                    docker rm ${DOCKER_CONTAINER_NAME} || true
                    docker rmi ${DOCKER_IMAGE_NAME} || true
                    docker build . -t $DOCKER_IMAGE_NAME:${BUILD_NUMBER}
                    """
                }
            }
        }
        stage('Deploy'){
            steps{
                script {
                    echo "Deploying Docker container..."
                    sh """
                    docker compose up
                    """
                }
            }
        }
    }
    post {
        failure {
            echo 'Pipeline failed! Sending notifications...'
        }
        success {
            echo 'Pipeline succeeded! Deployment completed.'
        }
    }
}