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
// **Module:** K8s kubectl Helper module.
node {
    String gitJenkinsModName = 'jenkins_modules'
    String gitJenkinsModCredentialsId = 'jenkins_id_rsa'
    String gitJenkinsModVersionTag = 'v0.0.1'
    String gitJenkinsModRepoUrl = 'git@github.com:project/jenkins-modules.git'

    String k8sContext = params.k8sContext
    String appName = params.appName
    String envName = params.envName
    String stackName = params.stackName
    String podNamePrefix = "${appName}-api"
    String namespace = "${appName}-${envName}-${stackName}"

    def kubectlHelper

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

        kubectlHelper = load "${gitJenkinsModName}/k8s/kubectl.groovy"
    }

    stage ("Run Migrations") {
        String podRequiredStatus = 'Running'
        int waitTimeout = 30

        try {
            def pod = kubectlHelper.waitForPod(podNamePrefix, namespace, podRequiredStatus, waitTimeout, k8sContext)

            String migrationsOutput = kubectlHelper.exec(pod.id, "python manage.py migrate", namespace, "-it", k8sContext)
            println migrationsOutput

        } catch (e) {
            println "[WARNING] migrations could not be run as the pod did not become ready. Hint: ensure podNamePrefix is correct."
            println "[WARNING] Exception: ${e}"
            currentBuild.result = 'FAILURE'
        }
    }
}