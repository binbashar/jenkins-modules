#!/usr/bin/env groovy
import groovy.json.JsonSlurper
/*
 * Jenkins Modules: AWS Elasticbeanstalk helper.
 *
 * Important: this module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that. The module also uses 'jq' to parse JSON output.
 *
 * Important: at this moment this module can only handle values of type String
 * and SecureString.
 */

/*
 * This function returns a list with EB environments for an specific application.
 * Will look for environment names starting with this name because is the envinronments
 * names follow the same prefix.
 */
def getEnvList(appName,envName, awsProfile, awsRegion){
    envList = []
    cmd = "aws elasticbeanstalk describe-environments --application-name \"${appName}\" --profile ${awsProfile} --region ${awsRegion}"

    envDescriptionRaw = sh (
            script: "${cmd}",
            returnStdout: true
    ).trim()

    def parsedRaw = new JsonSlurper().parseText( envDescriptionRaw )
    def parsed = parsedRaw.Environments

    parsed.each{
        if (it.EnvironmentName.startsWith("${envName}")){
            if (it.HealthStatus != "Unknown" && it.Status != "Terminated"){
                envList.add(it.EnvironmentName)
            }
        }
    }
    return envList
}

/*
 * This function will return the current active environment
 * Note: to be improved
 */
def getCurrentEnv(envList){
    def retvar
    envList.any{
        retvar = "${it}"
        return true
    }
    return retvar
}

/*
 * This function will return the new envinronment name to be used
 * in a BlueGreen deployment based on the current env name.
 * If there is no -BLUE or -GREEN env in place will start with BLUE
 */
def getNextEnv(envList){
    def retvar
    envList.any{
        if (it.endsWith("-BLUE")){
            retvar = "${it.reverse().drop(5).reverse()}-GREEN"
            return true
        }

        if (it.endsWith("-GREEN")){
            retvar = "${it.reverse().drop(6).reverse()}-BLUE"
            return true
        }

        // Default in case is not using B/G yet
        retvar = "${it}-BLUE"
        return true
    }
    return retvar
}

/*
 * This function will return the internal endpoint of and envinronment.
 * Designed mostly to validate if an env is responding OK and swap when
 * using Blue/Green deployments
 */
def getEnvEndpoint(envName, awsProfile, awsRegion){
    def retvar = null
    cmd = "aws elasticbeanstalk describe-environments --environment-names ${envName} --profile ${awsProfile} --region ${awsRegion}"

    envDescriptionRaw = sh (
            script: "${cmd}",
            returnStdout: true
    ).trim()

    def parsed = new JsonSlurper().parseText( envDescriptionRaw )
    retvar = parsed.Environments['CNAME'][0]

    return retvar
}

/*
 * This function will return the envinronment health staus.
 * Designed mostly to validate if an env is OK and swap when
 * using Blue/Green deployments
 */
def getEnvHealthStatus(envName, awsProfile, awsRegion){
    cmd = "aws elasticbeanstalk describe-environments --environment-names ${envName} --profile ${awsProfile} --region ${awsRegion}"

    envDescriptionRaw = sh (
            script: "${cmd}",
            returnStdout: true
    ).trim()

    def parsed = new JsonSlurper().parseText( envDescriptionRaw )
    return parsed.Environments['HealthStatus'][0]
}

/*
 * This function will return the color you see on AWS GUI of and envinronment.
 * Designed mostly to validate if an env is responding OK and swap when
 * using Blue/Green deployments
 */
def getEnvColor(envName, awsProfile, awsRegion){
    cmd = "aws elasticbeanstalk describe-environments --environment-names ${envName} --profile ${awsProfile} --region ${awsRegion}"

    envDescriptionRaw = sh (
            script: "${cmd}",
            returnStdout: true
    ).trim()

    def parsed = new JsonSlurper().parseText( envDescriptionRaw )
    return parsed.Environments['Health'][0].toString().toUpperCase()
}

/*
 * This function will clone the current environment into a new one
 * and then will deploy the latest app version in the new one.
 * Expected result: keep two concurrent environments, the original untouched
 * and the new one with the latest app version + env vars according to the
 * app definition (based on config files)
 */
def deploy_BG_eb(sourceEnv,appPath,appEnvVars, targetEnv){
    dir("${appPath}"){
        stage("Clone ${sourceEnv} on ${targetEnv}"){
            sh "#!/bin/bash -e\n" +
                    "eb use ${sourceEnv}"

            sh "#!/bin/bash -e\n" +
                    "eb clone -n ${targetEnv} --exact"
        }

        stage("Deploying on: ${targetEnv}") {
            deploy_eb(targetEnv,appPath,appEnvVars)
        }
    }
}

/*
 * This function invokes EB command to swap CNAMEs between 2 environments.
 * Tipically used for Blue/Green deployments.
 */
def swap_BG_env_eb(sourceEnv, appPath, targetEnv){
    dir("${appPath}"){
        stage("Swap BG from ${sourceEnv} to ${targetEnv}"){
            sh "#!/bin/bash -e\n" +
                    "eb swap ${sourceEnv} --destination_name ${targetEnv}"
        }
    }
}

/*
 * This function invokes EB command to terminate an environment.
 * Tipically used for Blue/Green deployments.
 */
def terminate_BG_env_eb(envName,appPath){
    dir("${appPath}"){
        stage("Terminating ${envName}"){
            sh "#!/bin/bash -e\n" +
                    "eb terminate ${envName} --force --timeout 20"
        }
    }
}

/*
 * This function will deploy EB assuming the app code is already in appPath
 * and setting environment vars passed on appEnvVars
 */
def deploy_eb(envName,appPath,appEnvVars){

    dir("${appPath}"){
        sh "#!/bin/bash -e\n" +
            "eb use ${envName}"

        if (appEnvVars.size() > 0) {
            sh "#!/bin/bash -e\n" +
                "eb setenv " + appEnvVars
        }


        sh "#!/bin/bash -e\n" +
            "eb deploy ${envName} --timeout 15"
    }
}

return this
