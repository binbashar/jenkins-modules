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
// **Module:** K8s Manifest Helper, Hashicorp Vault Authentication and Vault Key Value modules.
node {

    // Jenkins Modules git checkout variables
    String gitJenkinsModName = 'jenkins_modules'
    String gitJenkinsModCredentialsId = 'jenkins_id_rsa'
    String gitJenkinsModVersionTag = 'v0.0.1'
    String gitJenkinsModRepoUrl = 'git@github.com:project/jenkins-modules.git'

    // Load modules associated variables
    def vaultAuth
    def vaultKv
    def manifestHelper

    // App related imput paramters
    String appName = params.appName
    String envName = params.envName

    // Hashicorp Vault Server URL
    String vaultAddress = 'http://0.0.0.0:8200'

    // K8s secret manifests .yml variables
    def manifestName = "${appName}-${envName}-secrets"
    def manifestFile = "${manifestName}.yml"
    def secretManifest

    stage ("Check-out Libraries") {
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

        vaultAuth = load "${gitJenkinsModName}/hashicorp/vault/auth.groovy"
        vaultKv = load "${gitJenkinsModName}/hashicorp/vault/kv.groovy"
        manifestHelper = load "${gitJenkinsModName}/k8s/manifest.groovy"
    }

    stage ("Configure Application") {
        // Set vault address for subsequent method calls
        vaultAuth.vaultAddress = vaultAddress

        // Check if we are logged in to Vault, otherwise try and log in
        if (! vaultAuth.isLoggedIn()) {
            withCredentials([string(credentialsId: "jenkins-github-personal-access-token", variable: "jenkinsGithubToken")]) {
                if (vaultAuth.login(jenkinsGithubToken)) {
                    println "[INFO] Succesfully logged in to Vault"
                } else {
                    println "[ERROR] Unable to log in to Vault"
                    sh "exit 1"
                }
            }
        }

        // Fetch all parameters names/values of this app/env from Vault
        def allParams = vaultKv.get("secret/${appName}/${envName}")

        secretManifest = manifestHelper.buildSecret("${manifestName}", "${appName}", allParams)
    }

    stage ("Update Secrets") {
        writeFile file: "${manifestFile}", text: "${secretManifest}"
        sh "kubectl apply -f ${manifestFile} --context ${params.k8sCluster}"
    }

    stage ("Clean Up") {
        sh "rm ${manifestFile}"
    }
}