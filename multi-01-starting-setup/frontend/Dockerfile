# ---------- Build stage ----------
FROM node:20.16-alpine AS build
WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install
COPY . .

# Build the react app (Secrets are not needed here if not used by the app)
RUN npm run build

# ---------- Runtime stage ----------
FROM node:20.16-alpine
# FROM alpine:3.20
WORKDIR /app

# Install Infisical CLI in the runtime stage
RUN apk add --no-cache bash curl && curl -1sLf \
  'https://dl.cloudsmith.io/public/infisical/infisical-cli/setup.alpine.sh' | bash \
  && apk add infisical

# Copy the static build output
COPY --from=build /app/build ./build

# Use the direct infisical run command to fetch secrets at runtime and keep container alive
ENTRYPOINT ["sh", "-c", "infisical run --token=$INFISICAL_TOKEN --domain=$INFISICAL_API_URL --projectId=$PROJECT_ID --env=$ENV -- sh -c 'echo ✅ Success: Secrets successfully injected into the runtime environment! && echo 🚀 Frontend container initialized. Serving static assets via shared volume... && tail -f /dev/null'"]

