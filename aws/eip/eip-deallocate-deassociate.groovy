#!/usr/bin/env groovy
/*
 ** Jenkins Modules:
 * AWS deallocate EIP ans deassociate from EC2.
 *
 ** Important: this module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 * This module has to be load as shown in the root context README.md
*/

/**
 ** Function: This function deassociates an Elastic IP Address fron an EC2 and deallocates this EIP from AWS EIPs with the
 * arguments (strings) passed: "eipAssoc" & "eipAlloc".
 *
 * Parameters:
 * @param String eipAssoc   AWS EC2 EIP association ID
 * @param String eipAlloc   AWS EC2 EIP allocation ID
 */

/*
 ** Examples:
 * A) Sample usage from a Pipeline Stage (you must include the function)
 *
 *  node {
 *      stage('EIP deallocate deassociate Sample') {
 *          call("eipalloc-d31335e6,eipassoc-bf12d089")
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   EIP_DEASSOC_DEALLOC = load "jenkins_pipeline-aws_eip_deallocate_deassociate.groovy"
 *   EIP_DEASSOC_DEALLOC.call("eipalloc-d31335e6,eipassoc-bf12d089")
 */

def call(String eipAssoc, String eipAlloc) {

    try {
        sh "aws ec2 disassociate-address --association-id eipassoc-${eipAssoc}"
        sh "aws ec2 release-address --allocation-id eipalloc-${eipAlloc}"

        return true

    } catch (e) {
        throw e as Throwable
        echo "ec2 disassociate-address and/or release-address FAILED due to ${e}"
        return false
    }
}


// Note: this line is crucial when you want to load an external groovy script
return this