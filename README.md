<div align="center">
    <img src="figures/binbash.png" alt="drawing" width="350"/>
</div>
<div align="right">
  <img src="figures/binbash-leverage-jenkins.png" alt="leverage" width="230"/>
</div>

# DevOps Jenkins Modules

### Devops Groovy Module files import
- Orig Ref @ https://github.com/jenkinsci/pipeline-examples/blob/master/pipeline-examples/load-from-file/pipeline.groovy
```
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
```


### USE CASE EXAMPLES

- Modules: Slack & AWS SSM Parameter Store helper modules.

```
node {
    gitJenkinsModName = 'jenkins_modules'
    gitJenkinsModCredentialsId = 'jenkins_id_rsa'
    gitJenkinsModRepoUrl = 'bitbucket.org-access-keys:project/devops-jenkins-modules.git'
    gitJenkinsModVersionTag = 'v0.0.1'

    stage("Checkout ${gitJenkinsModName} Repo code") {
        dir("${gitJenkinsModName}") {
            checkout([
                $class: "GitSCM",
                branches: [[ name: gitJenkinsModVersionTag ]],
                userRemoteConfigs: [[
                    credentialsId: jenkinsCredentialId,
                    url: gitJenkinsModRepoUrl
                ]]
            ])
        }
        
        def rootDir = pwd()
        jenkinsModulesPath = "${rootDir}/jenkins_modules"
        slackHelper = load "${jenkinsModulesPath}/slack/notification.groovy"
        parameterStoreHelper = load "${jenkinsModulesPath}/aws/ssm/parameter-store.groovy"
    }

    stage("Call slackHelper module to Notify Start exec"){
        slackHelper.sendBuildStatus('STARTED')

    }

    stage ("Get Vault Unseal Keys") {
        def allParams = parameterStoreHelper.getParameters("/devops/vault/")
        // https://jenkins.io/doc/pipeline/steps/credentials-binding/
        withCredentials([string(credentialsId: "jenkins-vault-unseal-fake-credentials", variable: "jenkinsVaultUnsealFakeCredentials")]) {
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
```


- Module: AWS ECR helper module.

```
#!/usr/bin/env groovy

node {
    gitJenkinsModName = 'jenkins_modules'
    gitJenkinsModCredentialsId = 'jenkins_id_rsa'
    gitJenkinsModVersionTag = 'v0.0.1'
    gitJenkinsModRepoUrl = 'git@github.com:project/jenkins-modules.git'
    
    String jenkinsCredentialId = "jenkins-master-ssh-credentials"
    def ecrHelper
    
    stage ("Check-out Libraries") {
        dir("${gitJenkinsModName}") {
            checkout([
                $class: "GitSCM",
                branches: [[ name: gitJenkinsModVersionTag ]],
                userRemoteConfigs: [[
                    credentialsId: jenkinsCredentialId,
                    url: gitJenkinsModRepoUrl
                ]]
            ])
        }
        
        ecrHelper = load "jenkins-modules/aws/ecr/images.groovy"
    }
    
    stage ("Find/remove Image") {
        // List all images with matching prefix
        String repositoryName = params.repositoryName
        String imagePrefix = params.imagePrefix
        
        imagesList = ecrHelper.getImagesByPrefix(repositoryName, imagePrefix)
        
        // Remove any matching images
        println "[INFO] Found " + imagesList.size() + " images to delete"
        if (imagesList.size() == 0) {
            println "[INFO] No images to delete from repository=${repositoryName} with prefix=${imagePrefix}"
        } else {
            def deleteResult = ecrHelper.deleteImages(repositoryName, imagesList)
            println deleteResult
        }
    }
}
```