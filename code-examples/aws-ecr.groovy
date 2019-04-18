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
// **Module:** AWS ECR helper module.
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

        ecrHelper = load "load ${gitJenkinsModName}/aws/ecr/images.groovy"
    }

    stage ("Find/remove Image") {
        // List all images with matching prefix
        String repositoryName = params.repositoryName
        String imagePrefix = params.imagePrefix

        ArrayList imagesList = ecrHelper.getImagesByPrefix(repositoryName, imagePrefix)

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