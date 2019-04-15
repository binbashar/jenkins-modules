#!/usr/bin/env groovy
/*
 ** Jenkins Modules:
 * AWS allocate EIP ans associate to EC2.
 *
 ** IMPORTANT:
 * This module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 * This module has to be load as shown in the root context README.md
*/

/**
 ** Function:
 * This function allocates a new Elastic IP Address and associates with the argument (string) passed: "EC2 id"
 * after its execution it can return the Eip Allocated id, Eip Association Id, the Eip Pubic Address and/or
 * the Eip Private Address.
 *
 * Parameters:
 * @param String instanceId    AWS EC2 Identifier
 */

 /*
 ** Examples:
 * A) Sample usage from a Pipeline Stage (you must include the function)
 *
 *  node {
 *      stage('EIP allocate associate Sample') {
 *          call("i-0c0743312321b1200d")
 *          print "eipAllocId: " + returnEipAllocId()
 *          print "eipAssocId: " + returnEipAssocId()
 *          print "eipAddr: " + returnEipAddr()
 *          print "eipAddrPriv: " + returnEipAddrPriv()
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   EIP_ASSOC_ALLOC = load "jenkins_pipeline-aws_eip_allocate_associate.groovy"
 *   EIP_ASSOC_ALLOC.call("i-0c0743312321b1200d")
 *
 *   // or
 *   // We can just run it with "externalCall(...)" since it has a call method.
 *   EIP_ASSOC_ALLOC("i-0c0743312321b1200d")
 *
 *   print "eipAllocId: " + EIP_ASSOC_ALLOC.returnEipAllocId()
 *   print "eipAssocId: " + EIP_ASSOC_ALLOC.returnEipAssocId()
 *   print "eipAddr: " + EIP_ASSOC_ALLOC.returnEipAddr()
 *   print "eipAddrPriv: " + EIP_ASSOC_ALLOC.returnEipAddrPriv()
 */

def eipAllocId
def eipAssocId
def eipAddr
def eipAddrPriv

def call(String instanceId) {

        eipAllocId = sh(
                script: "aws ec2 allocate-address --domain vpc|grep eipalloc|cut -d ':' -f2|cut -d '-' -f2|cut -d '\"' -f1",
                returnStdout: true
        ).trim()
        echo "eipAllocId: ${eipAllocId}"


        eipAssocId = sh(
                script: "aws ec2 associate-address --instance-id ${instanceId} --allocation-id eipalloc-${eipAllocId}|grep eipassoc|cut -d ':' -f2|cut -d '-' -f2|cut -d '\"' -f1",
                returnStdout: true
        ).trim()
        echo "eipAllocId: ${eipAssocId}"

        eipAddr = sh(
                script: "aws ec2 describe-addresses --allocation-ids eipalloc-${eipAllocId}|grep PublicIp|cut -d ':' -f2|cut -d '\"' -f2",
                returnStdout: true
        ).trim()
        echo "eipAllocId: ${eipAddr}"

        eipAddrPriv = sh(
                script: "aws ec2 describe-addresses --allocation-ids eipalloc-${eipAllocId}|grep PrivateIpAddress|cut -d ':' -f2|cut -d '\"' -f2",
                returnStdout: true
        ).trim()
        echo "eipAddrPriv: ${eipAddrPriv}"

        sleep 10
}

// Note: this line is crucial when you want to load an external groovy script
return this


def returnEipAllocId() {
    try {
        return eipAllocId

    } catch (e) {
        throw e as Throwable
    }
}

def returnEipAssocId() {
    try {
        return eipAssocId

    } catch (e) {
        throw e as Throwable
    }
}

def returnEipAddr() {
    try {
        return eipAddr

    } catch (e) {
        throw e as Throwable
    }
}

def returnEipAddrPriv() {
    try {
        return eipAddrPriv

    } catch (e) {
        throw e as Throwable
    }
}