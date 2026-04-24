def call() {
    def props = variables()

    echo "Building & pushing Docker image"
    echo "Image: ${env.IMG_NAME}"
    echo "Tag: ${env.IMG_TAG}"

    withCredentials([usernamePassword(
        credentialsId: 'gitlab-registry-creds',
        usernameVariable: 'REG_USER',
        passwordVariable: 'REG_PASS'
    )]) {

        sh """
            echo "Logging into ${props.REGISTRY}..."
            echo "${REG_PASS}" | docker login ${props.REGISTRY} -u "${REG_USER}" --password-stdin

            docker build \
              --build-arg INFISICAL_API_URL=${props.INFISICAL_API_URL} \
              --build-arg INFISICAL_TOKEN=${env.INFISICAL_TOKEN} \
              --build-arg PROJECT_ID=${props.PROJECT_ID} \
              --build-arg ENV=${env.ENV} \
              -t ${env.IMG_NAME}:${env.IMG_TAG} .

            docker tag ${env.IMG_NAME}:${env.IMG_TAG} \
              ${props.REGISTRY}/${props.PROJECT}/${env.IMG_NAME}:${env.IMG_TAG}

            docker push ${props.REGISTRY}/${props.PROJECT}/${env.IMG_NAME}:${env.IMG_TAG}
        """
    }

    echo "Docker image pushed successfully"
}
