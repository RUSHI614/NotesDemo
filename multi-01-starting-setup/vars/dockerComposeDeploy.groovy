def call(String composeFile) {
    def props = variables()
    echo "Deploying using ${composeFile} to EC2: ${props.EC2_HOST}"
    
    withCredentials([usernamePassword(
        credentialsId: 'gitlab-registry-creds',
        usernameVariable: 'REG_USER',
        passwordVariable: 'REG_PASS'
    )]) {
        sshagent(credentials: ['ec2-ssh-key']) {
            sh """
                ssh -o StrictHostKeyChecking=no ${props.EC2_USER}@${props.EC2_HOST} "
                    cd ${props.TARGET_DIR}
                    
                    export INFISICAL_API_URL='${props.INFISICAL_API_URL}'
                    export INFISICAL_TOKEN='${env.INFISICAL_TOKEN}'
                    export PROJECT_ID='${props.PROJECT_ID}'
                    export ENV='${env.ENV}'
                    export REGISTRY='${props.REGISTRY}'
                    export PROJECT='${props.PROJECT}'
                    export IMG_TAG='${env.IMG_TAG}'
                    export NETWORK_NAME='${props.NETWORK_NAME}'

                    echo '${REG_PASS}' | docker login ${props.REGISTRY} -u '${REG_USER}' --password-stdin
                    docker compose -f ${composeFile} pull
                    docker compose -f ${composeFile} up -d
                "
            """
        }
    }
    echo "Remote deployment of ${composeFile} completed"
}
