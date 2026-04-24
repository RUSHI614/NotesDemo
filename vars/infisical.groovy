import groovy.transform.Field
@Field Map cache = [:]
@Field boolean cliReady = false

/**
 * Initializes the agent: ensures Infisical CLI is installed.
 */
def call() {
    if (cliReady) return
    if (sh(script: "infisical --version", returnStatus: true) != 0) {
        echo "📥 Installing Infisical CLI..."
        sh "curl -1sLf 'https://dl.cloudsmith.io/public/infisical/infisical-cli/setup.deb.sh' | sudo -E bash && sudo apt-get install -y infisical"
    }
    cliReady = true
}


/**
 * Handles on-demand fetching: ${infisical.VAR}
 */
def propertyMissing(String name) {
    if (cache.containsKey(name)) return cache[name]
    if (!env.INFISICAL_TOKEN || !env.PROJECT_ID || !env.ENV) return null
    if (!cliReady) call()

    try {
        def val = sh(script: "infisical get ${name} --token=$INFISICAL_TOKEN --domain=$INFISICAL_API_URL --projectId=$PROJECT_ID --env=$ENV --plain", returnStdout: true).trim()
        if (val) {
            echo "🔍 Fetched: ${name}"
            cache[name] = val
            return val
        }
    } catch (e) { /* ignore */ }
    return null
}
