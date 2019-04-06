#!/usr/bin/env groovy
/**
 *  Created by Exequiel Barrirero & Marcos Pagnucco on 03/02/17.
 */

def userInput_docker_machine_name
def cloud_provider

def extMain(projectName) {
    
    node {
        try {

                echo '✈ List Docker Machines ✅'
                sh "docker-machine ls | grep ${projectName} | awk {'print \$1'} > docker_machines_list.txt"
                def docker_machine_list
                docker_machine_list = readFile 'docker_machines_list.txt'
                echo "Docker Machines are: ${docker_machine_list}"

                userInput_docker_machine_name = input(
                        id: 'userInput', message: "Please type the Docker-Machine to deploy/stop/start/remove:\n ${docker_machine_list}", parameters: [
                        [$class: 'TextParameterDefinition', defaultValue: 'Docker Machine name receiving action', description: 'Docker-Machine name', name: 'dm']
                ])
                echo("Docker-Machine to deploy/stop/start/remove: ${userInput_docker_machine_name}")

                File dm_file = new File("${env.WORKSPACE}/docker_machines_list.txt")

                if (!dm_file.exists()) {
                    println "File does not exist"
                } else {
                    dm_file_lines = dm_file.readLines()
                    if (dm_file_lines.contains(userInput_docker_machine_name)) {
                        cloud_provider = sh(
                                script: "docker-machine ls | grep ${userInput_docker_machine_name} | awk {'print \$3'}",
                                returnStdout: true
                        ).trim()
                        echo("Docker-Machine Cloud Provider: ${cloud_provider}")
                    } else {
                        echo("The Docker-Machine name: ${userInput_docker_machine_name} was INCORRECTLY written in the INPUT")
                    }
                }

        } catch (e) {
            throw e as Throwable
        }
    }
}

return this

def returnMachineName() {
    node {
        try {
            return userInput_docker_machine_name
        } catch (e) {
            throw e as Throwable
        }
    }
}

def returnCloudProvider() {
    node {
        try {
            return cloud_provider
        } catch (e) {
            throw e as Throwable
        }
    }
}
