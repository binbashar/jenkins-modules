#!/usr/bin/env groovy
/*
 ** Jenkins Modules:
 * AWS ELB creation and post EC2 registration to it.
 *
 ** Important:
 * This module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 * This module has to be load as shown in the root context README.md
 *
 */

/**
 ** Function:
 * This function creates a new Elastic Load Balacer (VPC Classic type) and register and existing Ec2 to it so route
 * http (tpc port 80 from public elb request to ec2 80 tcp port), after its execution it can return the Elb DNS
 * public endpoint.
 *
 ** Parameters:
 * @param String subnet_id            AWS VPC subnet id.
 * @param String load_balancer_name   AWS EC2 Elastic Load Balancer name.
 * @param String sg_id                AWS EC2 Security Group id.
 * @param Stirng ec2_id               AWS EC2 ID.
 */

/*
 ** Examples:
 * A) Sample usage from a Pipeline Stage (you must include the function)
 *
 *  node {
 *      stage('EIP create elb http port and register ec2  Sample') {
 *          call(subnet-20eal90c,nubi-infra-jenkins-public,sg-2b166e5e,i-0c071000c63b1200d)
 *          print "eip_alloc_id: " + returnElbDns()
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   elbCreateRegister = load "jenkins_pipeline-aws_elbCreateRegister_ec2_http.groovy"
 *   elbCreateRegister.call(subnet-20eal90c,nubi-infra-jenkins-public,sg-2b166e5e,i-0c071000c63b1200d)
 *
 *   // or
 *   // We can just run it with "externalCall(...)" since it has a call method.
 *   elbCreateRegister(subnet-20eal90c,nubi-infra-jenkins-public,sg-2b166e5e,i-0c071000c63b1200d)
 *
 *   print "elb_dns: " + elbCreateRegister.returnElbDns()
 */
def call(String subnet_id, String load_balancer_name, String sg_id, String ec2_id) {

        // Reference awscli cmd
        //aws elb create-load-balancer --load-balancer-name my-load-balancer
        // --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80"
        // --subnets subnet-20eaf60c --security-groups sg-2b177e5e
        elb_dns = sh(
                script: "aws elb create-load-balancer" +
                        " --load-balancer-name ${load_balancer_name}" +
                        " --listeners \"Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80\" " +
                        "--subnets ${subnet_id} " +
                        "--security-groups ${sg_id}|grep DNSName|cut -d ':' -f2|cut -d '\"' -f2|cut -d '\"' -f1",
                returnStdout: true
        ).trim()
        echo "elb_dns: ${elb_dns}"

        // Reference awscli cmd
        //aws elb register-instances-with-load-balancer --load-balancer-name my-load-balancer --instances i-d6f6fae3
        sh "aws elb register-instances-with-load-balancer " +
                "--load-balancer-name ${load_balancer_name} " +
                "--instances ${ec2_id}"

        sleep 10

        return elb_dns
}

// Note: this line is crucial when you want to load an external groovy script
return this