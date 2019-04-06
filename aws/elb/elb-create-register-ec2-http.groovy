#!/usr/bin/env groovy
/*
 * Jenkins Modules: AWS ELB creation and post EC2 registration to it.
 *
 * Important: this module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 *
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
 *   ELB_CREATE_REGISTER = load "jenkins_pipeline-aws_elb_create_register_ec2_http.groovy"
 *   ELB_CREATE_REGISTER.call(subnet-20eal90c,nubi-infra-jenkins-public,sg-2b166e5e,i-0c071000c63b1200d)
 *   print "elb_dns: " + ELB_CREATE_REGISTER.returnElbDns()
 */

/*
 * This function creates a new Elastic Load Balacer (VPC Classic type) and register and existing Ec2 to it so route
 * http (tpc port 80 from public elb request to ec2 80 tcp port), after its execution it can return the Elb DNS
 * public endpoint.
 */

def call(subnet_id,load_balancer_name,sg_id,ec2_id) {

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