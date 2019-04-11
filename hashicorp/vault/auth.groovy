#! /usr/bin/groovy
/*
 * Jenkins Modules: Vault Auth helper.
 *
 * IMPORTANT: this module relies heavily on the vault CLI to run.
 */

/*
 * This variable needs to be set once if your vault address is different.
 * This is a workaround to avoid having to pass this to every function.
 */
String vaultAddress = 'https://127.0.0.1:8200'

/*
 * Log in to vault with the given token.
 *
 * IMPORTANT: only Github auth is supported, so your Github Personal Access
 * Token should be passed to this.
 */
def login(String token) {
    try {
        String cmd = """
            set +x
            ${getExportVaultAddress()}
            vault login -method=github -format=json token=\"${token}\"
        """
        String out = sh(returnStdout: true, script: cmd).trim()
        def jsonOut = parseJson(out)
        
        if (jsonOut.auth && jsonOut.auth.client_token)
            return true
        
    } catch (ex) {
        println "[ERROR] Unable to login with given token"
        println ex
    }
    
    return false
}

/*
 * Check if you are already logged in -- which means your token is still valid.
 */
def isLoggedIn() {
    try {
        String cmd = """
            set +x
            ${getExportVaultAddress()}
            vault token lookup -format=json
        """
        String out = sh(returnStdout: true, script: cmd).trim()
        def jsonOut = parseJson(out)
        
        if (jsonOut.data && jsonOut.data.id)
            return true
        
    } catch (ex) {
        // Vault CLI will return an exit code of 2 when an token is not found in
        // current context; that is expected, no need to show errors for that
        if (ex.toString().indexOf("exit code 2") != -1) {
            return false
        } else {
            println "[ERROR] Unexpected error while looking up for a token"
            println ex
        }
    }
    
    return false
}

/**
 ** Function:
 * Parse the given JSON encoded string. It uses Jenkins' readJSON utility which is so much better
 * than Groovy's JSONSluper.
 *
 *Ref Link: https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readjson-read-json-from-files-in-the-workspace
 *
 ** Parameters:
 * @param String jsonString    A string containing the JSON formatted data. Data could be access as an array or a map.
 */
def parseJson(String jsonString) {
    def decodedJson = null
    try {
        // Using JsonSlurper here because 'readJson' outputs the string being parsed
        decodedJson = new groovy.json.JsonSlurper().parseText(jsonString)
    } catch (ex) {
        println "[ERROR] Unable to parse JSON using jsonString=${jsonString}"
        println ex
    }
    return decodedJson
}

def getExportVaultAddress() {
    return "export VAULT_ADDR=${vaultAddress}"
}

return this