#!/usr/bin/env groovy
/*
 * Jenkins Modules: AWS EC2 get ip address.
 *
 * Important: this module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 * A) Sample usage from a Pipeline Stage (you must include the function)
 *
 *  node {
 *      stage('Get EC2 private IP addr Sample') {
 *          print "ec2_priv_ip_addr: " +  call(i-0c071000c63b1200d)
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   EC2_PRIV_IP_ADDR = load "jenkins_pipeline-aws_ec2_get_private_ip.groovy"
 *   print "EC2 private IP addr" + EC2_PRIV_IP_ADDR.call(i-0c071000c63b1200d)
 */

/*
 * This function returns the EC2 private IP address based on the intance id passed as argument
 * @param jenkins_modules_path      Path to Jenkins modules
 */

def call(ec2_id) {

        EC2_PRIV_IP_ADDR = sh(
                script: "aws ec2 describe-instances --instance-ids ${ec2_id} | grep PrivateIpAddress | tail -n 1 | cut -d':' -f2",
                returnStdout: true
                //returnStatus: true
        ).trim()

        echo "EC2_PRIV_IP_ADDR: ${EC2_PRIV_IP_ADDR}"

        return EC2_PRIV_IP_ADDR

}
// Note: this line is crucial when you want to load an external groovy script
return this