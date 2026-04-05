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
                deleteDir()
                checkout scm
            }
        }

        stage('Build Backend') {
            steps {
                dir('backend') { sh './gradlew build' }
            }
        }

        stage('Build Frontend') {
            steps {
                dir('frontend') {
                    sh """
                    docker run --rm \
                      -v \$(pwd):/app \
                      -w /app \
                      node:18 \
                      sh -c "npm install && npm run build"
                    """
                }
            }
        }

        stage('Docker Build') {
                    steps {
                        sh """
                        docker build -t ${BACKEND_IMAGE}:${TAG} -t ${BACKEND_IMAGE}:latest ./backend
                        docker build -t ${FRONTEND_IMAGE}:${TAG} -t ${FRONTEND_IMAGE}:latest ./frontend
                        """
                    }
                }

        stage('Push Images to Docker Hub') {
            steps {
                withCredentials([usernamePassword(credentialsId: 'dockerhub-creds',
                                                 usernameVariable: 'DOCKER_USERNAME',
                                                 passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh """
                     echo $DOCKER_PASSWORD | docker login -u $DOCKER_USERNAME --password-stdin
                     docker push ${BACKEND_IMAGE}:${TAG}
                     docker push ${FRONTEND_IMAGE}:${TAG}
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
            steps {
                sh """
                echo "Replacing TAG in YAML..."
                sed -i "s|\${TAG}|${TAG}|g" k8s/*.yaml

                echo "Deploying to Kubernetes..."
                kubectl apply -f k8s/
                """
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