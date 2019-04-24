#!/usr/bin/env groovy

/*
 ** Jenkins Modules:
 * AWS deallocate EIP ans disassociate from EC2.
 *
 ** IMPORTANT:
 * This module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
*/

/**
 ** Function:
 * This function disassociates an Elastic IP Address fron an EC2 and deallocates this EIP from AWS EIPs with the
 * arguments (strings) passed: "eipAssoc" & "eipAlloc".
 *
 * Parameters:
 * @param String eipAssoc   AWS EC2 EIP association ID
 * @param String eipAlloc   AWS EC2 EIP allocation ID
 *
 * @return Boolean          Will reflect if the EC2-VPC EIP related operations:
 *                          -> ec2 disassociate-address & ec2 release-address
 *                          are successfully executed (true) or failed (false).
 */

/*
 ** Examples:
 * A) Sample usage from a Pipeline Stage (you must include the function in the same groovy script)
 *
 *  node {
 *      stage('EIP deallocate disassociate Sample') {
 *          call("eipalloc-d31335e6,eipassoc-bf12d089")
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   eipDesassocDealloc = load "jenkins_pipeline-aws_eip_deallocate_disassociate.groovy"
 *   eipDesassocDealloc.call("eipalloc-d31335e6,eipassoc-bf12d089")
 *
 *   // or
 *   // We can just run it with "externalCall(...)" since it has a call method.
 *   eipDesassocDealloc("eipalloc-d31335e6,eipassoc-bf12d089")
 */

def call(String eipAssoc, String eipAlloc) {

    try {
        sh "aws ec2 disassociate-address --association-id eipassoc-${eipAssoc}"
        sh "aws ec2 release-address --allocation-id eipalloc-${eipAlloc}"

        return true

    } catch (e) {
        echo "[ERROR] ec2 disassociate-address || release-address FAILED due to ${e}"
        return false
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this