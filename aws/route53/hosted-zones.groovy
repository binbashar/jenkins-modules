#!/usr/bin/env groovy

/*
 ** Jenkins Modules:
 * AWS Route53 Hosted Zones helper.
 *
 ** IMPORTANT:
 * This module relies on the AWS CLI to be configured to run without
 * any initial or additional setup. This module DOES NOT handle AWS credentials.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 *
 * Examples:
 * stage ("Delete DNS") {
 *   String stackName = "jenkins"
 *   String hostedZoneId = "Z3OKADJT40HC77"
 *   String domainName = "${stackName}.binbash.com.ar."
 *   String aliasHostedZoneId = "Z3ALOTR6KTTL2"
 *   String aliasDnsName = "internal-kube-ing-lb-asddjrsbs-12345583.us-east-1.elb.amazonaws.com."
 *
 *   // Check if the record set already exists
 *   if (route53Helper.hasRecordSet(hostedZoneId, domainName)) {
 *       // Delete the record set on route 53
 *       def deleteResult = route53Helper.deleteAliasRecordSet(hostedZoneId, domainName, aliasHostedZoneId, aliasDnsName)
 *       println deleteResult
 *
 *       // Let's give it some time for the record to be deleted
 *       sleep 5
 *   } else {
 *       println "[INFO] Record set does not exist with domainName=${domainName}, hostedZoneId=${hostedZoneId}"
 *   }
 * }
 */

/**
 ** Function:
 * Get the hosted zone id for the given domain name.
 *
 ** Parameters:
 * @param String domainName      AWS Route53 resource record set domain name.
 *
 * @return String zoneId        AWS Route53 parsed hosted zone Id. The ID that Amazon Route 53 assigned to the hosted
 *                              zone when you created it without the std output prefix /hostedzone/. eg: 'Z3P5QSUBK4POTI'
 */
def getHostedZoneId(String domainName) {
    String zoneCmd = "aws route53 list-hosted-zones-by-name --dns-name ${domainName} --query 'HostedZones[0].Id'"
    String zoneOutput = sh(returnStdout: true, script: zoneCmd).trim()
    String zoneId = zoneOutput.replaceAll("/hostedzone/", "")
    return zoneId.replaceAll('"', '')
}

/**
 ** Function:
 * Check whether or not the given record set exists in the given hosted zone.
 *
 ** Parameters:
 * @param String hostedZoneId    AWS Route53 hosted zone ID.
 * @param String domainName      AWS Route53 resource record set domain name.
 *
 * @return Boolean              If the AWS Route53 record set exists return true if not false.
 * Ref link: https://docs.aws.amazon.com/cli/latest/reference/route53/list-hosted-zones-by-name.html
 */
def hasRecordSet(String hostedZoneId, String domainName) {
    String recordSet = getRecordSet(hostedZoneId, domainName)
    if (recordSet.indexOf(domainName) >= 0) {
        return true
    }
    return false
}

/**
 ** Function:
 * Get the record set data for the given hosted zone id and domain name.
 *
 * @param String hostedZoneId   AWS Route53 hosted zone ID.
 * @param String domainName     AWS Route53 resource record set domain name.
 *
 * @return String cmdOutput     AWS Route53 record set data for the given hosted zone id and domain name.
 */
def getRecordSet(String hostedZoneId, String domainName) {
    String route53Cmd = "aws route53 list-resource-record-sets --hosted-zone-id ${hostedZoneId}" +
            " --query \"ResourceRecordSets[?Name == '${domainName}']\""
    String cmdOutput = sh(returnStdout: true, script: route53Cmd).trim()
    return cmdOutput
}

/**
 ** Function:
 * Create a record set of type alias in the given hosted zone.
 *
 ** Parameters:
 * @param String hostedZoneId          AWS Route53 hosted zone ID.
 * @param String domainName            AWS Route53 resource record set domain name.
 * @param String aliasHostedZoneId     AWS Route53 alias hosted zone ID.
 * @param String aliasDnsName          AWS Route53 alias resource record set domain name.
 *
 * @return LinkedHashMap parseJson(out)   The returned object is LinkedHashMap from a converted json to a normal Map with
 *                                     String keys or a List of primitives or Map.
 *                                     eg: [ChangeInfo:[Status:PENDING, Comment:optional comment about the changes in
 *                                     this change batch request, SubmittedAt:2018-07-10T19:39:37.757Z,
 *                                     Id:/change/C3QYC83OA0KX5K]]
 */
def createAliasRecordSet(String hostedZoneId, String domainName, String aliasHostedZoneId, String aliasDnsName) {
    return changeRecordSet("CREATE", "A", hostedZoneId, domainName, aliasHostedZoneId, aliasDnsName)
}

/**
 ** Function:
 * Delete a record set of type alias from the given hosted zone.
 *
 ** Parameters:
 * @param String hostedZoneId          AWS Route53 hosted zone ID.
 * @param String domainName            AWS Route53 resource record set domain name.
 * @param String aliasHostedZoneId     AWS Route53 alias hosted zone ID.
 * @param String aliasDnsName          AWS Route53 alias resource record set domain name.
 *
 * @return LinkedHashMap parseJson(out)   The returned object is LinkedHashMap from a converted json to a normal Map with
 *                                     String keys or a List of primitives or Map.
 *                                     eg: [ChangeInfo:[Status:PENDING, Comment:optional comment about the changes in
 *                                     this change batch request, SubmittedAt:2018-07-10T19:39:37.757Z,
 *                                     Id:/change/C3QYC83OA0KX5K]]
 */
def deleteAliasRecordSet(String hostedZoneId, String domainName, String aliasHostedZoneId, String aliasDnsName) {
    return changeRecordSet("DELETE", "A", hostedZoneId, domainName, aliasHostedZoneId, aliasDnsName)
}

/**
 ** Function:
 * Change record set according to the action specified. This is intended to be
 * as generic as possible but keep in mind it only supports the 2 methods above.
 *
 ** Parameters:
 * @param action                        ChangeResourceRecordSets
 *                                      Required Permissions (API Action): route53:ChangeResourceRecordSets
 *                                      Resources: arn:aws:route53:::hostedzone/hosted zone ID
 *
 * @param String recordType            AWS Route53 record set type, eg: A, CNAME, TXT, etc.
 * @param String hostedZoneId          AWS Route53 hosted zone ID.
 * @param String domainName            AWS Route53 resource record set domain name.
 * @param String aliasHostedZoneId     AWS Route53 alias hosted zone ID.
 * @param String aliasDnsName          AWS Route53 alias resource record set domain name.
 *
 * @return LinkedHashMap parseJson(out)   The returned object is LinkedHashMap from a converted json to a normal Map with
 *                                     String keys or a List of primitives or Map.
 * json output:
 * {
 *      "ChangeInfo": {
 *         "Status": "PENDING",
 *         "Comment": "optional comment about the changes in this change batch request",
 *         "SubmittedAt": "2018-07-10T19:39:37.757Z",
 *         "Id": "/change/C3QYC83OA0KX5K"
 *      }
 * }
 *
 * parseJson(out) == [ChangeInfo:[Status:PENDING, Comment:optional comment about the changes in this change batch request,
 *                  SubmittedAt:2018-07-10T19:39:37.757Z, Id:/change/C3QYC83OA0KX5K]]
 */
def changeRecordSet(String action, String recordType, String hostedZoneId, String domainName,
                    String aliasHostedZoneId, String aliasDnsName) {
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
    String cmd = "aws route53 change-resource-record-sets --hosted-zone-id ${hostedZoneId}" +
            " --change-batch '${changeBatch}' --output=json"
    String out = sh(returnStdout: true, script: cmd).trim()
    return parseJson(out)
}

/**
 ** Function:
 * Parse the given JSON encoded string. It uses Jenkins' readJSON utility which is so much better
 * than Groovy's JSONSluper.
 *
 * IMPORTANT:
 * Reads a file in the current working directory or a String as a plain text JSON file.
 * The returned object is a normal Map with String keys or a List of primitives or Map.
 *
 * Ref Link: https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readjson-read-json-from-files-in-the-workspace
 *
 ** Parameters:
 * @param String jsonString    A string containing the JSON formatted data. Data could be access as an array or a map.
 *
 * @return LinkedHashMap decodedJson
 */
def parseJson(String jsonString) {
    def decodedJson = null
    try {
        decodedJson = readJSON text: jsonString
    } catch (ex) {
        println "[ERROR] Unable to parse JSON using jsonString=" + jsonString
        println ex
    }
    return decodedJson
}

// Note: this line is crucial when you want to load an external groovy script
return this