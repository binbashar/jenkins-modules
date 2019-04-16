#! /usr/bin/groovy
import groovy.json.JsonSlurper

/*
 ** Jenkins Modules:
 * Vault Key/Value operations helper.
 *
 ** IMPORTANT:
 * A) This module relies heavily on the Hashicorp Vault CLI with Key/Value (KV) Secrets Engine Ver1/Ver2 enabled to run.
 * A Vault Secrets Engine is responsible for managing secrets. Simple secrets engines like the "kv" secrets engine
 * (the only one supported by this momdule) simply return the same secret when queried.
 *
 * NOTE1: When running v2 of the kv backend a key can retain a configurable number of versions.
 * Ref Link 1: https://www.vaultproject.io/docs/secrets/kv/index.html
 * Ref Link 2: https://www.vaultproject.io/docs/secrets/kv/kv-v2.html
 *
 * NOTE2: We DON'T currently support other SE like AWS, Google Cloud, Databases, TOTP, transit, among others.
 * Ref linK: https://www.vaultproject.io/docs/secrets/index.html
 *
 *
 * B) This module has to be load as shown in the root context README.md
 */

/*
 * This variable needs to be set once if your vault address is different.
 * This is a workaround to avoid having to pass this to every function.
 * It will be automatically exported as OS ENV var via getExportVaultAddress() function
 * declared at the end of this module.
 */
String vaultAddress = 'https://127.0.0.1:8200'

/**
 ** Function:
 * List secrets at the given key value path. The list command lists data from Vault at the given path.
 * This can be used to list keys and it's secrets in a given secret engine (currently only Key/Value SE is supported).
 *
 * Vault CLI KV Ver1 Example:
 * List the keys and values under the KV secrets engine:
 *      $ vault kv list kv/
 *      Keys
 *      ----
 *      my-secret
 *
 ** Parameters:
 * @param String keyPath    Vault key path, eg: 'secret/app/dev'
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

/**
 ** Function:
 * Get all entries for the given key. This assumes that you have a single secret
 * with one or multiple entries in it, so a single get operation is needed to
 * retrieve all of those entries. The alternative would be having a single entry
 * per secret, so you would have to list all of them first, and then retrieve
 * them one by one which is more expensive in terms of time/networking.
 *
 * Vault CLI KV Ver2 Example:
 *      $ vault kv get secret/my-secret
 *      ====== Metadata ======
 *      Key              Value
 *      ---              -----
 *      created_time     2018-03-30T22:11:48.589157362Z
 *      deletion_time    n/a
 *      destroyed        false
 *      version          1
 *
 *      ====== Data ======
 *      Key         Value
 *      ---         -----
 *      my-value    s3cr3t
 *
 ** Parameters:
 * @param String key    Vault key path, eg: 'secret/app/dev'
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

/**
 * Function:
 * Put all given entries (one or many) into the single secret identified by the
 * given key.
 *
 ** IMPORTANT:
 * After the secrets engine is configured and a user/machine has a Vault token with the proper permission,
 * it can generate credentials. The kv secrets engine allows for writing keys with arbitrary values.
 *
 * Key names must always be strings. If you write non-string values directly via the CLI,
 * they will be converted into strings. However, you can preserve non-string values by
 * writing the key/value pairs to Vault from a JSON file or using the HTTP API.
 *
 * Vault CLI KV Ver2 Example:
 *      $ vault kv put secret/my-secret my-value=newer-s3cr3t
 *      Key              Value
 *      ---              -----
 *      created_time     2018-03-30T22:41:09.193643571Z
 *      deletion_time    n/a
 *      destroyed        false
 *      version          3
 *
 ** Parameters:
 * @param   String key  Vault key path, eg: 'secret/app/dev'
 * @param   entriesMap  Groovy Map, eg: [db_user:'readonly', db_pass:'s3cr3t']
 */
def put(String key, def entriesMap) {
    if (entriesMap.size() <= 0)
        return false

    String flatParams = ""
    // eg: entriesMap == [db_user:'readonly', db_pass:'s3cr3t']
    entriesMap.each { itemName, itemValue ->
        // eg: flatParams == db_user='readonly' db_pass='s3cr3t'
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

/**
 ** Function:
 * Parse the given JSON encoded string. It uses Jenkins' readJSON utility which is so much better
 * than Groovy's JSONSluper.
 *
 * IMPORTANT:
 * Reads a file in the current working directory or a String as a plain text JSON file.
 * The returned object is a normal Map with String keys or a List of primitives or Map.
 *
 *Ref Link: https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readjson-read-json-from-files-in-the-workspace
 *
 ** Parameters:
 * @param String jsonString    A string containing the JSON formatted data. Data could be access as an array or a map.
 *
 * @return Groovy Map decodedJson
 */
def parseJson(String jsonString) {
    def decodedJson = null
    try {
        // Using JsonSlurper here because 'readJson' outputs the string being parsed
        decodedJson = new JsonSlurper().parseText(jsonString)
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