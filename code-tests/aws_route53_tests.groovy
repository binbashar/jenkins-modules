#!/usr/bin/env groovy

// NOTE: File names have '_' instead of '-' because of the issue detailed in the link below:
// https://stackoverflow.com/questions/36461121/groovy-calling-a-method-with-def-parameter-fails-with-illegal-class-name
import groovy.json.JsonSlurper

// TEST-1
String testResult1 = getHostedZoneId("www.example.com")
if (testResult1 == "Z119WBBTVP5WFX") {
    print "\nreturn value: ${testResult1} \n"
    print "TEST PASSED!!!\n"
} else {
    print "TEST FAILED\n"
}

// FUNCTION-1
def getHostedZoneId(String domainName) {
/*
    String zoneCmd = "aws route53 list-hosted-zones-by-name --dns-name ${domainName} --query 'HostedZones[0].Id'"
    String zoneOutput = sh(returnStdout: true, script: zoneCmd).trim()
*/

    // Json mock for the before commented aws cli cmd
    String zoneCmd = '''
{
  "HostedZones": [
      {
          "ResourceRecordSetCount": 2,
          "CallerReference": "test20150527-2",
          "Config": {
              "Comment": "test2",
              "PrivateZone": false
          },
          "Id": "/hostedzone/Z119WBBTVP5WFX",
          "Name": "2.example.com."
      },
      {
          "ResourceRecordSetCount": 2,
          "CallerReference": "test20150527-1",
          "Config": {
              "Comment": "test",
              "PrivateZone": false
          },
          "Id": "/hostedzone/Z3P5QSUBK4POTI",
          "Name": "www.example.com."
      }
  ],
  "IsTruncated": false,
  "MaxItems": "100"
}'''

    def parsed = new JsonSlurper().parseText( zoneCmd )
    zoneOutput = parsed.HostedZones['Id'][0].toString()
    print "zoneOutput: $zoneOutput"

    String zoneId = zoneOutput.replaceAll "/hostedzone/", ""
    return zoneId.replaceAll('"', '')
}