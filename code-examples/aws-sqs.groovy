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
// **Module:** AWS SQS queues helper module.
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