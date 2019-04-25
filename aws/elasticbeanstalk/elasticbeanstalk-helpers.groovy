#!/usr/bin/env groovy

import groovy.json.JsonSlurper
/*
 ** Jenkins Modules:
 * AWS Elasticbeanstalk helper.
 * Ref link: https://aws.amazon.com/getting-started/tutorials/deploy-app-command-line-elastic-beanstalk/
 *
 ** IMPORTANT:
 * this module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that. The module also uses 'jq' to parse JSON output.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */

/**
 ** Function:
 * This function returns a list with EB environments for an specific application.
 * Will look for environment names starting with this name because is the environments
 * names follow the same prefix.
 *
 ** Parameters:
 * @param String appName        AWS ElasticBeanstalk application name.
 * @param String envName        AWS ElasticBeanstalk environment name.
 * @param String awsProfile     AWS IAM profile.
 * @param String awsRegion      AWS region.
 *
 * @retun ArrayList envList     Groovy ArrayList AWS ElasticBeanstalk environment name (different from Unknown and
 *                              Terminated states).
 */
def getEnvList(String appName, String envName, String awsProfile, String awsRegion){
    ArrayList envList = []
    cmd = "aws elasticbeanstalk describe-environments --application-name \"${appName}\" " +
            "--profile ${awsProfile} --region ${awsRegion}"

    String envDescriptionRaw = sh (
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

/**
 ** Function:
 * This function will return the current active environment
 * Note: to be improved
 *
 ** Parameters:
 * @param ArrayList envList     AWS ElasticBeanstalk environment list by name
 *
 * @return String retvar        1st AWS ElasticBeanstalk environment in the passed list
 */
def getCurrentEnv(ArrayList envList){
    String retvar
    envList.any{
        retvar = "${it}"
    }
    return retvar
}

/**
 ** Function:
 * This function will return the new environment name to be used
 * in a BlueGreen deployment based on the current env name.
 * If there is no -BLUE or -GREEN env in place will start with BLUE
 *
 ** Parameters:
 * @param ArrayList envList     AWS ElasticBeanstalk environment list by name
 *
 * @return String retvar        New AWS ElasticBeanstalk environment name based on the current env name.
 */
def getNextEnv(ArrayList envList){
    String retvar
    envList.any{
        // Default in case is not using B/G yet
        retvar = "${it}-BLUE"

        if (it.endsWith("-BLUE")){
            retvar = "${it.reverse().drop(5).reverse()}-GREEN"
        }

        if (it.endsWith("-GREEN")){
            retvar = "${it.reverse().drop(6).reverse()}-BLUE"
        }
    }
    return retvar
}

/**
 ** Function:
 * This function will return the internal endpoint of and environment.
 * Designed mostly to validate if an env is responding OK and swap when
 * using Blue/Green deployments
 *
 ** Parameters:
 * @param String envName    AWS ElasticBeanstalk environment name.
 * @param String awsProfile AWS IAM profile.
 * @param String awsRegion  AWS region.
 *
 * @return String retvar    AWS ElasticBeanstalk environment CNAME, The URL to the CNAME for this environment
 *                          eg: 'my-env.elasticbeanstalk.com'
 * Ref link: https://docs.aws.amazon.com/cli/latest/reference/elasticbeanstalk/describe-environments.html
 */
def getEnvEndpoint(String envName, String awsProfile, String awsRegion){
    String retvar
    cmd = "aws elasticbeanstalk describe-environments --environment-names ${envName}" +
            " --profile ${awsProfile} --region ${awsRegion}"

    String envDescriptionRaw = sh (
            script: "${cmd}",
            returnStdout: true
    ).trim()

    def parsed = new JsonSlurper().parseText( envDescriptionRaw )
    retvar = parsed.Environments['CNAME'][0].toString()

    return retvar
}

/**
 ** Function:
 * This function will return the environment health staus.
 * Designed mostly to validate if an env is OK and swap when
 * using Blue/Green deployments
 *
 ** Parameters:
 * @param String envName    AWS ElasticBeanstalk environment name.
 * @param String awsProfile AWS IAM profile.
 * @param String awsRegion  AWS region.
 *
 * @return String retvar    AWS ElasticBeanstalk HealthStatus. Returns the health status of the application running in
 *                          your environment. For more information, see Health Colors and Statuses .
 */
def getEnvHealthStatus(String envName, String awsProfile, String awsRegion){
    String retvar
    cmd = "aws elasticbeanstalk describe-environments --environment-names ${envName}" +
            " --profile ${awsProfile} --region ${awsRegion}"

    envDescriptionRaw = sh (
            script: "${cmd}",
            returnStdout: true
    ).trim()

    def parsed = new JsonSlurper().parseText( envDescriptionRaw )
    retvar = parsed.Environments['HealthStatus'][0].toString()
    return retvar
}

/**
 ** Function:
 * This function will return the color you see on AWS GUI of and environment.
 * Designed mostly to validate if an env is responding OK and swap when
 * using Blue/Green deployments
 *
 ** Parameters:
 * @param String envName    AWS ElasticBeanstalk environment name.
 * @param String awsProfile AWS IAM profile.
 * @param String awsRegion  AWS region.
 *
 * @return String retvar    AWS ElasticBeanstal Health: Describes the health status of the environment.
 *                          AWS Elastic Beanstalk indicates the failure levels for a running environment:
 *                          - Red : Indicates the environment is not responsive. Occurs when three or more consecutive
 *                            failures occur for an environment.
 *                          - Yellow : Indicates that something is wrong. Occurs when two consecutive failures occur for
 *                            an environment.
 *                          - Green : Indicates the environment is healthy and fully functional.
 *                          - Grey : Default health for a new environment. The environment is not fully launched and
 *                            health checks have not started or health checks are suspended during an UpdateEnvironment
 *                            or RestartEnvironment request.
 */
def getEnvColor(String envName, String awsProfile, String awsRegion){
    String retvar
    cmd = "aws elasticbeanstalk describe-environments --environment-names ${envName}" +
            " --profile ${awsProfile} --region ${awsRegion}"

    envDescriptionRaw = sh (
            script: "${cmd}",
            returnStdout: true
    ).trim()

    def parsed = new JsonSlurper().parseText( envDescriptionRaw )
    retvar = parsed.Environments['Health'][0].toString().toUpperCase()
    return retvar
}

/**
 ** Function:
 * This function will clone the current environment into a new one
 * and then will deploy the latest app version in the new one.
 * Expected result: keep two concurrent environments, the original untouched
 * and the new one with the latest app version + env vars according to the
 * app definition (based on config files)
 *
 ** Parameters:
 * @param String sourceEnv  Current AWS ElasticBeanstalk environment to be cloned.
 * @param String appPath    AWS ElasticBeanstalk application path where the
 *                          .elasticbeanstalk folder with profile will be created
 * @param String appEnvVars Application environment variables to be userd with
 *                          eb setenv foo=bar JDBC_CONNECTION_STRING=hello PARAM4= PARAM5=
 * @param String targetEnv  Target AWS ElasticBeanstalk environment to be created from clone operation.
 *
 * @return NO return value. This call will execute the stages declared in this module function.
 */
def deploy_BG_eb(String sourceEnv, String appPath, String appEnvVars, String targetEnv){
    dir("${appPath}"){
        stage("Clone ${sourceEnv} on ${targetEnv}"){
            sh "#!/bin/bash -e\n" +
                    "eb use ${sourceEnv}"

            sh "#!/bin/bash -e\n" +
                    "eb clone -n ${targetEnv} --exact"
        }

        stage("Deploying on: ${targetEnv}") {
            deploy_eb(targetEnv, appPath, appEnvVars)
        }
    }
}

/**
 ** Function:
 * This function invokes EB command to swap CNAMEs between 2 environments.
 * Typically used for Blue/Green deployments.
 *
 ** Parameters:
 * @param String sourceEnv  Current AWS ElasticBeanstalk environment to be cloned.
 * @param String appPath    AWS ElasticBeanstalk application path where the
 *                          .elasticbeanstalk folder with profile will be created
 * @param String targetEnv  Target AWS ElasticBeanstalk environment to be created from clone operation.
 *
 * @return NO return value. This call will execute the stages declared in this module function.
 */
def swap_BG_env_eb(String sourceEnv, String appPath, String targetEnv){
    dir("${appPath}"){
        stage("Swap BG from ${sourceEnv} to ${targetEnv}"){
            sh "#!/bin/bash -e\n" +
                    "eb swap ${sourceEnv} --destination_name ${targetEnv}"
        }
    }
}

/**
 ** Function:
 * This function invokes EB command to terminate an environment.
 * Typically used for Blue/Green deployments.
 *
 ** Parameters:
 * @param String envName    AWS ElasticBeanstalk environment name.
 * @param String appPath    AWS ElasticBeanstalk application path where the
 *                          .elasticbeanstalk folder with profile will be created
 *
 * @return NO return value. This call will execute the stages declared in this module function.
 */
def terminate_BG_env_eb(String envName, String appPath){
    dir("${appPath}"){
        stage("Terminating ${envName}"){
            sh "#!/bin/bash -e\n" +
                    "eb terminate ${envName} --force --timeout 20"
        }
    }
}

/**
 ** Function:
 * This function will deploy EB assuming the app code is already in appPath
 * and setting environment vars passed on appEnvVars
 *
 ** Parameters:
 * @param String envName    AWS ElasticBeanstalk environment name.
 * @param String appPath    AWS ElasticBeanstalk application path where the
 *                          .elasticbeanstalk folder with profile will be created
 * @param String appEnvVars Application environment variables to be userd with
 *                          eb setenv foo=bar JDBC_CONNECTION_STRING=hello PARAM4= PARAM5=
 *
 * @return NO return value. This call will execute the stages declared in this module function.
 */
def deploy_eb(String envName, String appPath, String appEnvVars){
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

// Note: this line is crucial when you want to load an external groovy script
return this
