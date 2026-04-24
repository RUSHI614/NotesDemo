# ---------- Builder Stage ----------
FROM node:20.16-alpine AS builder

WORKDIR /app

COPY package.json package-lock.json ./
RUN npm install --only=production

COPY . .

# ---------- Runtime Stage ----------
FROM node:20.16-alpine

WORKDIR /app

# ONLY copy necessary files
COPY --from=builder /app/package.json ./
COPY --from=builder /app/node_modules ./node_modules
COPY --from=builder /app/app.js ./
COPY --from=builder /app/instrument.js ./
COPY --from=builder /app/models ./models

# Create logs directory for access.log
RUN mkdir logs

EXPOSE 80

ENTRYPOINT ["node", "app.js"]
