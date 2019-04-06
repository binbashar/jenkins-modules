#!/usr/bin/env groovy
/*
 * Jenkins Modules: AWS deallocate EIP ans deassociate from EC2.
 *
 * Important: this module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 *
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

/*
 * This function deassociates an Elastic IP Address fron an EC2 and deallocates this EIP from AWS EIPs with the
 * arguments (strings) passed: "eip_assoc" & "eip_alloc".
 */


def call(eip_assoc,eip_alloc) {

    try {
        sh "aws ec2 disassociate-address --association-id eipassoc-${eip_assoc}"
        sh "aws ec2 release-address --allocation-id eipalloc-${eip_alloc}"

        return true

    } catch (e) {
        throw e as Throwable
        echo "ec2 disassociate-address and/or release-address FAILED due to ${e}"
        return false
    }
}


// Note: this line is crucial when you want to load an external groovy script
return this