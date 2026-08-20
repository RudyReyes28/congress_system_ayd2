pipeline {
    agent any

    environment {
        IMAGE_NAME = 'congress-backend'
        DEPLOY_DIR = '/opt/congress-backend'
    }

    stages {

        stage('Checkout') {
            steps { checkout scm }
        }

        stage('Test') {
            steps {
                sh 'mvn clean test -B'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }

        stage('Build Docker Image') {
            steps {
                sh 'docker build -t $IMAGE_NAME:latest -t $IMAGE_NAME:$BUILD_NUMBER .'
            }
        }

        stage('Deploy') {
            steps {
                sh '''
                    # Copiar archivos de configuracion al servidor
                    cp docker-compose.yml $DEPLOY_DIR/docker-compose.yml
                    cp nginx.conf         $DEPLOY_DIR/nginx.conf

                    # Levantar/actualizar contenedores
                    cd $DEPLOY_DIR
                    docker compose up -d --no-deps backend
                    docker compose up -d --no-deps nginx

                    # Limpiar imagenes viejas
                    docker image prune -f
                '''
            }
        }
    }

    post {
        failure { echo 'Pipeline del backend fallo' }
        success { echo 'Backend desplegado exitosamente' }
    }
}

