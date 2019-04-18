#!/usr/bin/env groovy

/////////////////////////////
/*
node {
    // Load the file 'externalMethod.groovy' from the current directory, into a variable called "externalMethod".
    def externalMethod = load("externalMethod.groovy")

    // Call the method we defined in externalMethod.
    externalMethod.lookAtThis("Steve")

    // Now load 'externalCall.groovy'.
    def externalCall = load("externalCall.groovy")

    // We can just run it with "externalCall(...)" since it has a call method.
    externalCall("Steve")
}
*/
/////////////////////////////

// USE CASE EXAMPLE
// **Modules:** Slack & AWS SSM Parameter Store helper modules.
node {
    String gitJenkinsModName = 'jenkins_modules'
    String gitJenkinsModCredentialsId = 'jenkins_id_rsa'
    String gitJenkinsModRepoUrl = 'bitbucket.org-access-keys:project/devops-jenkins-modules.git'
    String gitJenkinsModVersionTag = 'v0.0.1'

    def slackHelper
    def parameterStoreHelper

    stage("Checkout ${gitJenkinsModName} Repo code") {
        dir("${gitJenkinsModName}") {
            checkout([
                    $class: "GitSCM",
                    branches: [[ name: gitJenkinsModVersionTag ]],
                    userRemoteConfigs: [[
                        credentialsId: gitJenkinsModCredentialsId,
                        url: gitJenkinsModRepoUrl
                    ]]
            ])
        }

        slackHelper = load "${gitJenkinsModName}/slack/notification.groovy"
        parameterStoreHelper = load "${gitJenkinsModName}/aws/ssm/parameter-store.groovy"
    }

    stage("Call slackHelper module to Notify Start exec"){
        slackHelper.sendBuildStatus('STARTED')

    }

    stage ("Get Vault Unseal Keys") {
        def allParams = parameterStoreHelper.getParameters("/devops/vault/")
        withCredentials([string(credentialsId: "jenkins-vault-unseal-fake-credentials",
                variable: "jenkinsVaultUnsealFakeCredentials")]) {
            sh """
                set +x
                export VAULT_ADDR='http://0.0.0.0:8200'
                vault operator unseal ${allParams['unseal_key_1']}
                vault operator unseal ${allParams['unseal_key_2']}
                vault operator unseal ${allParams['unseal_key_3']}
               
                # This next line is only to force Jenkins to hide all commands,
                # their outputs should be covered by the +x
                #echo ${jenkinsVaultUnsealFakeCredentials} > /dev/null
            """
        }
    }

    stage("Call slackHelper module to Notify Successful exec"){
        slackHelper.sendBuildStatus('SUCCESS')
    }
}