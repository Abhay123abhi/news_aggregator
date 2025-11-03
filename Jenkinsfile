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
            steps {
                dir('backend') {
                    // Use Maven installed in Jenkins (no Docker for Maven)
                    sh 'mvn clean package -DskipTests'
                }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    // Build frontend using Node inside Docker
                    sh 'docker run --rm -v $(pwd):/app -w /app node:18-alpine sh -c "npm install && npm run build"'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                // Build combined Docker image for backend + frontend
                sh 'docker build -t ${IMAGE_NAME} .'
            }
        }

        stage('Run Container') {
            steps {
                // Start the app using docker-compose
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
