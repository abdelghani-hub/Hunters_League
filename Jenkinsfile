pipeline {
    agent {
        docker {
            image 'maven:3.8.8-eclipse-temurin-17'
            args '-v /var/run/docker.sock:/var/run/docker.sock'
        }
    }
    environment {
        SONAR_PROJECT_KEY = "Hunters_League"
        SONAR_TOKEN = "sqa_4bd153196a6890aaebaa8e23d1e59842a5563a0b"
        SONAR_HOST_URL = "http://host.docker.internal:9000"
    }
    stages {
        stage('Checkout Code') {
            steps {
                script {
                    echo "Checking out code from GitHub..."
                    checkout([$class: 'GitSCM',
                        branches: [[name: '*/main']],
                        userRemoteConfigs: [[
                            url: 'https://github.com/abdelghani-hub/Hunters_League.git'
                        ]]
                    ])
                }
            }
        }
        stage('Build and SonarQube Analysis') {
            steps {
                echo "Running Maven build and SonarQube analysis..."
                sh """
                mvn clean package sonar:sonar \
                  -Dsonar.projectKey=$SONAR_PROJECT_KEY \
                  -Dsonar.host.url=$SONAR_HOST_URL \
                  -Dsonar.login=$SONAR_TOKEN
                """
            }
        }
        stage('Quality Gate Check') {
            steps {
                script {
                    echo "Checking SonarQube Quality Gate..."
                    def qualityGate = sh(
                        script: """
                        curl -s -u "$SONAR_TOKEN:" \
                        "$SONAR_HOST_URL/api/qualitygates/project_status?projectKey=$SONAR_PROJECT_KEY" \
                        | tee response.json | jq -r '.projectStatus.status'
                        """,
                        returnStdout: true
                    ).trim()
                    if (qualityGate != "OK") {
                        error "Quality Gate failed! Stopping the build."
                    }
                    echo "Quality Gate passed! Proceeding..."
                }
            }
        }
        stage('Build Docker Image') {
            steps {
                script {
                    echo "Building Docker Image..."
                    sh 'docker build -t hunters_league_img .'
                }
            }
        }
        stage('Deploy Docker Container') {
            steps {
                script {
                    echo "Deploying Docker container..."
                    sh """
                    docker stop hunters_league_container || true
                    docker rm hunters_league_img || true
                    docker run -d --name hunters_league_container hunters_league_img
                    """
                }
            }
        }
    }
    post {
        success {
            echo "Build succeeded !"
            mail to: 'aaittamghart8@gmail.com',
                 subject: "SUCCESS: Build #${env.BUILD_NUMBER} - ${env.JOB_NAME}",
                 body: "Build succeeded.\n\nDetails : ${env.BUILD_URL}"
            slackSend color: 'good',
                      message: "SUCCESS: Build #${env.BUILD_NUMBER} of ${env.JOB_NAME} succeeded!\nDetails: ${env.BUILD_URL}"
        }
        failure {
            echo "Build failed !"
            mail to: 'aaittamghart8@gmail.com',
                 subject: "FAILURE: Build #${env.BUILD_NUMBER} - ${env.JOB_NAME}",
                 body: "Build failed.\n\nDetails : ${env.BUILD_URL}"
            slackSend color: 'danger',
                      message: "FAILURE: Build #${env.BUILD_NUMBER} of ${env.JOB_NAME} failed!\nDetails: ${env.BUILD_URL}"
        }
        always {
            echo "Pipeline execution complete."
        }
    }
}
