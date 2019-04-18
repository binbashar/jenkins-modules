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
// **Modules:** Slack & AWS Route53 DNS helper modules.
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