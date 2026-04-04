pipeline {
    agent any

    environment {
        DOCKER_USERNAME = 'abhayjais'
        BACKEND_IMAGE = "${DOCKER_USERNAME}/news-backend"
        FRONTEND_IMAGE = "${DOCKER_USERNAME}/news-frontend"
        TAG = "${BUILD_NUMBER}"
    }

    stages {

        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') { sh './gradlew build' }
            }
        }

        stage('Docker Build Backend') {
            steps {
                sh "docker build -t ${BACKEND_IMAGE}:${TAG} ./backend"
            }
        }

        stage('Docker Build Frontend') {
            steps {
                sh "docker build -t ${FRONTEND_IMAGE}:${TAG} ./frontend"
            }
        }

        stage('Push Images') {
            steps {
                // Use credentials here instead of environment block
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                 usernameVariable: 'DOCKER_USERNAME',
                                                 passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh """
                    echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                    docker push ${BACKEND_IMAGE}:${TAG}
                    docker push ${BACKEND_IMAGE}:latest
                    docker push ${FRONTEND_IMAGE}:${TAG}
                    docker push ${FRONTEND_IMAGE}:latest
                    """
                }
            }
        }

        stage('Load Images to Minikube') {
            steps {
                sh "minikube image load ${BACKEND_IMAGE}:${TAG}"
                sh "minikube image load ${FRONTEND_IMAGE}:${TAG}"
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh 'kubectl apply -f k8s/'
            }
        }

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