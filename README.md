<div align="center">
    <img src="figures/binbash.png" alt="drawing" width="350"/>
</div>
<div align="right">
  <img src="figures/binbash-leverage-jenkins.png" alt="leverage" width="230"/>
</div>

# DevOps Jenkins Modules

### Devops Groovy Module files import
- Orig Ref @ https://github.com/jenkinsci/pipeline-examples/blob/master/pipeline-examples/load-from-file/pipeline.groovy

```groovy
#!/usr/bin/env groovy

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

- **Modules:** Slack & AWS SSM Parameter Store helper modules.

```groovy
#!/usr/bin/env groovy

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
```


- **Module:** AWS ECR helper module.

```groovy
#!/usr/bin/env groovy

node {
    String gitJenkinsModName = 'jenkins_modules'
    String gitJenkinsModCredentialsId = 'jenkins_id_rsa'
    String gitJenkinsModVersionTag = 'v0.0.1'
    String gitJenkinsModRepoUrl = 'git@github.com:project/jenkins-modules.git'
    
    def ecrHelper
    
    stage ("Check-out Libraries") {
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
        
        ecrHelper = load "${gitJenkinsModName}/aws/ecr/images.groovy"
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

- **Modules:** Slack & AWS Route53 DNS helper modules.

```groovy
#!/usr/bin/env groovy

node {    
    String gitJenkinsModName = 'jenkins_modules'
    String gitJenkinsModCredentialsId = 'jenkins_id_rsa'
    String gitJenkinsModVersionTag = 'v0.0.1'
    String gitJenkinsModRepoUrl = 'git@github.com:project/jenkins-modules.git'
    
    def slackHelper
    def route53Helper
    
    String appName = params.appName
    String branchName = params.branchName
    String stackName = params.stackName
    String baseDomainName = params.baseDomainName
    
    stage ("Check-out Libraries") {
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
        route53Helper = load "${gitJenkinsModName}/aws/route53/hosted-zones.groovy"
    }
    
    stage ("Delete DNS") {
        String hostedZoneId = "Z3OKADJT40HC77"
        String domainName = "${stackName}.${baseDomainName}."
        String aliasHostedZoneId = "Z3ALOTR6KTTL2"
        String aliasDnsName = "internal-kube-ing-lb-asddjrsbs-12345583.us-east-1.elb.amazonaws.com."
        
        // Check if the record set already exists
        if (route53Helper.hasRecordSet(hostedZoneId, domainName)) {
            // Delete the record set on route 53
            def deleteResult = route53Helper.deleteAliasRecordSet(hostedZoneId, domainName, aliasHostedZoneId, aliasDnsName)
            println deleteResult
            
            // Let's give it some time for the record to be deleted
            sleep 5
        } else {
            println "[INFO] Record set does not exist with domainName=${domainName}, hostedZoneId=${hostedZoneId}"
        }
    }
    
    stage ("Notify") {
        if (params.enableNotifications) {
            String msg = ":white_check_mark: Push Button Environments \n"
            msg += "A DEV environment has been *deleted* for app: `${appName}` using branch: `${branchName}` "
            slackHelper.send(msg, "#A9D071")
        }
    }
}
```

- **Module:** AWS SQS queues helper module.

```groovy
#!/usr/bin/env groovy

node {

    String gitJenkinsModName = 'jenkins_modules'
    String gitJenkinsModCredentialsId = 'jenkins_id_rsa'
    String gitJenkinsModVersionTag = 'v0.0.1'
    String gitJenkinsModRepoUrl = 'git@github.com:project/jenkins-modules.git'
    
    String appName = "wordpress"
    String envName = "dev"
    String stackName = "bi"
    String queueName = "${appName}-${envName}-${stackName}"
    String awsJenkinsRole = "devstg-jenkins-role"
    
    def sqsHelper

    stage ("Check-out Libraries") {
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
        
        sqsHelper = load "${gitJenkinsModName}/aws/sqs/queues.groovy"
    }
    
    stage ("Delete Queue") {
        String queueUrl = sqsHelper.getQueue(queueName, awsJenkinsRole)
        
        if (queueUrl) {
            println "[INFO] Queue was found with queueName=${queueName}, queueUrl=${queueUrl}"
            def deleteResult = sqsHelper.deleteQueue(queueUrl, awsJenkinsRole)
            println "[INFO] Delete queue returned with result=${deleteResult}"
        } else {
            println "[INFO] Queue was not found with queueName=${queueName}"
        }
    }
}
```

- **Module:** AWS SSM Parameter Store, Hashicorp Vault Authentication and Vault Key Value modules.

```groovy
#!/usr/bin/env groovy

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

            parameterStoreHelper = load "jenkins-modules/aws/ssm/parameter-store.groovy"
            vaultAuth = load "jenkins-modules/hashicorp/vault/auth.groovy"
            vaultKv = load "jenkins-modules/hashicorp/vault/kv.groovy"
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
```

- **Module:** K8s Manifest Helper, Hashicorp Vault Authentication and Vault Key Value modules.

```groovy
#!/usr/bin/env groovy

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

        vaultAuth = load "jenkins-modules/hashicorp/vault/auth.groovy"
        vaultKv = load "jenkins-modules/hashicorp/vault/kv.groovy"
        manifestHelper = load "jenkins-modules/k8s/manifest.groovy"
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
```