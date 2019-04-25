#!/usr/bin/env groovy

/*
 ** Jenkins Modules:
 * docker-machine create with AWS drivers
 *
 ** IMPORTANT:
 * This module relies on docker and docker-machine, installed in the current jenkins server
 * to be configured to run as-is, this module does not handle that.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */

 /**
  *
  ** Function:
  * Based on the docker-machine AWS input arguments an AWS EC2 instance will be created
  *
  ** Parameters:
  * @param String       dockerMachineName       docker-machine name for the aws EC2 instancec.
  * @param String       userInputInstanceType   --amazonec2-instance-type: The instance type to run.
  * @param String       awsAccessKeyId          --amazonec2-access-key: Your access key ID for the Amazon Web Services API.
  * @param String       awsSecretAccessKey      --amazonec2-secret-key: Your secret access key for the Amazon Web Services API.
  * @param String       vpcId                   --amazonec2-vpc-id: Your VPC ID to launch the instance in.
  * @param String       subnetId                --amazonec2-subnet-id: AWS VPC subnet ID.
  * @param Integer[]    portNumbersArray        --amazonec2-open-port: Make the specified port number accessible from the Internet.
  * @param Integer      rootEc2VolSize          --amazonec2-root-size: The root disk size of the instance (in GB).
  * @param String       ec2AmiId                --amazonec2-ami: The AMI ID of the instance to use.
  * @param Boolean      publicIpAddr            --amazonec2-use-private-address: Use the private IP address for docker-machine,
  *                                             but still create a public IP address.
  *
  * @return NO return value. This call will execute the stages declared in this module function.
  *
  ** Examples:
  *     // Getting AWS SSM (getStringValueWprofile Function has been deprecated - so take it just as an example)
  *     String awsAccessKeyId = awsSsmHelper.getStringValueWprofile(ssmParamName1,true,aws_iam_role_profile_sr)
  *     String awsSecretAccessKey = awsSsmHelper.getStringValueWprofile(ssmParamName2,true,aws_iam_role_profile_sr)
  *
  *     // Docker Machine AWs Size
  *     String awsEc2Size = dockerMachineawsEc2Size()
  *
  *     // Creating Docker-Machine on AWS
  *     String dockerMachineName = "${userInputApp}-${date}-${appEnv}"
  *     Integer[] portNumbersArray = new Integer[2] as java.lang.Object
  *     portNumbersArray[0] = 80
  *     portNumbersArray[1] = 443
  *     Integer rootEc2VolSize = 30
  *     Boolean publicIpAddr = false
  *
  *     dockerMachineAws.call(awsEc2Size, awsAccessKeyId, awsSecretAccessKey, dockerMachineName, vpcId,
  *                 subnetId, portNumbersArray, publicIpAddr, rootEc2VolSize, ec2AmiId)
  *
  *     // or
  *     // We can just run it with "externalCall(...)" since it has a call method.
  *     dockerMachineAws(awsEc2Size, awsAccessKeyId, awsSecretAccessKey, dockerMachineName, vpcId,
  *                 subnetId, portNumbersArray, publicIpAddr ,rootEc2VolSize, ec2AmiId)
 */

def call(String userInputInstanceType, String awsAccessKeyId, String awsSecretAccessKey, String dockerMachineName,
         String vpcId, String subnetId, Integer[] portNumbersArray, Boolean publicIpAddr,Integer rootEc2VolSize,
         String ec2AmiId) {

    try {
        echo "Create docker machine ${userInputInstanceType} on AWS"
        //Validate docker OS pkg dependencies are satisfied.
        sh """
            #!/bin/bash
            docker -v && docker-machine -v
           """

        echo "AWS instance type: ${userInputInstanceType}"
        echo "AWS publicIpAddr: ${publicIpAddr}"

        if (publicIpAddr) {
            if (portNumbersArray.length == 1) {
                sh "#!/bin/bash +x \n" +
                        "docker-machine create --driver amazonec2 --amazonec2-access-key ${awsAccessKeyId} --amazonec2-secret-key ${awsSecretAccessKey}" +
                        " -amazonec2-ami ${ec2AmiId} --amazonec2-region 'us-east-1' --amazonec2-vpc-id ${vpcId} --amazonec2-zone 'a'" +
                        " --amazonec2-subnet-id ${subnetId} --amazonec2-open-port ${portNumbersArray[0]} --amazonec2-instance-type ${userInputInstanceType}" +
                        " --amazonec2-root-size ${rootEc2VolSize} --amazonec2-ssh-user ubuntu ${dockerMachineName}"

            } else if (portNumbersArray.length == 2) {
                sh "#!/bin/bash +x \n" +
                        "docker-machine create --driver amazonec2 --amazonec2-access-key ${awsAccessKeyId} --amazonec2-secret-key ${awsSecretAccessKey}" +
                        " -amazonec2-ami ${ec2AmiId} --amazonec2-region 'us-east-1' --amazonec2-vpc-id ${vpcId} --amazonec2-zone 'a'" +
                        " --amazonec2-subnet-id ${subnetId} --amazonec2-open-port ${portNumbersArray[0]} --amazonec2-open-port ${portNumbersArray[1]}" +
                        " --amazonec2-instance-type ${userInputInstanceType} --amazonec2-root-size ${rootEc2VolSize} --amazonec2-ssh-user ubuntu ${dockerMachineName}"

            } else if (portNumbersArray.length == 3) {
                sh "#!/bin/bash +x \n" +
                        "docker-machine create --driver amazonec2 --amazonec2-access-key ${awsAccessKeyId} --amazonec2-secret-key ${awsSecretAccessKey}" +
                        " -amazonec2-ami ${ec2AmiId} --amazonec2-region 'us-east-1' --amazonec2-vpc-id ${vpcId} --amazonec2-zone 'a'" +
                        " --amazonec2-subnet-id ${subnetId} --amazonec2-open-port ${portNumbersArray[0]} --amazonec2-open-port ${portNumbersArray[1]}" +
                        " --amazonec2-open-port ${portNumbersArray[2]} --amazonec2-instance-type ${userInputInstanceType} --amazonec2-root-size ${rootEc2VolSize}" +
                        " --amazonec2-ssh-user ubuntu ${dockerMachineName}"

            }
        } else {
            if (portNumbersArray.length == 1) {
                sh "#!/bin/bash +x \n" +
                        "docker-machine create --driver amazonec2 --amazonec2-access-key ${awsAccessKeyId} --amazonec2-secret-key ${awsSecretAccessKey}" +
                        " -amazonec2-ami ${ec2AmiId} --amazonec2-region 'us-east-1' --amazonec2-vpc-id ${vpcId} --amazonec2-zone 'a'" +
                        " --amazonec2-subnet-id ${subnetId} --amazonec2-open-port ${portNumbersArray[0]} --amazonec2-private-address-only" +
                        " --amazonec2-instance-type ${userInputInstanceType} --amazonec2-root-size ${rootEc2VolSize} --amazonec2-ssh-user ubuntu ${dockerMachineName}"

            } else if (portNumbersArray.length == 2) {
                sh "#!/bin/bash +x \n" +
                        "docker-machine create --driver amazonec2 --amazonec2-access-key ${awsAccessKeyId} --amazonec2-secret-key ${awsSecretAccessKey}" +
                        " -amazonec2-ami ${ec2AmiId} --amazonec2-region 'us-east-1' --amazonec2-vpc-id ${vpcId} --amazonec2-zone 'a'" +
                        " --amazonec2-subnet-id ${subnetId} --amazonec2-open-port ${portNumbersArray[0]} --amazonec2-open-port ${portNumbersArray[1]}" +
                        " --amazonec2-private-address-only --amazonec2-instance-type ${userInputInstanceType} --amazonec2-root-size ${rootEc2VolSize}" +
                        " --amazonec2-ssh-user ubuntu ${dockerMachineName}"

            } else if (portNumbersArray.length == 3) {
                sh "#!/bin/bash +x \n" +
                        "docker-machine create --driver amazonec2 --amazonec2-access-key ${awsAccessKeyId} --amazonec2-secret-key ${awsSecretAccessKey}" +
                        " -amazonec2-ami ${ec2AmiId} --amazonec2-region 'us-east-1' --amazonec2-vpc-id ${vpcId} --amazonec2-zone 'a'" +
                        " --amazonec2-subnet-id ${subnetId} --amazonec2-open-port ${portNumbersArray[0]} --amazonec2-open-port ${portNumbersArray[1]}" +
                        " --amazonec2-open-port ${portNumbersArray[2]} --amazonec2-private-address-only --amazonec2-instance-type ${userInputInstanceType}" +
                        " --amazonec2-root-size ${rootEc2VolSize} --amazonec2-ssh-user ubuntu ${dockerMachineName}"
            }

            sh "docker-machine status ${dockerMachineName}"
        }
    } catch (e) {
        echo "[ERROR] Exception: ${e}"
        throw e as Throwable
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this