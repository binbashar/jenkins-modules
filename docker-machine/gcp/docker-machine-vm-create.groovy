#!/usr/bin/env groovy
/*
 ** Jenkins Modules:
 * docker-machine create with GCP drivers managing VM Size and public/private IP addr opt.
 *
 ** IMPORTANT: this module relies on docker and docker-machine, installed in the current jenkins server
 * to be configured to run as-is, this module does not handle that.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
*/
/**
 ** Function:
 * Based on the docker-machine GPC input arguments an GCP VM will be created.
 *
 ** Parameters:
 * @param String    dockerMachineName  docker-machine name for the GCP VM.
 * @param String    projectId          --google-project: required The ID of your project to use when launching the instance.
 * @param String    machineUsername    --google-username: The username to use for the instance.
 * @param String    userInputVmSize    --google-disk-type: The disk type of instance.
 * @param Integer   rootVmDiskSize     --google-disk-size: The disk size of instance.
 *
 * @return NO return value. This call will execute the stages declared in this module function.
 *
 ** Examples:
 *     // Docker Machine GPC Size
 *     String userInputVmSize = dockerMachineawsVmSize()
 *
 *     // Creating Docker-Machine on GPC
 *     String dockerMachineName = "${userInputApp}-${date}-${appEnv}"
 *     Integer rootVmDiskSize = 30
 *     String projectId = cool-ocean-345643
 *     String machineUsername = "jenkins"
 *
 *     dockerMachineGcp.call(userInputVmSize, dockerMachineName, rootVmDiskSize, machineUsername)
 *
 *     // or
 *     // We can just run it with "externalCall(...)" since it has a call method.
 *     dockerMachineGcp(userInputVmSize, dockerMachineName, rootVmDiskSize, machineUsername)
 */
def call(String dockerMachineName, String machineUsername,String projectId, String userInputVmSize) {
    
    String userInputVmPubIp

    try {
        
        stage('\u2708 Use Public IP Address? \u2705') {
            userInputVmPubIp = input(
                    id: 'userInputVmPubIp', message: 'Add Public IP Address?', ok: 'Submit', parameters: [
                    [$class: 'ChoiceParameterDefinition', choices: 'Yes\nNo', description: 'GCP VM Public Ip Addr', name: 'target']
            ])

            userInputVmPubIp = userInputVmPubIp.toBoolean()
            echo "GCP VM it's going to use Public IP: ${userInputVmPubIp}"
        }

        stage("Create docker machine ${userInputVmSize} on GoogleCP") {
            //Validate docker OS pkg dependencies are satisfied.
            sh """
                #!/bin/bash
                docker -v && docker-machine -v
               """

            echo "GCP instance type: ${userInputVmSize}"

            if (userInputVmPubIp){
                sh "#!/bin/sh \n" +
                        "docker-machine create -d google --google-project ${projectId} --google-use-internal-ip --google-tags http-server,https-server " +
                        "--google-machine-image 'ubuntu-os-cloud/global/images/family/ubuntu-1604-lts' --google-machine-type ${userInputVmSize} " +
                        "--google-disk-size ${rootVmDiskSize} --google-zone 'us-east1-d' --google-username ${machineUsername} ${dockerMachineName}"
            } else {
                sh "#!/bin/sh \n" +
                        "docker-machine create -d google --google-project ${projectId} --google-use-internal-ip-only --google-tags http-server,https-server " +
                        "--google-machine-image 'ubuntu-os-cloud/global/images/family/ubuntu-1604-lts' --google-machine-type ${userInputVmSize} " +
                        "--google-disk-size ${rootVmDiskSize} --google-zone 'us-east1-d' --google-username ${machineUsername} ${dockerMachineName}"
            }

            sh "docker-machine status ${dockerMachineName}"
        }

    } catch (e) {
        echo "[ERROR] Exception: ${e}"
        throw e as Throwable
    }
}

return this