#!/usr/bin/env groovy

/////////////////////////////
/*node {
    // Load the file 'externalMethod.groovy' from the current directory, into a variable called "externalMethod".
    def externalMethod = load("externalMethod.groovy")

    // Call the method we defined in externalMethod.
    externalMethod.lookAtThis("Steve")

    // Now load 'externalCall.groovy'.
    def externalCall = load("externalCall.groovy")

    // We can just run it with "externalCall(...)" since it has a call method.
    externalCall("Steve")
}*/
/////////////////////////////

// USE CASE EXAMPLE
// **Module:** AWS SSM Parameter Store, Hashicorp Vault Authentication and Vault Key Value modules.
node {

    String gitJenkinsModName = 'jenkins_modules'
    String gitJenkinsModCredentialsId = 'jenkins_id_rsa'
    String gitJenkinsModVersionTag = 'v0.0.1'
    String gitJenkinsModRepoUrl = 'git@github.com:project/jenkins-modules.git'

    String vaultAddress = 'http://0.0.0.0:8200'

    def allParams

    def parameterStoreHelper
    def vaultAuth
    def vaultKv

    stage("Check-out Libraries") {
        dir("jenkins-modules") {
            dir("${gitJenkinsModName}") {
                checkout([
                        $class           : "GitSCM",
                        branches         : [[name: gitJenkinsModVersionTag]],
                        userRemoteConfigs: [[
                            credentialsId: gitJenkinsModCredentialsId,
                            url: gitJenkinsModRepoUrl
                        ]]
                ])
            }

            parameterStoreHelper = load "${gitJenkinsModName}/aws/ssm/parameter-store.groovy"
            vaultAuth = load "${gitJenkinsModName}/hashicorp/vault/auth.groovy"
            vaultKv = load "${gitJenkinsModName}/hashicorp/vault/kv.groovy"
        }

        stage("Parameter Store Get") {
            // Fetch all parameters names/values of this app/env from SSM
            allParams = parameterStoreHelper.getParameters("/nwbe/${params.envName}/")
            println allParams.size()
        }

        stage("Write to Vault") {
            // Set vault address for subsequent method calls
            vaultAuth.vaultAddress = vaultAddress

            if (!vaultAuth.isLoggedIn()) {
                withCredentials([string(credentialsId: "jenkins-github-personal-access-token", variable: "jenkinsGithubToken")]) {
                    if (vaultAuth.login(jenkinsGithubToken)) {
                        println "[INFO] Successfully logged in to Vault"
                    } else {
                        println "[ERROR] Unable to log in to Vault"
                        sh "exit 1"
                    }
                }
            } else {
                println "[INFO] Already logged in to Vault"
            }

            if (vaultKv.put("secret/nwbe/${params.envName}", allParams)) {
                println "[INFO] Parameters written to Vault"
            } else {
                println "[ERROR] Unable to write parameters to Vault"
            }
        }

        stage("Vault Read") {
            def afterParams = vaultKv.get("secret/nwbe/${params.envName}")
            println afterParams.size()
        }
    }
}