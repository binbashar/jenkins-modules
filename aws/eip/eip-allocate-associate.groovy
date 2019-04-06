#!/usr/bin/env groovy
/*
 * Jenkins Modules: AWS allocate EIP ans associate to EC2.
 *
 * Important: this module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 *
 * A) Sample usage from a Pipeline Stage (you must include the function)
 *
 *  node {
 *      stage('EIP allocate associate Sample') {
 *          call("i-0c0743312321b1200d")
 *          print "eip_alloc_id: " + returnEipAllocId()
 *          print "eip_assoc_id: " + returnEipAssocId()
 *          print "eip_addr: " + returnEipAddr()
 *          print "eip_addr_priv: " + returnEipAddrPriv()
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   EIP_ASSOC_ALLOC = load "jenkins_pipeline-aws_eip_allocate_associate.groovy"
 *   EIP_ASSOC_ALLOC.call("i-0c0743312321b1200d")
 *   print "eip_alloc_id: " + EIP_ASSOC_ALLOC.returnEipAllocId()
 *   print "eip_assoc_id: " + EIP_ASSOC_ALLOC.returnEipAssocId()
 *   print "eip_addr: " + EIP_ASSOC_ALLOC.returnEipAddr()
 *   print "eip_addr_priv: " + EIP_ASSOC_ALLOC.returnEipAddrPriv()
 */

/*
 * This function allocates a new Elastic IP Address and associates with the argument (string) passed: "EC2 id"
 * after its execution it can return the Eip Allocated id, Eip Association Id, the Eip Pubic Address and/or
 * the Eip Private Address.
 */

def eip_alloc_id
def eip_assoc_id
def eip_addr
def eip_addr_priv

def call(instance_id) {

        eip_alloc_id = sh(
                script: "aws ec2 allocate-address --domain vpc|grep eipalloc|cut -d ':' -f2|cut -d '-' -f2|cut -d '\"' -f1",
                returnStdout: true
        ).trim()
        echo "eip_alloc_id: ${eip_alloc_id}"


        eip_assoc_id = sh(
                script: "aws ec2 associate-address --instance-id ${instance_id} --allocation-id eipalloc-${eip_alloc_id}|grep eipassoc|cut -d ':' -f2|cut -d '-' -f2|cut -d '\"' -f1",
                returnStdout: true
        ).trim()
        echo "eip_alloc_id: ${eip_assoc_id}"

        eip_addr = sh(
                script: "aws ec2 describe-addresses --allocation-ids eipalloc-${eip_alloc_id}|grep PublicIp|cut -d ':' -f2|cut -d '\"' -f2",
                returnStdout: true
        ).trim()
        echo "eip_alloc_id: ${eip_addr}"

        eip_addr_priv = sh(
                script: "aws ec2 describe-addresses --allocation-ids eipalloc-${eip_alloc_id}|grep PrivateIpAddress|cut -d ':' -f2|cut -d '\"' -f2",
                returnStdout: true
        ).trim()
        echo "eip_addr_priv: ${eip_addr_priv}"

        sleep 10
}

// Note: this line is crucial when you want to load an external groovy script
return this


def returnEipAllocId() {
    try {
        return eip_alloc_id

    } catch (e) {
        throw e as Throwable
    }
}

def returnEipAssocId() {
    try {
        return eip_assoc_id

    } catch (e) {
        throw e as Throwable
    }
}

def returnEipAddr() {
    try {
        return eip_addr

    } catch (e) {
        throw e as Throwable
    }
}

def returnEipAddrPriv() {
    try {
        return eip_addr_priv

    } catch (e) {
        throw e as Throwable
    }
}