#!/usr/bin/env groovy

/**
 * Jenkins Modules: Update DNS Record on AWS Route53.
 *
 * This module handle EC2 profile AWS credentials
 *
 * IMPORTANT
 * pre-reqs: this module relies on GNU/Linux pkgs libssl-dev, python-dev, python-pip, python2.7
 * and py libraries Fabric3 == 1.13.1.post1, boto3, pyOpenSSL, decorator to be configured to run without
 * any initial or additional setup.
 *
 * This module handle EC2 profile AWS credentials
 *
 ** Function:
 * This function updates a DNS Record on AWS Route53 having the corresponding arguments being passed.
 *
 ** Parameters:
 * @param String jenkinsModulesPath     Path to Jenkins modules
 * @param String dnsRecordSetComment    DNS record set comment
 * @param String dnsRecordSetType       DNS record set type
 * @param String dnsHostedZoneId        Hosted Zone ID
 * @param String awsRegion              AWS region name
 * @param String dnsRecordSetName       DNS record set name
 * @param String dnsRecordSetValue      DNS record set value
 *
 * @return NO return value. This call will execute the stages declared in this module function.
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
 * awsRoute53UpdateRecord.extMain(jenkinsModulesPath,dnsRecordSetComment,dnsRecordSetType,dnsHostedZoneId,
 * awsRegion,dnsRecordSetName,dnsRecordSetValue)
 */

def call(String jenkinsModulesPath, String dnsRecordSetComment, String dnsRecordSetType, String dnsHostedZoneId,
         String awsRegion, String dnsRecordSetName, String dnsRecordSetValue) {

    try {
        sh "#!/bin/bash \n" +
                "fab -f ${jenkinsModulesPath}/python/dns/jenkins_dns_aws_route53_ec2_profile.py -R local" +
                " update_resources_record_sets:" +
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

        echo "[ERROR] Exception: ${e}"
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this