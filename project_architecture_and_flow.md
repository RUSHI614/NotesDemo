# Complete Project Architecture & Flow

This document provides a comprehensive overview of the **rushiNotesApp**—a full-stack Goals Management application—along with a step-by-step breakdown of how it moves from code to a secure deployment on AWS.

---

## 0. Overall Project Idea

The **rushiNotesApp** is a multi-tier application designed for goal tracking. It leverages modern DevOps practices to ensure security, scalability, and automated delivery.

-   **Frontend**: A **React** application that serves as the user interface. It is built as static assets and served via **Nginx**.
-   **Backend**: A **Node.js/Express** API that handles business logic and communicates with the database.
-   **Database**: **MongoDB** is used as the persistent data store for user goals.
-   **Infrastructure**: The entire stack is containerized using **Docker** and orchestrated with **Docker Compose**.
-   **Security**: **Infisical** manages all sensitive configurations (DB URLs, API keys) which are injected at runtime, ensuring no secrets are stored in the code or images.
-   **CI/CD**: **Jenkins** pipelines (using shared libraries) automate the build, test, and deployment phases to **AWS EC2**.

---

## 1. Pipeline Initialization & Variable Sourcing

When you click **Build with Parameters** in Jenkins:

### A. The Parameters
Jenkins asks for the `ENV` (dev/prod). This is stored as `env.ENV`.

### B. Loading the Shared Library
The `Jenkinsfile` starts with `@Library('my-shared-lib') _`. This pulls your helper scripts (like `dockerBuildAndPush`) from your GitLab library repository.

### C. variables.groovy (The Config Central)
The line `def props = variables()` does the following:
1.  Executes `vars/variables.groovy`.
2.  Returns a **Groovy Map** containing all static configs (Registry URL, EC2 IP, Infisical URL, etc.).
3.  These are accessed via `props.VARIABLE_NAME`.

---

## 2. Standard Stages

| Stage | Action |
| :--- | :--- |
| **Initialize** | Runs `checkout scm` to pull the latest code from GitLab into the Jenkins workspace. |
| **Prepare Image Tag** | Runs `git rev-parse --short HEAD` to get the unique commit hash (e.g., `a1b2c3d`). This becomes `env.IMG_TAG`. |

---

## 3. Build & Push Stage

This stage calls the shared library `dockerBuildAndPush()`. Here is what happens inside that function:

1.  **Registry Login**: Uses `gitlab-registry-creds` from Jenkins Credentials to log into `props.REGISTRY`.
2.  **Docker Build**:
    - It passes Infisical configs as `--build-arg`. 
    - **Variables used**: `props.INFISICAL_API_URL`, `props.PROJECT_ID`, `env.INFISICAL_TOKEN` (from Jenkins creds), and `env.ENV`.
3.  **Docker Tag & Push**:
    - Tags the local image with the full registry path: `${props.REGISTRY}/${props.PROJECT}/backend:${env.IMG_TAG}`.
    - Pushes it to your GitLab Container Registry.

---

## 4. Transfer & Deployment Stage

### A. Environment Generation (`generateEnv()`)
The pipeline runs `generateEnv()` locally. This creates a temporary `.env` file containing:
- Registry URL, Project Name, and the **Image Tag** (`IMG_TAG`).
- **IMPORTANT**: This `.env` file is ONLY for Docker Compose to know which image version to pull. It does NOT contain secrets.

### B. File Transfer (`transferfile()`)
The `transferfile.groovy` script:
1.  Tars `docker-compose-backend.yml` and the `.env` file.
2.  Sends them to the EC2 instance using `scp`.
3.  Unzips them into `home/ubuntu/project/notes`.

### C. Remote Deployment (`dockerComposeDeploy()`)
The script SSHs into the EC2 and performs:
1.  **Exports**: It exports your Infisical credentials to the EC2 shell.
2.  **Docker Compose UP**: Runs `docker compose up -d`.
3.  **Image Pull**: Docker Compose reads `.env`, finds the correct `IMG_TAG`, and pulls the image from GitLab.

---

## 5. Runtime Secret Injection (The "Secret Sauce")

### A. Backend Injection
When the backend container starts on EC2, it runs `entrypoint.sh`:
1.  **Connectivity**: The container talks to `http://host.docker.internal:8091`. 
2.  **The Bridge**:
    - `host.docker.internal` ➡️ EC2 Host.
    - Port `8091` ➡️ `socat` relay ➡️ SSH Tunnel ➡️ **Local Machine (8090)**.
3.  **Infisical Run**: The command `infisical run --env ${ENV} -- npm start` is executed.
4.  **Injection**: Infisical fetches secrets and injects them directly into the Node.js process memory.

### B. Frontend Architecture Flow
The frontend uses a unique **Shared Volume** strategy:
1.  **Build Phase**: React is built into static files (`/app/build`).
2.  **Frontend Container**: 
    - Mounts its `/app/build` directory to a shared Docker volume (`frontend-static`).
    - Runs `infisical run` to initialize the environment.
3.  **Nginx Container**: 
    - Mounts the same `frontend-static` volume to its HTML root (`/usr/share/nginx/html`).
    - Serves the static React files to the user's browser.
    - Proxies `/api/` requests to the Backend service.

---

## 6. Post Stage (Cleanup)

Jenkins runs the `post` block:
- **Success/Failure**: Echoes status.
- **Always**: Runs `rm -f .env`. This deletes the temporary deployment config file from the Jenkins workspace to keep it clean.

---

## Variable Quick-Reference

| Variable Type | Source | Usage |
| :--- | :--- | :--- |
| **Static Config** | `variables.groovy` | Registry URL, Project IDs, EC2 Host. |
| **Secrets** | Infisical | DB passwords, API Keys (Runtime only). |
| **Credentials** | Jenkins Credentials | `INFISICAL_TOKEN`, Registry Password. |
| **Dynamic** | Git SCM | `IMG_TAG` (from commit hash). |




***Run these 2 comands on EC2 instance to create tunnel bridge relay which will help to connect infisical to local machine and fetch secrets at runtime***

sudo apt-get update && sudo apt-get install socat -y
socat TCP-LISTEN:8091,fork,reuseaddr TCP:127.0.0.1:8090 &


Implementing Tunnel Bridge Relay
I'm completing the setup for the SSH tunnel bridge. This involves switching the Infisical API port to the reachable relay port (8091) on the EC2 instance. I've also identified several 'permanent' solutions to avoid these manual steps in the future.there could be a way to 