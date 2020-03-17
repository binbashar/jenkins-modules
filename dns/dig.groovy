#!/usr/bin/env groovy

/*
 ** Dig helper.
 * Use this to resolve domain via dig.
 */

/**
 * Resolve the given domain.
 *
 * @param String domain     The domain you want to resolve
 * @param String recordType The record type you want to query
 * @param String dnsServer  An optional DNS server to use for DNS resolution
 *
 * @return ArrayList A list of records
 */
def resolve(String domain, String recordType = 'a', String dnsServer = '') {
    if (dnsServer.size() > 0 && dnsServer.indexOf('@') == -1)
        dnsServer = "@${dnsServer}"
    
    String cmd = "dig +short ${recordType} ${dnsServer} ${domain}"
    String rawValue = sh(returnStdout: true, script: cmd).trim()
    ArrayList records = rawValue.split("\n")
    return records
}

return this