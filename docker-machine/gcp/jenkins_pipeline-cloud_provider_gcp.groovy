#!/usr/bin/env groovy
/*
 * Jenkins Modules: docker-machine create with GCP drivers managing VM Size and public/private IP addr opt.
 *
 * Important: this module relies on docker and docker-machine, installed in the current jenkins server
 * to be configured to run as-is, this module does not handle that.
 *
 * @param machineName       docker-machine name for the aws EC2 instancec.
 * @param projectId         --google-project: required The ID of your project to use when launching the instance.
 * @param machineUsername   --google-username: The username to use for the instance.
 */

def call(machineName,machineUsername,projectId) {

    def userInputVmSize
    def userInputVmPubIp

    try {

        stage('\u2708 Enter GCP machine size \u2705') {
            userInputVmSize = input(
                    id: 'userInputVmSize', message: 'Choose VM Size', ok: 'Submit', parameters: [
                    [$class: 'ChoiceParameterDefinition', choices: 'f1-micro\n' +
                            'g1-small\nn1-standard-1\nn1-standard-2', description: 'GCP VM Size:\n' +
                            'f1-micro Effective hourly rate $0.006/hr ||\n' +
                            'g-1small $0.019/hr ||\n' +
                            'n1-standard-1 $0.034/hr ||\n' +
                            'n1-standard-2 $0.067/hr ||\n' +
                            'Billing Details: https://cloud.google.com/compute/pricing', name: 'target']
            ])
            echo "GCP Virtual Machine Size: ${userInputVmSize}"

        }

        stage('\u2708 Use Public IP Address? \u2705') {
            userInputVmPubIp = input(
                    id: 'userInputVmPubIp', message: 'Add Public IP Address?', ok: 'Submit', parameters: [
                    [$class: 'ChoiceParameterDefinition', choices: 'Yes\nNo', description: 'GCP VM Public Ip Addr', name: 'target']
            ])

            echo "GCP VM it's going to use Public IP: ${userInputVmPubIp}"
        }

        stage("Create docker machine ${userInputVmSize} on GoogleCP") {
            sh "#!/bin/bash \n" +
                    "docker -v && -v && docker-machine -v"

            echo "GCP instance type: ${userInputVmSize}"

            if (userInputVmPubIp == 'Yes'){
                sh "#!/bin/sh \n" +
                        "docker-machine create -d google --google-project ${projectId} --google-use-internal-ip --google-tags http-server,https-server " +
                        "--google-machine-image 'ubuntu-os-cloud/global/images/family/ubuntu-1604-lts' --google-machine-type ${userInputVmSize} " +
                        "--google-zone 'us-east1-d' --google-username ${machineUsername} ${machineName}"
            } else {
                sh "#!/bin/sh \n" +
                        "docker-machine create -d google --google-project ${projectId} --google-use-internal-ip-only --google-tags http-server,https-server " +
                        "--google-machine-image 'ubuntu-os-cloud/global/images/family/ubuntu-1604-lts' --google-machine-type ${userInputVmSize} " +
                        "--google-zone 'us-east1-d' --google-username ${machineUsername} ${machineName}"
            }

            sh "#!/bin/sh \n" +
                    "docker-machine status ${machineName}"
        }

    } catch (e) {
        throw e as Throwable
    }
}

return this