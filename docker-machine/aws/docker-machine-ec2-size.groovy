#!/usr/bin/env groovy

/*
 ** Jenkins Modules:
 * jenkins pipeline AWS EC2 type input parameter selection box.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */

/**
 *
 ** Function:
 * This function will instanciate the std lib jenkins method to create an input parameter selection box
 *
 * Method declaration:
 *     method(name: 'input', type: 'Object', namedParams: [parameter(name: 'message', type: 'java.lang.String'),
 *     parameter(name: 'id', type: 'java.lang.String'), parameter(name: 'ok', type: 'java.lang.String'),
 *     parameter(name: 'parameters', type: 'Map'), parameter(name: 'submitter', type: 'java.lang.String'),
 *     parameter(name: 'submitterParameter', type: 'java.lang.String'), ], doc: 'Wait for interactive input')
 *
 ** Parameters:
 * @return String userInputEc2Size  AWS EC2 Type: t2.micro, t2.small, t2.medium or t2.large
 *
 ** Examples:
 *     // Docker Machine AWs Size
 *     String awsuserInputEc2Size = dockerMachineawsuserInputEc2Size()
 *
 *     // Creating Docker-Machine on AWS
 *     String dockerMachineName = "${userInputApp}-${date}-${appEnv}"
 *     Integer[] portNumbersArray = new Integer[2] as java.lang.Object
 *     portNumbersArray[0] = 80
 *     portNumbersArray[1] = 443
 *     Integer rootEc2VolSize = 30
 *     Boolean publicIpAddr = false
 *
 *     dockerMachineAws.call(userInputEc2Size, awsAccessKeyId, awsSecretAccessKey, dockerMachineName, vpcId,
 *                 subnetId, portNumbersArray, publicIpAddr, rootEc2VolSize, ec2AmiId, awsDmEc2Profile)
 *
 *     // or
 *     // We can just run it with "externalCall(...)" since it has a call method.
 *     dockerMachineAws(userInputEc2Size, awsAccessKeyId, awsSecretAccessKey, dockerMachineName, vpcId,
 *                 subnetId, portNumbersArray, publicIpAddr ,rootEc2VolSize, ec2AmiId, awsDmEc2Profile)
 */


def call() {
    try {
        stage('\u2708 Enter AWS Instance Size \u2705') {
            String userInputEc2Size = input(
                    id: 'userInputInstanceType', message: 'Choose instance type', ok: 'Submit', parameters: [
                    [$class: 'ChoiceParameterDefinition', choices:
                            't2.micro\n' +
                            't2.small\n' +
                            't2.medium\n' +
                            't2.large\n' +
                            't2.xlarge', description: 'AWS instance type:\n' +
                            't2.small $0.023/hr ||\n' +
                            't2.medium $0.047/hr ||\n' +
                            't2.large $0.094/hr ||\n' +
                            't2.xlarge $0.186/hr ||\n' +
                            'Billing Details: https://aws.amazon.com/ec2/pricing/', name: 'target']
            ])
            echo "AWS instance type: ${userInputEc2Size}"
            return userInputEc2Size
        }
    } catch (e) {
        echo "[ERROR] Exception: ${e}"
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this