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

# Copy the static build output
COPY --from=build /app/build ./build

# Keep container alive for volume mounting
ENTRYPOINT ["sh", "-c", "echo 'Frontend container initialized. Static assets available in /app/build' && tail -f /dev/null"]

