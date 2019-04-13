#!/usr/bin/env groovy
/**
 * Jenkins Modules: Get the public record of the given ELB name.
 *
 * IMPORTANT: this module relies on the AWS CLI to be configured to run without
 * any initial or additional setup. This module DOES NOT handle AWS credentials.
 *
 * @param elb_name ELB name
 */

def call(elb_name) {
    try {
        elb_describe_result = sh(
                script: "aws elb describe-load-balancers | grep '\"DNSName\": \"${elb_name}' | cut -d':' -f2 | cut -d'\"' -f2",
                returnStdout: true
        ).trim()

        echo "[DEBUG] ELB describe output: ${elb_describe_result}"
        return elb_describe_result

    } catch (Exception e) {
        echo "[ERROR] Error while running elb describe with name=${elb_name}"
        echo "[ERROR] Exception: ${e}"
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this