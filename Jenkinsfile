pipeline {
    agent any

    environment {
        BACKEND_IMAGE = "news-backend"
        FRONTEND_IMAGE = "news-frontend"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        // ---------------- BACKEND ----------------
        stage('Build Backend (Gradle)') {
            steps {
                dir('backend') {
                    sh './gradlew build'
                }
            }
        }

        stage('Docker Build Backend') {
            steps {
                sh 'docker build -t $BACKEND_IMAGE ./backend'
            }
        }

        // ---------------- FRONTEND ----------------
        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Docker Build Frontend') {
            steps {
                sh 'docker build -t $FRONTEND_IMAGE ./frontend'
            }
        }

        // ---------------- MINIKUBE ----------------
        stage('Load Images to Minikube') {
            steps {
                sh 'minikube image load $BACKEND_IMAGE'
                sh 'minikube image load $FRONTEND_IMAGE'
            }
        }

        // ---------------- DEPLOY ----------------
        stage('Deploy to Kubernetes') {
            steps {
                sh 'kubectl apply -f k8s/'
            }
        }

        // ---------------- VERIFY ----------------
        stage('Verify Deployment') {
            steps {
                sh 'kubectl get pods'
                sh 'kubectl get svc'
            }
        }
    }

    post {
        success {
            echo "✅ Full stack deployed successfully 🚀"
        }
        failure {
            echo "❌ Pipeline failed. Check logs."
        }
    }
}