#!/usr/bin/env groovy

/*
 ** Jenkins Modules:
 * Vault Key/Value get secrets helper module.
 *
 ** IMPORTANT:
 ** This module relies heavily on the HashiCorp Vault Plugin
 * Ref Link: https://wiki.jenkins.io/display/JENKINS/HashiCorp+Vault+Plugin
 *
 * Plugin official usage Example:
 * node {
 * // define the secrets and the env variables
 * def vaultSecretsDemo = [
 *     [$class: 'VaultSecret', path: 'secret/testing', secretValues: [
 *         [$class: 'VaultSecretValue', envVar: 'testingDbUser', vaultKey: 'db_user'],
 *         [$class: 'VaultSecretValue', envVar: 'testingDbPass', vaultKey: 'db_pass']]],
 *     
 *     [$class: 'VaultSecret', path: 'secret/staging', secretValues: [
 *         [$class: 'VaultSecretValue', envVar: 'stagingToken', vaultKey: 'github_token']]]
 * ]
 *
 * // optional configuration, if you do not provide this the next higher configuration
 * // (e.g. folder or global Jenkins configs) will be used.
 * def configuration = [$class: 'VaultConfiguration',
 *                      vaultUrl: 'http://my-very-other-vault-url.com',
 *                      vaultCredentialId: 'my-vault-cred-id']
 *
 * // inside this block your credentials will be available as env variables
 * wrap([$class: 'VaultBuildWrapper', configuration: configuration, vaultSecrets: vaultSecretsDemo]) {
 *     sh 'echo $testingDbUser'
 *     sh 'echo $testingDbPass'
 *     sh 'echo $stagingToken'
 *  }
 *}
 *
 * NOTE: One very important point about this function: the function uses a mechanism to read
 * variables from 'env' object that is not enabled by default on Jenkins. The issue is explained
 * here: https://stackoverflow.com/questions/38276341/jenkins-ci-pipeline-scripts-not-permitted-to-use-method-groovy-lang-groovyobject
 * Basically, Jenkins will fail when running that function unless the pipeline is not run in a sandbox or until an
 * Admin approves the kind of static access the function needs to do.
 *
 ** This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */

/**
 * Function:
 * This function returns the list of fields defined in fieldsList that are
 * stored in the vault identified by the given vaultId (vault key path).
 *
 ** Parameters:
 * @param String    vaultId             Vault path key, eg: 'secret/dev-mysql'
 * @param ArrayList fieldsList          Groovy ArrayList of secrets provided, eg: ["mysql-database", "mysql-user", "mysql-pass"]
 *
 * @return LinkedHashMap secretsBag     Contains the secrets gotten from vault,
 *                                      eg: [mysql-database:"db_name", mysql-user:"readonly", mysql-pass:"s3cr3t"]
 *
 ** Examples
 * A) Sample usage from a Pipeline Stage (you must include the function in the same groovy script)
 *
 *  node {
 *      stage('Vault Secrets Sample') {
 *          LinkedHashMap secrets = [:]
 *          secrets = getSecrets("secret/dev-mysql", ["mysql-database", "mysql-user", "mysql-pass"])
 *          print "mysql-database: " + secrets['mysql-database']
 *          print "mysql-user: " + secrets['mysql-user']
 *          print "mysql-pass: " + secrets['mysql-pass']
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   MY_VAULT_SCRIPT = load "jenkins_pipeline-secrets_get.groovy"
 *   LinkedHashMap secrets = [:]
 *   secrets = MY_VAULT_SCRIPT.getSecrets("secret/dev-mysql", ["mysql-database", "mysql-user", "mysql-pass"])
 *   print "mysql-database: " + secrets['mysql-database']
 *   print "mysql-user: " + secrets['mysql-user']
 *   print "mysql-pass: " + secrets['mysql-pass']
 */
def getSecrets(String vaultId, ArrayList fieldsList) {
    String envVarPrefix = "VAR_"
    def vaultSecretsConfig = [[
        $class: 'VaultSecret',
        path: vaultId,
        secretValues: []
    ]]
    
    // Build the config expected by vault plugin using the list of secret names provided in order to retrieve each
    // VaultSecretValue declared in our fieldsList and set an envVar for each one of them (check the example at the
    // beginning of this module for more info.
    // NOTE: if we pass the secret 'mysql-database' to be looked up, we'll get the envVar with the name:
    // VAR_MYSQL_DATABASE (this it's just a convention we've chosen for the hashicorp plugin intermediari env vars
    // and hence the code developed in this function).
    for (field in fieldsList) {
        String envField = field.replace("-", "_").toUpperCase()
        vaultSecretsConfig[0]['secretValues'].add([
            $class: 'VaultSecretValue',
            envVar: "${envVarPrefix}${envField}",
            vaultKey: field
        ])
    }
    
    // Read the list of secrets via plugin from the configured vaultSecretsConfig vault and making use of the
    // VaultBuildWrapper class fill a LinkedHashMap to be returned
    LinkedHashMap secretsBag = [:]
    wrap([$class: 'VaultBuildWrapper', vaultSecrets: vaultSecretsConfig]) {
        for (field in fieldsList) {
            String envField = field.replace("-", "_").toUpperCase()
            // iteration eg: secretBag[mysql-database] = env[VAR_MYSQL_DATABASE]
            secretsBag[field] = env["${envVarPrefix}${envField}"]
        }
    }
    
    return secretsBag
}

// Note: this line is crucial when you want to load an external groovy script
return this