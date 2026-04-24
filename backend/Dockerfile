# ---------- Builder Stage ----------
FROM node:20.16-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install --only=production

COPY . .

# ---------- Runtime Stage ----------
FROM node:20.16-alpine

WORKDIR /app

# Install Infisical CLI in the runtime stage
RUN apk add --no-cache bash curl && curl -1sLf \
  'https://dl.cloudsmith.io/public/infisical/infisical-cli/setup.alpine.sh' | bash \
  && apk add infisical

# ONLY copy necessary files
COPY --from=builder /app/package.json ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/app.js ./
COPY --from=builder /app/instrument.js ./
COPY --from=builder /app/models ./models

# Create logs directory for access.log
RUN mkdir logs

EXPOSE 80

# Use the direct infisical run command to inject secrets at runtime
ENTRYPOINT ["sh", "-c", "infisical run --token=$INFISICAL_TOKEN --domain=$INFISICAL_API_URL --projectId=$PROJECT_ID --env=$ENV -- npm start"]
