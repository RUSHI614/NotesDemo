def call() {
    def props = variables()
    echo "Generating .env from configuration..."
    
    sh """
        cat > ${props.APP_PATH}/.env <<EOF
REGISTRY=${props.REGISTRY}
PROJECT=${props.PROJECT}
NETWORK_NAME=${props.NETWORK_NAME}
DOMAIN_NAME=${props.DOMAIN_NAME}
IMG_TAG=${env.IMG_TAG}
SENTRY_RELEASE=rushinotes-backend@${env.IMG_TAG}
EOF
    """
    
    echo "✅ .env file successfully generated."
}
