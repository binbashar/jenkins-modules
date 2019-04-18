#!/usr/bin/env groovy
/**
 ** Jenkins Modules:
 * Module to validate if a DNS record currently exists
 * 
 ** IMPORTANT:
 * This module relies on the GNU/Linux 'nslookup' binary to properly work.
 * Consider:
 *      - Debian based pkg: dnsutils
 *      - RHEL/Centos pkg: bind-utils
 *
 * This module has to be load as shown in the root context README.md
 */

/** 
 ** Function:
 * Check if domain name resolves to a valid DNS record entry.
 *
 ** Parameters:
 * @param String dns_record_set_name   Domain name
 * 
 ** Examples:
 *    // We can just run it with "externalCall(...)" since it has a call method.
 *    boolean dnsDomainExists = dnsNslookupHelper(dns_record_set_comment)
 */

def call(String dns_record_set_name) {
    String lookup_result = ""

    try {
        lookup_result = sh(
            script: "nslookup ${dns_record_set_name} | grep 't find'",
            returnStdout: true
        ).trim()

        echo "[DEBUG] nslookup output: ${lookup_result}"

    } catch (Exception e) {
        echo "[ERROR] Error while running nslookup with domain=${dns_record_set_name}"
        echo "[ERROR] Exception=${e}"
    }

    // Since the grep expression expects to find the negative case, we test for
    // an empty string here
    if (lookup_result == "") {
        return true
    }
    return false
}

// Note: this line is crucial when you want to load an external groovy script
return this