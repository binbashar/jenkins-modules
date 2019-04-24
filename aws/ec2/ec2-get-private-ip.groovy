#!/usr/bin/env groovy

/**
 ** Jenkins Modules:
 * AWS EC2 get ip address.
 *
 ** IMPORTANT:
 * this module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 *

/*
 ** Function:
 ** This function returns the EC2 private IP address based on the intance id passed as argument
 *
 ** Parameters:
 * @param String ec2_id             AWS EC2 id.
 *
 * @return String ec2PrivIpAddr     String containing the AWS EC2 private IP address, eg: '172.23.10.1'
 *
 ** Examples:
 * A) Sample usage from a Pipeline Stage (you must include the function in the same groovy script)
 *
 *  node {
 *      stage('Get EC2 private IP addr Sample') {
 *          print "ec2PrivIpAddr: " + call(i-0c071000c63b1200d)
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   ec2PrivIpAddr = load "jenkins_pipeline-aws_ec2_get_private_ip.groovy"
 *   print "EC2 private IP addr" + ec2PrivIpAddr.call(i-0c071000c63b1200d)
 *
 *   // or
 *   // We can just run it with "externalCall(...)" since it has a call method.
 *   print "EC2 private IP addr" + ec2PrivIpAddr(i-0c071000c63b1200d)
 */

def call(String ec2_id) {

    ec2PrivIpAddr = sh(
            script: "aws ec2 describe-instances --instance-ids ${ec2_id} | grep PrivateIpAddress | tail -n 1 | cut -d':' -f2",
            returnStdout: true
            //returnStatus: true
    ).trim()

    echo "ec2PrivIpAddr: ${ec2PrivIpAddr}"

    return ec2PrivIpAddr

}

// Note: this line is crucial when you want to load an external groovy script
return this