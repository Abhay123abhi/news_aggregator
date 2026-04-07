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
                    agent {
                        docker {
                            image 'gradle:7.6-jdk17-alpine'
                            args '-v $HOME/.gradle:/home/gradle/.gradle'
                        }
                    }
                    steps {
                        sh './gradlew clean build -x test'
                    }
                }

        stage('Build Frontend') {
                    agent {
                        docker {
                            image 'node:18-alpine'
                        }
                    }
                    steps {
                        sh 'npm install'
                        sh 'npm run build'
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
                     docker push ${BACKEND_IMAGE}:latest

                     docker push ${FRONTEND_IMAGE}:${TAG}
                     docker push ${FRONTEND_IMAGE}:latest
                    """
                }
            }
        }

        stage('Deploy to Kubernetes') {
                    agent {
                                    docker {
                                        image 'bitnami/kubectl:latest'
                                    }
                                }
                    steps {
                        withCredentials([file(credentialsId: 'kubeconfig-id', variable: 'KUBECONFIG')]) {
                            sh """
                            echo "Applying Kubernetes manifests..."
                            kubectl apply -f k8s/

                            echo "Updating deployments with new images..."
                            kubectl set image deployment/news-backend news-backend=${BACKEND_IMAGE}:${TAG}
                            kubectl set image deployment/news-frontend news-frontend=${FRONTEND_IMAGE}:${TAG}

                            echo "Waiting for rollout to finish..."
                            kubectl rollout status deployment/news-backend
                            kubectl rollout status deployment/news-frontend
                            """
                        }
                    }
                }

        stage('Verify Deployment') {
            agent {
                            docker {
                                image 'bitnami/kubectl:latest'
                            }
                        }
            steps {
                sh 'kubectl get pods'
                sh 'kubectl get svc'
                sh 'kubectl get deployment'
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