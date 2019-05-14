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
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */

/** 
 ** Function:
 * Check if domain name resolves to a valid DNS record entry.
 *
 ** Parameters:
 * @param String dnsRecordSetName   Domain name
 * 
 * @return Boolean                  true for successful dns domain resolution, false if server can't find domain name.
 * 
 ** Examples:
 *    // We can just run it with "externalCall(...)" since it has a call method.
 *    boolean dnsDomainExists = dnsNslookupHelper(dns_record_set_comment)
 */

def call(String dnsRecordSetName) {
    String lookupResult = ""

    try {
        lookupResult = sh(
            script: "nslookup ${dnsRecordSetName} | grep 't find'",
            returnStdout: true
        ).trim()

        echo "[DEBUG] nslookup ${dnsRecordSetName} output: ${lookupResult}"
        echo "[DEBUG] if output empty -> OK"

    } catch (Exception e) {
        echo "[ERROR] Error while running nslookup with domain=${dnsRecordSetName}"
        echo "[ERROR] Exception=${e}"
    }

    // Since the grep expression expects to find the negative case
    // eg: server can't find www.binbash.com.ca: NXDOMAIN), we test for an empty string here
    if (lookupResult == "") {
        echo "[DEBUG] nslookup ${dnsRecordSetName} RESOLVING"
        return true
    }
    return false
}

// Note: this line is crucial when you want to load an external groovy script
return this