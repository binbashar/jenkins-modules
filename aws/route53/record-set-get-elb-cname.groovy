#!/usr/bin/env groovy

/**
 ** Jenkins Modules:
 * Get the public record of the given ELB name.
 *
 * IMPORTANT:
 * This module relies on the AWS CLI to be configured to run without any initial or additional setup.
 * This module DOES NOT handle AWS credentials.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 *
 ** Parameters:
 * @param String elbName            AWS ELB name, eg: my-load-balancer
 *
 * @return String elbDescribeResult AWS ELB DNSNmame, The DNS name of the load balancer.
 *                                  eg: my-load-balancer-1234567890.us-west-2.elb.amazonaws.com
 */

def call(String elbName) {
    try {
        String elbDescribeResult = sh(
                script: "aws elb describe-load-balancers " +
                        "| grep '\"DNSName\": \"${elbName}' | cut -d':' -f2 | cut -d'\"' -f2",
                returnStdout: true
        ).trim()

        echo "[DEBUG] ELB describe output: ${elbDescribeResult}"
        return elbDescribeResult

    } catch (Exception e) {
        echo "[ERROR] Error while running elb describe with name=${elbName}"
        echo "[ERROR] Exception: ${e}"
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this