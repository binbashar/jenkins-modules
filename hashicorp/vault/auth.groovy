#!/usr/bin/env groovy

import groovy.json.JsonSlurper
/*
 ** Jenkins Modules:
 * Hashicorp Vault Auth helper.
 * Vault Auth Method - An auth method is used to authenticate users or applications which are connecting to Vault.
 * Once authenticated, the auth method returns the list of applicable policies which should be applied.
 * Vault takes an authenticated user and returns a client token that can be used for future requests.
 * As an example, the userpass auth method uses a username and password to authenticate the user.
 * Alternatively, the github auth method allows users to authenticate via GitHub.
 *
 ** IMPORTANT:
 * This module relies heavily on the vault CLI to run.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
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
 * Log in to Hashicorp vault with the given token.
 *
 * IMPORTANT: only Github auth is supported, so your Github Personal Access
 * Token should be passed to this.
 *
 ** Parameters:
 * @param String token      Github Personal Access Token. Client Token - A client token (aka "Vault Token") is a
 *                          conceptually similar to a session cookie on a web site. Once a user authenticates,
 *                          Vault returns a client token which is used for future requests. The token is used by Vault
 *                          to verify the identity of the client and to enforce the applicable ACL policies.
 *                          This token is passed via HTTP headers.
 *                          ref-link: (https://help.github.com/en/articles/creating-a-personal-access-token-for-the-command-line)
 *
 * @return Boolean          If vault login method (Githib personal access token) has successfully authenticated return
 *                          true, if failed for any reason return false.
 *
 ** Example:
 *  // Set vault address for subsequent method calls
 *  vaultAuth.vaultAddress = vaultAddress
 *
 *  if (!vaultAuth.isLoggedIn()) {
 *      withCredentials([string(credentialsId: "jenkins-github-personal-access-token", variable: "jenkinsGithubToken")]) {
 *          if (vaultAuth.login(jenkinsGithubToken)) {
 *              println "[INFO] Successfully logged in to Vault"
 *          } else {
 *              println "[ERROR] Unable to log in to Vault"
 *              sh "exit 1"
 *          }
 *      }
 *  } else {
 *      println "[INFO] Already logged in to Vault"
 *  }
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
        
        // Validate if vault login method has successfully authenticated
        if (jsonOut.auth && jsonOut.auth.client_token)
            return true
        
    } catch (ex) {
        println "[ERROR] Unable to login with given token"
        println ex
    }
    
    return false
}

/**
 ** Function:
 * Check if you are already logged in -- which means your token is still valid (currently Github token).
 *
 ** Parameters:
 * @return Boolean  If vault login method has successfully authenticated return true, else false.
 *
 ** Example:
 *  // Set vault address for subsequent method calls
 *  vaultAuth.vaultAddress = vaultAddress
 *
 *  if (!vaultAuth.isLoggedIn()) {
 *      withCredentials([string(credentialsId: "jenkins-github-personal-access-token", variable: "jenkinsGithubToken")]) {
 *          if (vaultAuth.login(jenkinsGithubToken)) {
 *              println "[INFO] Successfully logged in to Vault"
 *          } else {
 *              println "[ERROR] Unable to log in to Vault"
 *              sh "exit 1"
 *          }
 *      }
 *  } else {
 *      println "[INFO] Already logged in to Vault"
 *  }
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

        // Validate if vault login method has successfully authenticated
        if (jsonOut.data && jsonOut.data.id)
            return true
        
    } catch (ex) {
        // Vault CLI will return an exit code of 2 when a token is not found in
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
 * IMPORTANT:
 * Reads a file in the current working directory or a String as a plain text JSON file.
 * The returned object is a normal Map with String keys or a List of primitives or Map.
 *
 *Ref Link: https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readjson-read-json-from-files-in-the-workspace
 *
 ** Parameters:
 * @param String jsonString    A string containing the JSON formatted data. Data could be access as an array or a map.
 *
 * @return LinkedHashMap decodedJson
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

/**
 ** Function:
 * Will export String vaultAddress = 'https://127.0.0.1:8200' variable. Since this var needs to be set once if your
 * vault address is different. This is a workaround to avoid having to pass this to every function.
 *
 ** Parameters:
 * @return Exec export OS cmd.
 */
def getExportVaultAddress() {
    return "export VAULT_ADDR=${vaultAddress}"
}

// Note: this line is crucial when you want to load an external groovy script
return this