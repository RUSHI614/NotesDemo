pipeline {
    agent any

    environment {
        SERVICE_TYPE = "${env.JOB_NAME.contains('backend') ? 'backend' : 'frontend'}"
        IMAGE_NAME = "rushi614/notes-${SERVICE_TYPE}"
        SERVICE_DIR = "${SERVICE_TYPE}"
        COMPOSE_FILE = "docker-compose-${SERVICE_TYPE}.yml"
        COMPOSE_PROJECT_NAME = "${SERVICE_TYPE == 'backend' ? 'notebe' : 'notesfe'}"
        PORT_FILTER = "${SERVICE_TYPE == 'backend' ? '27017,8081' : '80'}"
    }

    parameters {
        choice(name: 'ENV', choices: ['dev', 'prod'], description: 'Environment to deploy to')
    }

    stages {
        stage('Initialize') {
            steps {
                echo '=== Initialize ==='
                checkout scm
            }
        }

        stage('Prepare Image Tag') {
            steps {
                echo '=== Prepare Image Tag ==='
                script {
                    env.IMG_TAG = sh(script: 'git rev-parse --short HEAD', returnStdout: true).trim()
                    echo "Using image tag: ${env.IMG_TAG}"
                }
            }
        }

        stage('Build & Push Image') {
            steps {
                echo "=== Build & Push ${SERVICE_TYPE.capitalize()} Image ==="
                dir(env.SERVICE_DIR) {
                    withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh '''
                            echo "Logging in to Docker Hub"
                            echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                            echo "Building Docker image ${IMAGE_NAME}:${IMG_TAG}"
                            docker build -t ${IMAGE_NAME}:${IMG_TAG} .
                            echo "Pushing Docker image to Docker Hub"
                            docker push ${IMAGE_NAME}:${IMG_TAG}
                        '''
                    }
                }
            }
        }

        stage('Prepare EC2 Instance') {
            steps {
                echo '=== Prepare EC2 Instance ==='
                sh 'echo "No EC2 preparation configured; skipping manual preparation."'
            }
        }

        stage('Transfer Backend Files') {
            steps {
                echo '=== Transfer Backend Files ==='
                sh 'echo "No remote transfer configured; this is a manual placeholder."'
            }
        }

        stage('Deploy') {
            steps {
                echo "=== Deploy ${SERVICE_TYPE.capitalize()} ==="
                withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                    sh '''
                        echo "Logging in to Docker Hub"
                        echo "$DOCKER_PASSWORD" | docker login -u "$DOCKER_USERNAME" --password-stdin
                        echo "Pulling image ${IMAGE_NAME}:${IMG_TAG} from Docker Hub"
                        docker pull ${IMAGE_NAME}:${IMG_TAG}
                        echo "Creating external network if it doesn't exist"
                        docker network create app_net || true
                        echo "Stopping any containers using ports ${PORT_FILTER}"
                        for port in $(echo ${PORT_FILTER} | tr ',' ' '); do
                            docker stop $(docker ps -q --filter "publish=$port") || true
                            docker rm $(docker ps -aq --filter "publish=$port") || true
                        done
                        echo "Cleaning up existing containers, volumes, and orphaned containers"
                        docker-compose -f ${COMPOSE_FILE} -p ${COMPOSE_PROJECT_NAME} down --volumes --remove-orphans || true
                        echo "Starting compose deployment"
                        docker-compose -f ${COMPOSE_FILE} -p ${COMPOSE_PROJECT_NAME} up -d
                    '''
                }
            }
        }

        stage('Mark Sentry Deployment') {
            steps {
                echo '=== Mark Sentry Deployment ==='
                sh 'echo "Sentry deployment marking is not configured in this simplified flow."'
            }
        }



    }

    post {
        success {
            echo "${SERVICE_TYPE.capitalize()} pipeline completed and deployed successfully"
        }
        failure {
            echo "${SERVICE_TYPE.capitalize()} pipeline failed during execution"
        }
    }
}

