#!/usr/bin/env groovy
/*
 ** Jenkins Modules:
 * docker-machine list and let the user to interactively choose the machine to work with.
 *
 ** IMPORTANT:
 * This module relies on docker and docker-machine, installed in the current jenkins server
 * to be configured to run as-is, this module does not handle that.
 *
 * This module has to be load as shown in the root context README.md
 */

/**
 ** Function:
 * Based on the docker-machine ls output an input parameter combo box will be filled and presented for the user to
 * interactively choose the docker-machine to work with afterwards returning the docker-machine name.
 *
 ** Parameters:
 * @param String projectName                    Project name (or custom string) to filter the docker-machine ls output.
 *
 * @return String userInputDockerMachineName    Docker Machine Name
 *
 ** Examples:
 *  dockerMachineName = dockerMachineHelper.getDmNameInteractively("py-flask-app")
 */
def getDmNameInteractively(String projectName) {

    try {
        echo '✈ List Docker Machines ✅'
        
        // Parse the output of 'docker-machine ls' cmd in order to get and redirect the list to a .txt file 
        sh "docker-machine ls | grep ${projectName} | awk {'print \$1'} > docker_machines_list.txt"
        
        // Assing the listed docker-machines from the .txt previously generated file
        def dockerMachineList
        dockerMachineList = readFile 'docker_machines_list.txt'
        echo "Docker Machines are: ${dockerMachineList}"

        // Interactively let the user choose (type) which docker-machine to work with
        String userInputDockerMachineName = input(
                id: 'userInput', message: "Please type the Docker-Machine to deploy/stop/start/remove:\n ${dockerMachineList}", parameters: [
                [$class: 'TextParameterDefinition', defaultValue: 'Docker Machine name receiving action', description: 'Docker-Machine name', name: 'dm']
        ])
        
        echo("Docker-Machine to deploy/stop/start/remove: ${userInputDockerMachineName}")

        File dm_file = new File("${env.WORKSPACE}/docker_machines_list.txt")

        if (!dm_file.exists()) {
            println "File does not exist"
        } else {
            dm_file_lines = dm_file.readLines()
            if (dm_file_lines.contains(userInputDockerMachineName)) {
                // Parse the docker-machine provider: aws, gcp, digital ocean, etc.
                dockerMachineProvider = sh(
                        script: "docker-machine ls | grep ${userInputDockerMachineName} | awk {'print \$3'}",
                        returnStdout: true
                ).trim()
                echo("Docker-Machine Cloud Provider: ${dockerMachineProvider}")
            } else {
                echo("The Docker-Machine name: ${userInputDockerMachineName} was INCORRECTLY written in the INPUT")
            }
        }

        return userInputDockerMachineName

    } catch (e) {
        throw e as Throwable
    }
}

/**
 ** Function:
 * Based on the docker-machine ls output an input parameter combo box will be filled and presented for the user to
 * interactively choose the docker-machine to work with afterwards returning the docker-machine provider for the machine.
 *
 ** Parameters:
 * @param String projectName    Project name (or custom string) to filter the docker-machine ls output.
 *
 ** Examples:
 *  dockerMachineProvider = getDockerMachineProvider.getDmNameInteractively("py-flask-app")
 */
def getDockerMachineProvider(String projectName) {

    try {
        echo '✈ List Docker Machines ✅'

        // Parse the output of 'docker-machine ls' cmd in order to get and redirect the list to a .txt file
        sh "docker-machine ls | grep ${projectName} | awk {'print \$1'} > docker_machines_list.txt"

        // Assing the listed docker-machines from the .txt previously generated file
        def dockerMachineList
        dockerMachineList = readFile 'docker_machines_list.txt'
        echo "Docker Machines are: ${dockerMachineList}"

        // Interactively let the user choose (type) which docker-machine to work with
        userInputDockerMachineName = input(
                id: 'userInput', message: "Please type the Docker-Machine to deploy/stop/start/remove:\n ${dockerMachineList}", parameters: [
                [$class: 'TextParameterDefinition', defaultValue: 'Docker Machine name receiving action', description: 'Docker-Machine name', name: 'dm']
        ])

        echo("Docker-Machine to deploy/stop/start/remove: ${userInputDockerMachineName}")

        File dm_file = new File("${env.WORKSPACE}/docker_machines_list.txt")

        if (!dm_file.exists()) {
            println "File does not exist"
        } else {
            dm_file_lines = dm_file.readLines()
            if (dm_file_lines.contains(userInputDockerMachineName)) {
                // Parse the docker-machine provider: aws, gcp, digital ocean, etc.
                String dockerMachineProvider = sh(
                        script: "docker-machine ls | grep ${userInputDockerMachineName} | awk {'print \$3'}",
                        returnStdout: true
                ).trim()
                echo("Docker-Machine Cloud Provider: ${dockerMachineProvider}")
            } else {
                echo("The Docker-Machine name: ${userInputDockerMachineName} was INCORRECTLY written in the INPUT")
            }
        }

        return dockerMachineProvider
        
    } catch (e) {
        throw e as Throwable
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this