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
 * List secrets at the given key path.
 */
def list(String keyPath) {
    try {
        String cmd = """
            set +x
            ${getExportVaultAddress()}
            vault kv list -format=json ${keyPath}
        """
        String out = sh(returnStdout: true, script: cmd).trim()
        return parseJson(out)

    } catch (ex) {
        // Vault CLI will return an exit code of 2 when no secrets are found at
        // the given path which is not an error so we return an empty list here
        if (ex.toString().indexOf("exit code 2") != -1) {
            return []
        } else {
            println "[ERROR] Unable to list secrets with keyPath=${keyPath}"
            println ex
        }
    }
    return null
}

/*
 * Get all entries for the given key. This assumes that you have a single secret
 * with one or multiple entries in it, so a single get operation is needed to
 * retrieve all of those entries. The alternative would be having a single entry
 * per secret, so you would have to list all of them first, and then retrieve
 * them one by one which is more expensive in terms of time/networking.
 */
def get(String key) {
    try {
        String cmd = """
            set +x
            ${getExportVaultAddress()}
            vault kv get -format=json ${key}
        """
        String out = sh(returnStdout: true, script: cmd).trim()
        def jsonOut = parseJson(out)

        if (jsonOut.data && jsonOut.data.data) {
            return jsonOut.data.data
        }

    } catch (ex) {
        println "[ERROR] Unable to get secret with key=${key}"
        println ex
    }
    
    return [:]
}

/*
 * Put all given entries (one or many) into the single secret identified by the
 * given key.
 */
def put(String key, def entriesMap) {
    if (entriesMap.size() <= 0)
        return false

    String flatParams = ""
    entriesMap.each { itemName, itemValue ->
        flatParams += itemName + "='" + itemValue + "' "
    }
    try {
        String cmd = """
            set +x
            ${getExportVaultAddress()}
            vault kv put -format=json ${key} ${flatParams}
        """
        String out = sh(returnStdout: true, script: cmd).trim()
        def jsonOut = parseJson(out)
        
        if (jsonOut.data && jsonOut.data.version) {
            return true
        }

    } catch (ex) {
        println "[ERROR] Unable to put secret with key=${key}"
        println ex
    }
    
    return false
}

def parseJson(jsonString) {
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