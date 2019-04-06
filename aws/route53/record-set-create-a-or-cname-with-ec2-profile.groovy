#!/usr/bin/env groovy
/**
 * Jenkins Modules: Create DNS Record on AWS Route53.
 *
 * This module handle EC2 profile AWS credentials
 *
 * Important
 * pre-reqs: this module relies on GNU/Linux pkgs libssl-dev, python-dev, python-pip, python2.7
 * and py libraries fabric <= 1.14.1, boto3, pyOpenSSL, decorator to be configured to run without
 * any initial or additional setup.
 *
 * Parameters
 * @param jenkins_modules_path      Path to Jenkins modules
 * @param dns_record_set_comment    DNS record set comment
 * @param dns_record_set_type       DNS record set type
 * @param dns_hosted_zone_id        Hosted Zone ID
 * @param aws_region                AWS region name
 * @param dns_record_set_name       DNS record set name
 * @param dns_record_set_value      DNS record set value
 */

def call(jenkins_modules_path, dns_record_set_comment, dns_record_set_type,
         dns_hosted_zone_id, aws_region, dns_record_set_name, dns_record_set_value) {

    try {
        sh "#!/bin/bash \n" +
                "fab -f ${jenkins_modules_path}/python/jenkins_dns_aws_route53_ec2_profile.py -R local" +
                " create_resources_record_sets:" +
                "\"${dns_record_set_name}\"," +
                "\"${dns_record_set_value}\"," +
                "\"${dns_record_set_comment}\"," +
                "\"${dns_record_set_type}\"," +
                "\"${dns_hosted_zone_id}\"," +
                "\"${aws_region}\""

    } catch (Exception e) {
        echo "[ERROR] Error while running fabric with jenkins_modules_path=${jenkins_modules_path}" +
                ", dns_record_set_comment=${dns_record_set_comment}" +
                ", dns_record_set_type=${dns_record_set_type}" +
                ", dns_hosted_zone_id=${dns_hosted_zone_id}" +
                ", aws_region=${aws_region}" +
                ", dns_record_set_name=${dns_record_set_name}" +
                ", dns_record_set_value=${dns_record_set_value}"
    }
}

return this