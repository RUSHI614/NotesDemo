#!/bin/bash
set -e

echo "🔐 Demonstrating Infisical secret injection for frontend..."

# Using 'infisical run' to inject secrets into the process environment.
exec infisical run --token="$INFISICAL_TOKEN" --domain="$INFISICAL_API_URL" --projectId="$PROJECT_ID" --env="$ENV" -- sh -c '
    echo "✅ Success: Secrets successfully injected into the runtime environment!"
    echo "🚀 Frontend container initialized. Serving static assets via shared volume..."
    tail -f /dev/null
'
