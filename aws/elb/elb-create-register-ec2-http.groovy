#!/usr/bin/env groovy

/*
 ** Jenkins Modules:
 * AWS ELB creation and post EC2 registration to it.
 *
 ** IMPORTANT:
 * This module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 *
 */

/**
 ** Function:
 * This function creates a new Elastic Load Balacer (VPC Classic type) and register and existing Ec2 to it so route
 * http (tpc port 80 from public elb request to ec2 80 tcp port), after its execution it can return the Elb DNS
 * public endpoint.
 *
 ** Parameters:
 * @param String subnetId  AWS VPC subnet id.
 * @param String elbName   AWS EC2 Elastic Load Balancer name.
 * @param String sgId      AWS EC2 Security Group id.
 * @param Stirng ec2Id     AWS EC2 ID.
 *
 * @return String elbDns   DNSName, the DNS name of the load balancer.
 * Ref link: https://docs.aws.amazon.com/cli/latest/reference/elb/create-load-balancer.html
 */

/*
 ** Examples:
 * A) Sample usage from a Pipeline Stage (you must include the function in the same groovy script)
 *
 *  node {
 *      stage('EIP create elb http port and register ec2 Sample') {
 *          print "elbDns: " + call(subnet-20eal90c,nubi-infra-jenkins-public,sg-2b166e5e,i-0c071000c63b1200d)
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   elbCreateRegister = load "elb-create-register-ec2-http.groovy"
 *   elbCreateRegister.call(subnet-20eal90c,nubi-infra-jenkins-public,sg-2b166e5e,i-0c071000c63b1200d)
 *
 *   // or
 *   // We can just run it with "externalCall(...)" since it has a call method.
 *   String elbDns = elbCreateRegister(subnet-20eal90c,nubi-infra-jenkins-public,sg-2b166e5e,i-0c071000c63b1200d)
 *   print "elbDns: " + ${elbDns}
 */
def call(String subnetId, String elbName, String sgId, String ec2Id) {

        // Reference awscli cmd
        //aws elb create-load-balancer --load-balancer-name my-load-balancer
        // --listeners "Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80"
        // --subnets subnet-20eaf60c --security-groups sg-2b177e5e
        String elbDns = sh(
                script: "aws elb create-load-balancer" +
                        " --load-balancer-name ${elbName}" +
                        " --listeners \"Protocol=HTTP,LoadBalancerPort=80,InstanceProtocol=HTTP,InstancePort=80\" " +
                        "--subnets ${subnetId} " +
                        "--security-groups ${sgId}" +
                        "|grep DNSName|cut -d ':' -f2|cut -d '\"' -f2|cut -d '\"' -f1",
                returnStdout: true
        ).trim()
        echo "elbDns: ${elbDns}"

        // Reference awscli cmd
        //aws elb register-instances-with-load-balancer --load-balancer-name my-load-balancer --instances i-d6f6fae3
        sh "aws elb register-instances-with-load-balancer " +
                "--load-balancer-name ${elbName} " +
                "--instances ${ec2Id}"

        sleep 10

        return elbDns
}

// Note: this line is crucial when you want to load an external groovy script
return this