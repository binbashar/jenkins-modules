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
// **Module:** Git Helper and Slack Helper modules.
node {

    // Jenkins Modules git checkout variables
    String gitJenkinsModName = 'jenkins_modules'
    String gitJenkinsModCredentialsId = 'jenkins_id_rsa'
    String gitJenkinsModVersionTag = 'v0.0.1'
    String gitJenkinsModRepoUrl = 'git@github.com:project/jenkins-modules.git'

    // Load modules associated variables
    def slackHelper
    def gitHelper

    // Examples related variables
    String gitCommitId
    String releaseTagPrefix = "release"
    String lastChangesDiffMsg = ''

    try {
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

            slackHelper = load "${gitJenkinsModName}/slack/notification.groovy"
            gitHelper = load "${gitJenkinsModName}/scm/git.groovy"
        }

        stage ("Notify Start") {
            if (params.enableNotifications)
                slackHelper.sendBuildStatus('STARTED')
        }

        stage ("Notify Last Changes") {
            if (params.tagRelease) {
                String releaseTagFilter = releaseTagPrefix + "_*"
                String previousDeployCommitHash = gitHelper.getCommitHashByTag(releaseTagFilter)
                String currentDeployCommitHash = gitCommitId

                if (previousDeployCommitHash != '') {
                    lastChangesDiffMsg = gitHelper.getDiffMessages(previousDeployCommitHash, currentDeployCommitHash)
                } else {
                    lastChangesDiffMsg = 'No Changes Found'
                }

                // Replace problematic characters in diff message
                lastChangesDiffMsg = lastChangesDiffMsg.replace('"', '\'')

                println "lastChangesDiffMsg: ${lastChangesDiffMsg}"
                String previousTagName = gitHelper.getLastTagName(releaseTagFilter)
                String lastChangesUrl = gitHelper.getRepositoryUrl() + "/compare/${previousTagName}...${currentDeployCommitHash}"
                String lastChangesMsg = "Last Changes: <${lastChangesUrl}>  ```${lastChangesDiffMsg}```"

                if (params.enableNotifications) {
                    slackHelper.sendBuildStatus('LAST CHANGES', lastChangesMsg)
                } else {
                    println lastChangesMsg
                }
            }
        }

        stage ("Tag Release") {
            if (params.tagRelease) {
                String currentDeployCommitHash = gitCommitId
                String tagDate = sh(returnStdout: true, script: 'date +%Y-%m-%d-%H-%M-%S').trim()
                println "tagPrefix_date: ${releaseTagPrefix}_${tagDate}"
                println "currentDeployCommitHash: ${currentDeployCommitHash}"
                println "lastChangesDiffMsg: ${lastChangesDiffMsg}"

                sh "git tag -a ${releaseTagPrefix}_${tagDate} ${currentDeployCommitHash} -m \"${lastChangesDiffMsg}\""
                sh 'git push --tags'
            }
        }

        stage ("Notify Success") {
            if (params.enableNotifications)
                slackHelper.sendBuildStatus('SUCCESS')
        }

    } catch (e) {
        if (params.enableNotifications)
            slackHelper.sendBuildStatus('FAILURE')

        currentBuild.result = "FAILED"
        throw e
    }
}