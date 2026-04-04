pipeline {
    agent any

    environment {
        // Jenkins credentials ID (stored securely in Jenkins)
        DOCKER_CREDENTIALS = 'dockerhub-creds'

        // Docker username (you can hardcode this or also store in Jenkins credentials)
        DOCKER_USERNAME = 'yourusername'

        // Image names
        BACKEND_IMAGE = "${DOCKER_USERNAME}/news-backend"
        FRONTEND_IMAGE = "${DOCKER_USERNAME}/news-frontend"

        // Version tag for images
        TAG = "${BUILD_NUMBER}"
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
                script {
                    backendImage = docker.build("${BACKEND_IMAGE}:${TAG}", "./backend")
                }
            }
        }

        // ---------------- FRONTEND ----------------
        stage('Docker Build Frontend') {
            steps {
                script {
                    frontendImage = docker.build("${FRONTEND_IMAGE}:${TAG}", "./frontend")
                }
            }
        }

        // ---------------- PUSH TO DOCKER HUB ----------------
        stage('Push Images') {
            steps {
                script {
                    docker.withRegistry('https://index.docker.io/v1/', DOCKER_CREDENTIALS) {
                        backendImage.push("${TAG}")
                        backendImage.push("latest")

                        frontendImage.push("${TAG}")
                        frontendImage.push("latest")
                    }
                }
            }
        }

        // ---------------- MINIKUBE ----------------
        stage('Load Images to Minikube') {
            steps {
                sh "minikube image load ${BACKEND_IMAGE}:${TAG}"
                sh "minikube image load ${FRONTEND_IMAGE}:${TAG}"
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