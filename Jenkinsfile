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
                dir('backend') {
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build'
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
                    args '--network host --entrypoint="" -u root'
                }
            }
            steps {
                withCredentials([file(credentialsId: 'kubeconfig-id', variable: 'KUBECONFIG_FILE')]) {
                    sh '''
                    export KUBECONFIG=$KUBECONFIG_FILE

                    echo "Using kubeconfig:"
                    cat $KUBECONFIG_FILE

                    echo "Checking cluster access..."
                    kubectl get nodes

                    echo "Applying Kubernetes manifests..."
                    kubectl apply -f k8s/ --validate=false

                    echo "Updating deployments..."
                    kubectl set image deployment/news-backend news-backend=${BACKEND_IMAGE}:${TAG}
                    kubectl set image deployment/news-frontend news-frontend=${FRONTEND_IMAGE}:${TAG}

                    echo "Waiting for rollout..."
                    kubectl rollout status deployment/news-backend
                    kubectl rollout status deployment/news-frontend
                    '''
                }
            }
        }

        stage('Verify Deployment') {
            agent {
                docker {
                    image 'bitnami/kubectl:latest'
                    args '--network host --entrypoint="" -u root'
                }
            }
            steps {
                withCredentials([file(credentialsId: 'kubeconfig-id', variable: 'KUBECONFIG_FILE')]) {
                    sh '''
                    export KUBECONFIG=$KUBECONFIG_FILE
                    kubectl get pods
                    kubectl get svc
                    kubectl get deployment
                    '''
                }
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