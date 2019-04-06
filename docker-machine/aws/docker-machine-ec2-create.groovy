#!/usr/bin/env groovy
/*
 * Jenkins Modules: docker-machine create with AWS drivers
 *
 * Important: this module relies on docker and docker-machine, installed in the current jenkins server
 * to be configured to run as-is, this module does not handle that.
 *
 * @param machineName               docker-machine name for the aws EC2 instancec.
 * @param userInputInstanceType     --amazonec2-instance-type: The instance type to run.
 * @param awsAccessKeyId mysql      --amazonec2-access-key: Your access key ID for the Amazon Web Services API.
 * @param awsSecretAccessKey        --amazonec2-secret-key: Your secret access key for the Amazon Web Services API.
 * @param vpcId                     --amazonec2-vpc-id: Your VPC ID to launch the instance in.
 * @param subnetId                  --amazonec2-subnet-id: AWS VPC subnet ID.
 * @param portNumbersArray          --amazonec2-open-port: Make the specified port number accessible from the Internet.
 * @param rootEc2VolSize            --amazonec2-root-size: The root disk size of the instance (in GB).
 * @param ec2AmiId                  --amazonec2-ami: The AMI ID of the instance to use.
 * @param publicIpAddr              --amazonec2-use-private-address: Use the private IP address for docker-machine,
 *                                  but still create a public IP address.
 */

def call(userInputInstanceType, awsAccessKeyId, awsSecretAccessKey,machineName,vpcId,subnetId,
            portNumbersArray,publicIpAddr,rootEc2VolSize,ec2AmiId) {

    try {
        echo "Create docker machine ${userInputInstanceType} on AWS"
        sh "#!/bin/bash \n" +
                "docker -v && docker-compose -v && docker-machine -v"

        echo "AWS instance type: ${userInputInstanceType}"
        echo "AWS publicIpAddr: ${publicIpAddr}"

        if (publicIpAddr == true) {
            if (portNumbersArray.length == 1) {
                sh "#!/bin/bash +x \n" +
                        "docker-machine create --driver amazonec2 --amazonec2-access-key ${awsAccessKeyId} --amazonec2-secret-key ${awsSecretAccessKey}" +
                        " -amazonec2-ami ${ec2AmiId} --amazonec2-region 'us-east-1' --amazonec2-vpc-id ${vpcId} --amazonec2-zone 'a'" +
                        " --amazonec2-subnet-id ${subnetId} --amazonec2-open-port ${portNumbersArray[0]} --amazonec2-instance-type ${userInputInstanceType}" +
                        " --amazonec2-root-size ${rootEc2VolSize} --amazonec2-ssh-user ubuntu ${machineName}"

            } else if (portNumbersArray.length == 2) {
                sh "#!/bin/bash +x \n" +
                        "docker-machine create --driver amazonec2 --amazonec2-access-key ${awsAccessKeyId} --amazonec2-secret-key ${awsSecretAccessKey}" +
                        " -amazonec2-ami ${ec2AmiId} --amazonec2-region 'us-east-1' --amazonec2-vpc-id ${vpcId} --amazonec2-zone 'a'" +
                        " --amazonec2-subnet-id ${subnetId} --amazonec2-open-port ${portNumbersArray[0]} --amazonec2-open-port ${portNumbersArray[1]}" +
                        " --amazonec2-instance-type ${userInputInstanceType} --amazonec2-root-size ${rootEc2VolSize} --amazonec2-ssh-user ubuntu ${machineName}"

            } else if (portNumbersArray.length == 3) {
                sh "#!/bin/bash +x \n" +
                        "docker-machine create --driver amazonec2 --amazonec2-access-key ${awsAccessKeyId} --amazonec2-secret-key ${awsSecretAccessKey}" +
                        " -amazonec2-ami ${ec2AmiId} --amazonec2-region 'us-east-1' --amazonec2-vpc-id ${vpcId} --amazonec2-zone 'a'" +
                        " --amazonec2-subnet-id ${subnetId} --amazonec2-open-port ${portNumbersArray[0]} --amazonec2-open-port ${portNumbersArray[1]}" +
                        " --amazonec2-open-port ${portNumbersArray[2]} --amazonec2-instance-type ${userInputInstanceType} --amazonec2-root-size ${rootEc2VolSize}" +
                        " --amazonec2-ssh-user ubuntu ${machineName}"

            }
        } else {
            if (portNumbersArray.length == 1) {
                sh "#!/bin/bash +x \n" +
                        "docker-machine create --driver amazonec2 --amazonec2-access-key ${awsAccessKeyId} --amazonec2-secret-key ${awsSecretAccessKey}" +
                        " -amazonec2-ami ${ec2AmiId} --amazonec2-region 'us-east-1' --amazonec2-vpc-id ${vpcId} --amazonec2-zone 'a'" +
                        " --amazonec2-subnet-id ${subnetId} --amazonec2-open-port ${portNumbersArray[0]} --amazonec2-private-address-only" +
                        " --amazonec2-instance-type ${userInputInstanceType} --amazonec2-root-size ${rootEc2VolSize} --amazonec2-ssh-user ubuntu ${machineName}"

            } else if (portNumbersArray.length == 2) {
                sh "#!/bin/bash +x \n" +
                        "docker-machine create --driver amazonec2 --amazonec2-access-key ${awsAccessKeyId} --amazonec2-secret-key ${awsSecretAccessKey}" +
                        " -amazonec2-ami ${ec2AmiId} --amazonec2-region 'us-east-1' --amazonec2-vpc-id ${vpcId} --amazonec2-zone 'a'" +
                        " --amazonec2-subnet-id ${subnetId} --amazonec2-open-port ${portNumbersArray[0]} --amazonec2-open-port ${portNumbersArray[1]}" +
                        " --amazonec2-private-address-only --amazonec2-instance-type ${userInputInstanceType} --amazonec2-root-size ${rootEc2VolSize}" +
                        " --amazonec2-ssh-user ubuntu ${machineName}"

            } else if (portNumbersArray.length == 3) {
                sh "#!/bin/bash +x \n" +
                        "docker-machine create --driver amazonec2 --amazonec2-access-key ${awsAccessKeyId} --amazonec2-secret-key ${awsSecretAccessKey}" +
                        " -amazonec2-ami ${ec2AmiId} --amazonec2-region 'us-east-1' --amazonec2-vpc-id ${vpcId} --amazonec2-zone 'a'" +
                        " --amazonec2-subnet-id ${subnetId} --amazonec2-open-port ${portNumbersArray[0]} --amazonec2-open-port ${portNumbersArray[1]}" +
                        " --amazonec2-open-port ${portNumbersArray[2]} --amazonec2-private-address-only --amazonec2-instance-type ${userInputInstanceType}" +
                        " --amazonec2-root-size ${rootEc2VolSize} --amazonec2-ssh-user ubuntu ${machineName}"
            }

            sh "docker-machine status ${machineName}"
        }
    } catch (e) {
        throw e as Throwable
    }
}

return this