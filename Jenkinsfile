pipeline {
    agent any

    environment {
        IMAGE_NAME = 'news-aggregator:local'
    }

    stages {
        stage('Checkout') {
            steps {
                git 'https://github.com/Abhay123abhi/news_aggregator.git'
            }
        }

        stage('Build Backend') {
            agent {
                docker {
                    image 'maven:3.9.9-eclipse-temurin-17'
                    args '-v /root/.m2:/root/.m2'
                }
            }
            steps {
                dir('backend') {
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
            agent {
                docker {
                    image 'node:18-alpine'
                }
            }
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t ${IMAGE_NAME} .'
            }
        }

        stage('Run Container') {
            steps {
                sh 'docker-compose up -d'
            }
        }
    }

    post {
        success {
            echo "✅ Application successfully deployed on Docker (http://localhost:8080)"
        }
        failure {
            echo "❌ Build failed! Check logs in Jenkins console."
        }
    }
}
