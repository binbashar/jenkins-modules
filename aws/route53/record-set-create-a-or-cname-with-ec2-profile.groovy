#!/usr/bin/env groovy
/**
 ** Jenkins Modules:
 * Create DNS Record on AWS Route53.
 *
 ** Important:
 * pre-reqs: this module relies on GNU/Linux pkgs libssl-dev, python-dev, python-pip, python2.7
 * and py libraries fabric <= 1.14.1, boto3, pyOpenSSL, decorator to be configured to run without
 * any initial or additional setup.
 *
 * This module handle EC2 profile AWS credentials
 *
 ** Function:
 * This function creates a DNS Record on AWS Route53 having the corresponding arguments being passed.
 *
 ** Parameters:
 * @param String jenkinsModulesPath      Path to Jenkins modules
 * @param String dnsRecordSetComment    DNS record set comment
 * @param String dnsRecordSetType       DNS record set type
 * @param String dnsHostedZoneId        Hosted Zone ID
 * @param String awsRegion                AWS region name
 * @param String dnsRecordSetName       DNS record set name
 * @param String dnsRecordSetValue      DNS record set value
 *
 ** Examples:
 * String rootDir = pwd()
 * String jenkinsModulesPath = "${rootDir}/jenkins_modules"
 * String dnsRecordSetComment = "jenkins.mydomain.com"
 * String dnsHostedZoneId = "/hostedzone/ZDDOABCDZK48"
 * String awsRegion = "us-east-1"
 * String dnsRecordSetName = "jenkins.mydomain.com."
 * String dnsRecordSetValue = "172.20.0.5"
 * String dnsRecordSetType = "A"
 *
 * awsRoute53CreateRecord.extMain(jenkinsModulesPath,dnsRecordSetComment,dnsRecordSetType,dnsHostedZoneId,
 * awsRegion,dnsRecordSetName,dnsRecordSetValue)
 */

def call(String jenkinsModulesPath, String dnsRecordSetComment, String dnsRecordSetType,
         String dnsHostedZoneId, String awsRegion, String dnsRecordSetName, String dnsRecordSetValue) {

    try {
        sh "#!/bin/bash \n" +
                "fab -f ${jenkinsModulesPath}/python/jenkins_dns_aws_route53_ec2_profile.py -R local" +
                " create_resources_record_sets:" +
                "\"${dnsRecordSetName}\"," +
                "\"${dnsRecordSetValue}\"," +
                "\"${dnsRecordSetComment}\"," +
                "\"${dnsRecordSetType}\"," +
                "\"${dnsHostedZoneId}\"," +
                "\"${awsRegion}\""

    } catch (Exception e) {
        echo "[ERROR] Error while running fabric with jenkinsModulesPath=${jenkinsModulesPath}" +
                ", dnsRecordSetComment=${dnsRecordSetComment}" +
                ", dnsRecordSetType=${dnsRecordSetType}" +
                ", dnsHostedZoneId=${dnsHostedZoneId}" +
                ", awsRegion=${awsRegion}" +
                ", dnsRecordSetName=${dnsRecordSetName}" +
                ", dnsRecordSetValue=${dnsRecordSetValue}"
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this