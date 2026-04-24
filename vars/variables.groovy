def call() {
    return [
        APP_PATH: "day5/multi-01-starting-setup",
        BACKEND_COMPOSE_FILE: "docker-compose-backend.yml",
        FRONTEND_COMPOSE_FILE: "docker-compose-frontend.yml",
        BACKEND_IMG_NAME: 'backend',
        FRONTEND_IMG_NAME: 'frontend',
        EC2_HOST: '13.233.21.44',
        EC2_USER: "ubuntu",
        TARGET_DIR: "home/ubuntu/project/notes",
        NETWORK_NAME: "app-network",
        DOMAIN_NAME: "example.com",
        REGISTRY: "registry.gitlab.webelight.co.in",
        PROJECT: "webelight/devops/training",
        INFISICAL_API_URL: "http://13.233.21.44:8091",
        PROJECT_ID: "e957082a-6386-4388-90c9-f9066f4f1c3e",
        SENTRY_ORG: "webelight-sentry",
        SENTRY_PROJECT: "rushinotes-backend",
        SENTRY_URL: "https://sentry.webelight.co.in"
    ]
}   