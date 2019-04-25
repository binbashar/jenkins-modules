#!/usr/bin/env groovy

/*
 ** Jenkins Modules:
 * AWS allocate EIP and associate to EC2.
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
 * This function allocates a new AWS Elastic IP Address and associates with the passed "String instanceId" argument
 * after its execution it can return the EIP Association Id.
 *
 ** Parameters:
 * @param   String instanceId   AWS EC2 Identifier.
 *
 * @return  String eipAssocId   AWS EIP AssociationId to EC2-VPC. The ID that represents the association of the Elastic
 *                              IP address with an instance. Eg: "eipassoc-2bebb745"
 */

/*
** Examples:
* A) Sample usage from a Pipeline Stage (you must include the function in the same groovy script)
*
*  node {
*      stage('EIP allocate & associate to EC2 sample') {
*          print "eipAllocAssocId: " + getEipAllocAssocId("i-0c0743312321b1200d")
*      }
*  }
*
*  B) Sample usage as a loaded groovy script
*
*   eipAllocAssocHelper = load "eip-allocate-associate.groovy"
*   print "eipAllocAssocId: " + eipAllocAssocHelper.getEipAllocAssocId("i-0c0743312321b1200d")
*/
def getEipAllocAssocId(String instanceId) {
    String eipAllocId = getEipAllocId()
    String eipAssocId = sh(
            script: "aws ec2 associate-address --instance-id ${instanceId}" +
                    " --allocation-id eipalloc-${eipAllocId}|grep eipassoc" +
                    "|cut -d ':' -f2|cut -d '-' -f2|cut -d '\"' -f1",
            returnStdout: true
    ).trim()
    echo "eipAssocId: ${eipAssocId}"
    return eipAssocId
}

/**
 ** Function:
 * This function returns the AWS EIP Public Address needing as input argument the "String eipAllocId"
 *
 ** Parameters:
 * @param String eipAllocId    AWS EIP AllocationId [EC2-VPC] The ID that AWS assigns to represent the allocation of
 *                             the Elastic IP address for use with instances in a VPC. Eg: "eipalloc-64d5890a"
 *
 * @return String eipAddr      AWS EIP public address.
 */

/*
** Examples:
* A) Sample usage from a Pipeline Stage (you must include the function in the same groovy script)
*
*  node {
*      stage('EIP public address sample') {
*          print "eipAddr: " + getEipAddr("eipalloc-64d5890a")
*      }
*  }
*
*  B) Sample usage as a loaded groovy script
*
*   eipAddrHelper = load "eip-allocate-associate.groovy"
*   print "eipAddr: " + eipAddrHelper.getEipAddr("eipalloc-64d5890a")
*/
def getEipAddr(String eipAllocId) {
    String eipAddr = sh(
            script: "aws ec2 describe-addresses --allocation-ids ${eipAllocId}" +
                    "|grep PublicIp|cut -d ':' -f2|cut -d '\"' -f2",
            returnStdout: true
    ).trim()
    echo "eipAllocId: ${eipAddr}"
    return eipAddr
}

/**
 ** Function:
 * This function returns the AWS VPC-EC2 private ip Address needing as input argument the "String eipAllocId"
 *
 ** Parameters:
 * @param String eipAllocId    AWS EIP AllocationId [EC2-VPC] The ID that AWS assigns to represent the allocation of
 *                             the Elastic IP address for use with instances in a VPC. Eg: "eipalloc-64d5890a"
 *
 * @return String eipAddrPriv  AWS EC2 private ip address.
 */

/*
** Examples:
* A) Sample usage from a Pipeline Stage (you must include the function in the same groovy script)
*
*  node {
*      stage('AWS EC2 ip address sample') {
*          print "eipAddrPriv: " + getEipAddrPriv("eipalloc-64d5890a")
*      }
*  }
*
*  B) Sample usage as a loaded groovy script
*
*   eipAddrPrivHelper = load "eip-allocate-associate.groovy"
*   print "eipAddrPriv: " + eipAddrPrivHelper.getEipAddrPriv("eipalloc-64d5890a")
*/
def getEipAddrPriv(String eipAllocId) {
    String eipAddrPriv = sh(
            script: "aws ec2 describe-addresses --allocation-ids eipalloc-${eipAllocId}|" +
                    "grep PrivateIpAddress|cut -d ':' -f2|cut -d '\"' -f2",
            returnStdout: true
    ).trim()
    echo "eipAddrPriv: ${eipAddrPriv}"
    return eipAddrPriv
}

/**
 ** Function:
 * This function allocates a new AWS Elastic IP Address after its execution it can return the EIP Allocated id.
 *
 ** Parameters:
 * @return String eipAllocId   AWS EIP AllocationId [EC2-VPC] The ID that AWS assigns to represent the allocation of
 *                             the Elastic IP address for use with instances in a VPC. Eg: "eipalloc-64d5890a"
 */

/*
** Examples:
* A) Sample usage from a Pipeline Stage (you must include the function in the same groovy script)
*
*  node {
*      stage('EIP allocate sample') {
*          print "eipAllocId: " + getEipAllocId()

*      }
*  }
*
*  B) Sample usage as a loaded groovy script
*
*   eipAllocHelper = load "eip-allocate-associate.groovy"
*   print "eipAllocId: " + eipAllocHelper.getEipAllocId()
*/
def getEipAllocId() {
    String eipAllocId = sh(
            script: "aws ec2 allocate-address --domain vpc|grep eipalloc|cut -d ':' -f2|cut -d '-' -f2|cut -d '\"' -f1",
            returnStdout: true
    ).trim()
    echo "eipAllocId: ${eipAllocId}"
    return eipAllocId
}

// Note: this line is crucial when you want to load an external groovy script
return this