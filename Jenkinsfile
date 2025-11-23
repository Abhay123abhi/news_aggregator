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
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
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
