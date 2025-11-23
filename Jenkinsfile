pipeline {
    agent any

    environment {
        IMAGE_NAME = 'news-aggregator:local'
    }

    stages {

        stage('Checkout') {
            steps {
                git branch: 'main', url: 'https://github.com/Abhay123abhi/news_aggregator.git'
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') {
                    sh '''
                        docker run --rm \
                        -v $(pwd):/app \
                        -w /app \
                        maven:3.9-eclipse-temurin-17 \
                        mvn clean package -DskipTests
                    '''
                }
            }
        }


        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh '''
                        docker run --rm \
                        -v $(pwd):/app \
                        -w /app \
                        node:18-alpine \
                        sh -c "npm install && npm run build"
                    '''
                }
            }
        }


        stage('Build Docker Image') {
            steps {
                sh 'docker build -t ${IMAGE_NAME} .'
            }
        }
    }

    post {
        success {
            echo "✅ Application successfully deployed on Docker"
        }
        failure {
            echo "❌ Build failed! Check logs."
        }
    }
}
