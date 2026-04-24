// @Library('my-shared-lib') _

// def props = variables()

// pipeline {
//     agent none

//     environment {
//         INFISICAL_TOKEN   = credentials('infisical-machine-token')
//         INFISICAL_API_URL = 'http://15.206.203.171:8090'
//         CLEAN_DEPLOY      = 'true'
//     }

//     stages {

//         stage('Initialize') {
//             agent any
//             steps {
//                 script {
//                     try {
//                         checkout scm
//                     } catch (e) {
//                          error "Initialization failed: ${e.getMessage()}"
//                     }
//                 }
//             }
//         }

//         stage('Prepare Image Tag') {
//             agent any
//             steps {
//                 script {
//                     def commitId = sh(
//                         script: 'git rev-parse --short HEAD',
//                         returnStdout: true
//                     ).trim()

//                     env.IMG_TAG = "${env.BRANCH}-${BUILD_NUMBER}-${commitId}"
//                     echo "Image tag: ${env.IMG_TAG}"
//                 }
//             }
//         }

//         stage('Build & Push Backend') {
//             agent any
//             environment {
//                 IMG_NAME = 'backend'
//             }
//             steps {
//                 dir("${env.APP_PATH}/backend") {
//                     dockerBuildAndPush()
//                 }
//             }
//         }

//         stage('Build & Push Frontend') {
//             agent any
//             environment {
//                 IMG_NAME = 'frontend'
//             }
//             steps {
//                 dir("${env.APP_PATH}/frontend") {
//                     dockerBuildAndPush()
//                 }
//             }
//         }


//         stage('Deploy with Docker Compose') {
//             agent any
//             steps {
//                 dir(env.APP_PATH) {
//                     dockerComposeDeploy()
//                 }
//             }
//         }
//     }

//     post {
//         success {
//             echo "✅ Pipeline completed successfully"
//         }
//         failure {
//             echo "❌ Pipeline failed"
//         }
//         always {
//             node {
//                 echo "Cleaning up workspace..."
//             }
//         }
//     }
// }   
