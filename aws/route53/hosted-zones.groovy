#!/usr/bin/env groovy
/*
 * Jenkins Modules: AWS Route53 Hosted Zones helper.
 *
 * Important: this module relies on the AWS CLI to be configured to run without
 * any initial or additional setup. This module DOES NOT handle AWS credentials.
 */


/*
 * Get the hosted zone id for the given domain name.
 *
 * @param domainName      AWS Route53 resource record set domain name.
 */
def getHostedZoneId(domainName) {
    String zoneCmd = "aws route53 list-hosted-zones-by-name --dns-name ${domainName} --query 'HostedZones[0].Id'"
    String zoneOutput = sh(returnStdout: true, script: zoneCmd).trim()
    String zoneId = zoneOutput.replaceAll("/hostedzone/", "")
    return zoneId.replaceAll('"', '')
}

/*
 * Check whether or not the given record set exists in the given hosted zone.
 *
 * @param hostedZoneId    AWS Route53 hosted zone ID.
 * @param domainName      AWS Route53 resource record set domain name.
 */
def hasRecordSet(hostedZoneId, domainName) {
    String recordSet = getRecordSet(hostedZoneId, domainName)
    if (recordSet.indexOf(domainName) >= 0) {
        return true
    }
    return false
}

/*
 * Get the record set data for the given hosted zone id and domain name.
 *
 * @param hostedZoneId    AWS Route53 hosted zone ID.
 * @param domainName      AWS Route53 resource record set domain name.
 */
def getRecordSet(hostedZoneId, domainName) {
    String route53Cmd = "aws route53 list-resource-record-sets --hosted-zone-id ${hostedZoneId} --query \"ResourceRecordSets[?Name == '${domainName}']\""
    String cmdOutput = sh(returnStdout: true, script: route53Cmd).trim()
    return cmdOutput
}

/*
 * Create a record set of type alias in the given hosted zone.
 *
 * @param hostedZoneId          AWS Route53 hosted zone ID.
 * @param domainName            AWS Route53 resource record set domain name.
 * @param aliasHostedZoneId     AWS Route53 alias hosted zone ID.
 * @param aliasDnsName          AWS Route53 alias resource record set domain name.
 */
def createAliasRecordSet(hostedZoneId, domainName, aliasHostedZoneId, aliasDnsName) {
    return changeRecordSet("CREATE", "A", hostedZoneId, domainName, aliasHostedZoneId, aliasDnsName)
}

/*
 * Delete a record set of type alias from the given hosted zone.
 *
 * @param hostedZoneId          AWS Route53 hosted zone ID.
 * @param domainName            AWS Route53 resource record set domain name.
 * @param aliasHostedZoneId     AWS Route53 alias hosted zone ID.
 * @param aliasDnsName          AWS Route53 alias resource record set domain name.
 */
def deleteAliasRecordSet(hostedZoneId, domainName, aliasHostedZoneId, aliasDnsName) {
    return changeRecordSet("DELETE", "A", hostedZoneId, domainName, aliasHostedZoneId, aliasDnsName)
}

/*
 * Change record set according to the action specified. This is intended to be
 * as generic as possible but keep in mind it only supports the 2 methods above.
 *
 * @param action                ChangeResourceRecordSets
 *                              Required Permissions (API Action): route53:ChangeResourceRecordSets
 *                              Resources: arn:aws:route53:::hostedzone/hosted zone ID
 *
 * @param recordType            AWS Route53 record set type, eg: A, CNAME, TXT, etc.
 * @param hostedZoneId          AWS Route53 hosted zone ID.
 * @param domainName            AWS Route53 resource record set domain name.
 * @param aliasHostedZoneId     AWS Route53 alias hosted zone ID.
 * @param aliasDnsName          AWS Route53 alias resource record set domain name.
 */
def changeRecordSet(action, recordType, hostedZoneId, domainName, aliasHostedZoneId, aliasDnsName) {
    String changeBatch = """
{
  "Changes": [
    {
      "Action": "${action}",
      "ResourceRecordSet": {
        "Name": "${domainName}",
        "Type": "${recordType}",
        "AliasTarget": {
          "HostedZoneId": "${aliasHostedZoneId}",
          "DNSName": "${aliasDnsName}",
          "EvaluateTargetHealth": true
        }
      }
    }
  ]
}
"""
    String cmd = "aws route53 change-resource-record-sets --hosted-zone-id ${hostedZoneId} --change-batch '${changeBatch}' --output=json"
    String out = sh(returnStdout: true, script: cmd).trim()
    return parseJson(out)
}

/*
 * Parse a JSON string. It uses Jenkins' readJSON utility which is so much better
 * than Groovy's JSONSluper.
 */
def parseJson(jsonString) {
    def decodedJson = null
    try {
        decodedJson = readJSON text: jsonString
    } catch (ex) {
        println "[ERROR] Unable to parse JSON using jsonString=" + jsonString
        println ex
    }
    return decodedJson
}

return this