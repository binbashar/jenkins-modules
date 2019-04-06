#!/usr/bin/env groovy
/**
 * Check if domain name resolves to a valid record entry.
 *
 * @param dns_record_set_name   Domain name
 */

def call(dns_record_set_name) {
    String lookup_result = ""

    try {
        lookup_result = sh(
            script: "nslookup ${dns_record_set_name} | grep 't find'",
            returnStdout: true
        ).trim()

        echo "[DEBUG] nslookup output: ${lookup_result}"

    } catch (Exception e) {
        echo "[ERROR] Error while running nslookup with domain=${dns_record_set_name}"
    }

    // Since the grep expression expects to find the negative case, we test for
    // an empty string here
    if (lookup_result == "") {
        return true
    }
    return false
}

return this