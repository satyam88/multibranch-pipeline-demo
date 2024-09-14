pipeline {
    agent any

    environment {
        AWS_ACCOUNT_ID = "533267238276"
        REGION = "ap-south-1"
        ECR_URL = "${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com"
        BRANCH_NAME = "${env.BRANCH_NAME}"
        IMAGE_NAME = "satyam88/multibranch-pipeline-demo:dev-multibranch-pipeline-demo-v.1.${env.BUILD_NUMBER}"
        ECR_IMAGE_NAME = "${AWS_ACCOUNT_ID}.dkr.ecr.${REGION}.amazonaws.com/multibranch-pipeline-demo:dev-multibranch-pipeline-demo-v.1.${env.BUILD_NUMBER}"
    }

    options {
        buildDiscarder(logRotator(numToKeepStr: '5', artifactNumToKeepStr: '5'))
    }

    tools {
        maven 'maven_3.9.4'
    }

    stages {
        stage('Build and Test for Dev') {
            when {
                branch 'dev'
            }
            stages {
                stage('Code Compilation') {
                    steps {
                        echo 'Code Compilation in Progress'
                        sh 'mvn clean compile'
                        echo 'Code Compilation Completed'
                    }
                }

                stage('JUnit Test Execution') {
                    steps {
                        echo 'Running JUnit Tests'
                        sh 'mvn clean test'
                        echo 'JUnit Tests Completed'
                    }
                }

                stage('Package Application') {
                    steps {
                        echo 'Packaging WAR Artifact'
                        sh 'mvn clean package'
                        echo 'WAR Artifact Created'
                    }
                }

                stage('Build and Tag Docker Image') {
                    steps {
                        echo "Building Docker Image: ${env.IMAGE_NAME}"
                        sh "docker build -t ${env.IMAGE_NAME} ."
                        echo 'Docker Image Built Successfully'
                    }
                }

                stage('Push Docker Image to DockerHub') {
                    steps {
                        withCredentials([usernamePassword(credentialsId: 'DOCKER_HUB_CRED', usernameVariable: 'DOCKER_USERNAME', passwordVariable: 'DOCKER_PASSWORD')]) {
                            echo "Pushing Docker Image to DockerHub: ${env.IMAGE_NAME}"
                            sh "docker login -u ${DOCKER_USERNAME} -p ${DOCKER_PASSWORD}"
                            sh "docker push ${env.IMAGE_NAME}"
                            echo "DockerHub Push Completed"
                        }
                    }
                }

                stage('Push Docker Image to Amazon ECR') {
                    steps {
                        echo "Tagging Docker Image for ECR: ${env.ECR_IMAGE_NAME}"
                        sh "docker tag ${env.IMAGE_NAME} ${env.ECR_IMAGE_NAME}"
                        echo "Pushing Docker Image to ECR: ${env.ECR_IMAGE_NAME}"

                        withDockerRegistry([credentialsId: 'ecr:ap-south-1:ecr-credentials', url: "https://${ECR_URL}"]) {
                            sh "docker push ${env.ECR_IMAGE_NAME}"
                        }
                        echo "Docker Image Push to ECR Completed"
                    }
                }
            }
        }

        stage('Deploy to Development Environment') {
            when {
                branch 'dev'
            }
            steps {
                script {
                    echo "Deploying to Dev Environment"
                    def yamlFile = 'kubernetes/dev/05-deployment.yaml'
                    sh "sed -i 's/<latest>/dev-multibranch-pipeline-demo-v.1.${BUILD_NUMBER}/g' ${yamlFile}"

                    withCredentials([file(credentialsId: 'kubeconfig-dev', variable: 'KUBECONFIG')]) {
                        sh "kubectl apply -f kubernetes/dev/*.yaml --kubeconfig $KUBECONFIG"
                    }
                    echo "Deployment to Dev Completed"
                }
            }
        }

        stage('Deploy to Preprod Environment') {
            when {
                branch 'preprod'
            }
            steps {
                script {
                    echo "Deploying to Preprod Environment"
                    def yamlFile = 'kubernetes/preprod/05-deployment.yaml'
                    sh "sed -i 's/<latest>/preprod-multibranch-pipeline-demo-v.1.${BUILD_NUMBER}/g' ${yamlFile}"

                    withCredentials([file(credentialsId: 'kubeconfig-preprod', variable: 'KUBECONFIG')]) {
                        sh "kubectl apply -f kubernetes/preprod/*.yaml --kubeconfig $KUBECONFIG"
                    }
                    echo "Deployment to Preprod Completed"
                }
            }
        }

        stage('Deploy to Production Environment') {
            when {
                branch 'prod'
            }
            steps {
                script {
                    echo "Deploying to Prod Environment"
                    def yamlFile = 'kubernetes/prod/05-deployment.yaml'
                    sh "sed -i 's/<latest>/prod-multibranch-pipeline-demo-v.1.${BUILD_NUMBER}/g' ${yamlFile}"

                    withCredentials([file(credentialsId: 'kubeconfig-prod', variable: 'KUBECONFIG')]) {
                        sh "kubectl apply -f kubernetes/prod/*.yaml --kubeconfig $KUBECONFIG"
                    }
                    echo "Deployment to Prod Completed"
                }
            }
        }
    }

    post {
        success {
            echo "Deployment to ${env.BRANCH_NAME} environment completed successfully"
        }
        failure {
            echo "Deployment to ${env.BRANCH_NAME} environment failed. Check logs for details."
        }
    }
}
