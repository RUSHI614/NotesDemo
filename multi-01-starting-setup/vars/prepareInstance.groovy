def call() {
    def props = variables()
    echo "Preparing EC2 Instance: ${props.EC2_HOST}"

    sshagent(credentials: ['ec2-ssh-key']) {
        sh """
            ssh -o StrictHostKeyChecking=no ${props.EC2_USER}@${props.EC2_HOST} '
                echo "Step 1: System update"
                if sudo apt-get update; then
                    echo "Update successful."
                else
                    echo "Update failed, but continuing..."
                fi

                echo "Step 2: Check Docker accessibility"
                if docker --version > /dev/null 2>&1;then
                    echo "Docker is already installed: \$(docker --version)"
                else
                    echo "Docker not found. Installing docker.io..."
                    sudo apt-get install -y docker.io
                    echo "Waiting 10 seconds for service to initialize..."
                    sleep 10
                    sudo usermod -aG docker ${props.EC2_USER}
                    echo "Docker installed. Version: \$(docker --version)"
                fi

                echo "Step 3: Check Docker Compose"
                if docker compose version > /dev/null 2>&1; then
                    echo "Docker Compose is already installed."
                else
                    echo "Docker Compose not found. Installing docker-compose-v2..."
                    sudo apt-get install -y docker-compose-v2
                    echo "Waiting 5 seconds..."
                    sleep 5
                    echo "Docker Compose installed. Version: \$(docker compose version)"
                fi

                echo "Step 4: Setting permissions"
                sudo chmod 666 /var/run/docker.sock || true
                
                echo "Step 5: Check and create Docker network"
                if docker network inspect ${props.NETWORK_NAME} > /dev/null 2>&1; then
                    echo "Network ${props.NETWORK_NAME} already exists."
                else
                    echo "Network ${props.NETWORK_NAME} not found. Creating..."
                    docker network create ${props.NETWORK_NAME}
                fi
                
                echo "--- EC2 Preparation Complete ---"

                docker --version
                docker compose version
            '
        """
    }
}
