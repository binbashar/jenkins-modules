#!/usr/bin/env groovy
/*
 * 
 * A) Sample usage from a Pipeline Stage (you must include the function)
 *
 *  node {
 *      stage('Vault Secrets Sample') {
 *          def secrets = getSecrets("secret/dev-mysql", ["mysql-database", "mysql-user"])
 *          print "mysql-database: " + secrets['mysql-database']
 *          print "mysql-user: " + secrets['mysql-user']
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   MY_VAULT_SCRIPT = load "jenkins_pipeline-secrets_get.groovy"
 *   def secrets = MY_VAULT_SCRIPT.getSecrets("secret/dev-mysql", ["mysql-database", "mysql-user"])
 *   print "mysql-database: " + secrets['mysql-database']
 *   print "mysql-user: " + secrets['mysql-user']
 *
 * NOTE: One very important point about this function that I forgot to mention: the function uses a mechanism to read
 * variables from 'env' object that is not enabled by default on Jenkins. The issue is explained
 * here: https://stackoverflow.com/questions/38276341/jenkins-ci-pipeline-scripts-not-permitted-to-use-method-groovy-lang-groovyobject
 * Basically, Jenkins will fail when running that function unless the pipeline is not run in a sandbox or until an
 * Admin approves the kind of static access the function needs to do.
 *
 * /

/*
 * This function returns the list of fields defined in fieldsList that are
 * stored in the vault identified by the given vaultId.
 */
def getSecrets(String vaultId, def fieldsList) {
    String envVarPrefix = "VAR_"
    def vaultSecretsConfig = [[
        $class: 'VaultSecret',
        path: vaultId,
        secretValues: []
    ]]
    
    // Build the config expected by vault plugin using the list of secrets provided
    for (field in fieldsList) {
        String envField = field.replace("-", "_").toUpperCase()
        vaultSecretsConfig[0]['secretValues'].add([
            $class: 'VaultSecretValue',
            envVar: "${envVarPrefix}${envField}",
            vaultKey: field
        ])
    }
    
    // Read the list of secrets from the vault
    def secretsBag = [:]
    wrap([$class: 'VaultBuildWrapper', vaultSecrets: vaultSecretsConfig]) {
        for (field in fieldsList) {
            String envField = field.replace("-", "_").toUpperCase()
            secretsBag[field] = env["${envVarPrefix}${envField}"]
        }
    }
    
    return secretsBag
}

// Note: this line is crucial when you want to load an external groovy script
return this