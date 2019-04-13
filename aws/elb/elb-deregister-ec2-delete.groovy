#!/usr/bin/env groovy
/*
 ** Jenkins Modules: AWS EC2 deregistration from ELB and post ELB deletion.
 *
 ** IMPORTANT: this module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 * This module has to be load as shown in the root context README.md
 *
 */

/**
 ** Function:
 * This function deletes an existingElastic Load Balacer (VPC Classic type) previously deregistering a pre existing Ec2
 * that was attached to this ELB.
 *
 ** Parameters:
 * @param String load_balancer_name     AWS ELB name.
 * @param String ec2_id                 AWS EC2 ID.
 */

/*
 ** Examples:
 * A) Sample usage from a Pipeline Stage (you must include the function)
 *
 *  node {
 *      stage('EIP delete elb http port and deregister ec2  Sample') {
 *          call(nubi-infra-jenkins-public,i-0c071000c63b1200d)
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   elbDeleteRegister = load "jenkins_pipeline-aws_elb_deregister_ec2_delete.groovy"
 *   elbDeleteRegister.call(nubi-infra-jenkins-public,i-0c071000c63b1200d)
 *
 *   // or
 *   // We can just run it with "externalCall(...)" since it has a call method.
 *   elbDeleteRegister(nubi-infra-jenkins-public,i-0c071000c63b1200d)
 */
def call(String load_balancer_name, String ec2_id) {

    // Reference awscli cmd
    //aws elb deregister-instances-with-load-balancer --load-balancer-name my-load-balancer --instances i-d6f6fae3
    sh "aws elb deregister-instances-from-load-balancer " +
            "--load-balancer-name ${load_balancer_name} " +
            "--instances ${ec2_id}"

    // Reference awscli cmd
    //aws elb delete-load-balancer --load-balancer-name my-load-balancer
    sh "aws elb delete-load-balancer --load-balancer-name ${load_balancer_name}"

    sleep 10
}

// Note: this line is crucial when you want to load an external groovy script
return this