#!/usr/bin/env groovy

// NOTE: File names have '_' instead of '-' because of the issue detailed in the link below:
// https://stackoverflow.com/questions/36461121/groovy-calling-a-method-with-def-parameter-fails-with-illegal-class-name

// TEST-1
String jenkinsModulesPath = ".."
String dnsRecordSetType = "A"
String dnsHostedZoneId = "/hostedzone/ZSQDB826ULKFM"
String awsRegion = "us-east-1"
String awsIamProfile = "bb-shared-oaar"
String dnsRecordSetComment = "recordset-test.binbash.com.ar"
String dnsRecordSetName = "recordset-test.binbash.com.ar."

// Create record func call
String dnsRecordSetValue = "172.16.0.1"
awsRoute53CreateRecordWithProfile(jenkinsModulesPath, dnsRecordSetComment, dnsRecordSetType, dnsHostedZoneId, awsRegion,
                                  dnsRecordSetName, dnsRecordSetValue, awsIamProfile)
sleep 40000
Boolean testResult1 = dnsCheckDomainExists(dnsRecordSetComment)
print "TEST1 = ${testResult1}\n"

// Update record func call
dnsRecordSetValue = "172.16.0.2"
awsRoute53UpdateRecordWithProfile(jenkinsModulesPath, dnsRecordSetComment, dnsRecordSetType, dnsHostedZoneId, awsRegion,
        dnsRecordSetName, dnsRecordSetValue, awsIamProfile)
sleep 10000
Boolean testResult2 = dnsCheckDomainExists(dnsRecordSetComment)
print "TEST2 = ${testResult2}\n"

// Delete record func call
awsRoute53DeleteRecordWithProfile(jenkinsModulesPath, dnsRecordSetComment, dnsRecordSetType, dnsHostedZoneId, awsRegion,
        dnsRecordSetName, dnsRecordSetValue, awsIamProfile)
sleep 50000
Boolean testResult3 = dnsCheckDomainExists(dnsRecordSetComment)
print "TEST3 = ${testResult3}\n"

// INT TEST
if (testResult1 && testResult2 && !testResult3) {
        print "TEST #1 #2 #3 PASSED!!!\n"
} else {
    print "TEST FAILED\n"
}

// FUNCTIONS-1
def awsRoute53CreateRecordWithProfile(jenkinsModulesPath, dnsRecordSetComment, dnsRecordSetType, dnsHostedZoneId,
                                      awsRegion, dnsRecordSetName, dnsRecordSetValue, awsIamProfile) {

    try {

/*        # delivery @ delivery-I7567 in ~/Binbash/repos/BB-Leverage/jenkins/jenkins-modules/code-tests on git:BBL-64-readme-pre-reqs-update-v0.0.2 x [22:50:58]
        $ fab -f ../python/dns/jenkins_dns_aws_route53.py -R local create_resources_record_sets:"bb-shared-oaar","record-set-test.binbash.com.ar.","172.16.0.1","record-set-test.binbash.com.ar","A","/hostedzone/ZSQDB826ULKFM","us-east-1"
        [localhost] Executing task 'create_resources_record_sets'
        Connecting to Route53
        Connecting to Route53

        Route53 Rosource record sets for zone: /hostedzone/ZSQDB826ULKFM
        resource_record_sets_name: record-set-test.binbash.com.ar. does NOT exists!

                SUPPORTED RECORD TYPE and RECORD WILL BE CREATED

        {'ChangeInfo': {'Status': 'PENDING', 'Id': '/change/C1WIA9CBX7ROJB', 'SubmittedAt': datetime.datetime(2019, 5, 2, 1, 51, 1, 985000, tzinfo=tzutc()), 'Comment': 'record-set-test.binbash.com.ar'}, 'ResponseMetadata': {'RetryAttempts': 0, 'HTTPStatusCode': 200, 'HTTPHeaders': {'content-type': 'text/xml', 'content-length': '325', 'x-amzn-requestid': 'bd4f833a-6c7c-11e9-9f6d-7348f71de6ef', 'date': 'Thu, 02 May 2019 01:51:01 GMT'}, 'RequestId': 'bd4f833a-6c7c-11e9-9f6d-7348f71de6ef'}}
        Connecting to Route53

        Route53 Rosource record sets for zone: /hostedzone/ZSQDB826ULKFM
        resource_record_sets_name: record-set-test.binbash.com.ar. EXISTS!

                record set: record-set-test.binbash.com.ar. SUCCESSFULLY CREATED

        Done.*/

/*        String bashCmd = "fab -f ${jenkinsModulesPath}/python/dns/jenkins_dns_aws_route53.py -R local" +
                " create_resources_record_sets:" +
                "\"${awsIamProfile}\"," +
                "\"${dnsRecordSetName}\"," +
                "\"${dnsRecordSetValue}\"," +
                "\"${dnsRecordSetComment}\"," +
                "\"${dnsRecordSetType}\"," +
                "\"${dnsHostedZoneId}\"," +
                "\"${awsRegion}\""
                */

        String bashCmd = "/bin/bash aws_route53_tests_int.sh create_resources_record_sets " +
                "${dnsRecordSetComment} ${jenkinsModulesPath} ${dnsRecordSetType} ${dnsHostedZoneId} ${awsRegion} " +
                "${dnsRecordSetName} ${dnsRecordSetValue} ${awsIamProfile}"
        println "\nbashCmd: ${bashCmd}"
        executeBashCommand(bashCmd)
        return true

    } catch (Exception e) {
        print "[ERROR] Error while running fabric with jenkinsModulesPath=${jenkinsModulesPath}" +
                ", dnsRecordSetComment=${dnsRecordSetComment}" +
                ", dnsRecordSetType=${dnsRecordSetType}" +
                ", dnsHostedZoneId=${dnsHostedZoneId}" +
                ", awsRegion=${awsRegion}" +
                ", dnsRecordSetName=${dnsRecordSetName}" +
                ", dnsRecordSetValue=${dnsRecordSetValue}"

        print "[ERROR] Exception: ${e}"
    }
}

def awsRoute53UpdateRecordWithProfile(String jenkinsModulesPath, String dnsRecordSetComment, String dnsRecordSetType, String dnsHostedZoneId,
                                      String awsRegion, String dnsRecordSetName, String dnsRecordSetValue, String awsIamProfile) {

    try {
/*
        sh "#!/bin/bash \n" +
                "fab -f ${jenkinsModulesPath}/python/dns/jenkins_dns_aws_route53.py -R local" +
                " update_resources_record_sets:" +
                "\"${awsIamProfile}\"," +
                "\"${dnsRecordSetName}\"," +
                "\"${dnsRecordSetValue}\"," +
                "\"${dnsRecordSetComment}\"," +
                "\"${dnsRecordSetType}\"," +
                "\"${dnsHostedZoneId}\"," +
                "\"${awsRegion}\""
*/

        String bashCmd = "/bin/bash aws_route53_tests_int.sh update_resources_record_sets " +
                "${dnsRecordSetComment} ${jenkinsModulesPath} ${dnsRecordSetType} ${dnsHostedZoneId} ${awsRegion} " +
                "${dnsRecordSetName} ${dnsRecordSetValue} ${awsIamProfile}"
        println "bashCmd: ${bashCmd}"
        executeBashCommand(bashCmd)
        return true

    } catch (Exception e) {
        echo "[ERROR] Error while running fabric with jenkinsModulesPath=${jenkinsModulesPath}" +
                ", dnsRecordSetComment=${dnsRecordSetComment}" +
                ", dnsRecordSetType=${dnsRecordSetType}" +
                ", dnsHostedZoneId=${dnsHostedZoneId}" +
                ", awsRegion=${awsRegion}" +
                ", dnsRecordSetName=${dnsRecordSetName}" +
                ", dnsRecordSetValue=${dnsRecordSetValue}"

        echo "[ERROR] Exception: ${e}"
    }
}

def awsRoute53DeleteRecordWithProfile(String jenkinsModulesPath, String dnsRecordSetComment, String dnsRecordSetType, String dnsHostedZoneId,
         String awsRegion, String dnsRecordSetName, String dnsRecordSetValue, String awsIamProfile) {

    try {
/*
        sh "#!/bin/bash \n" +
                "fab -f ${jenkinsModulesPath}/python/dns/jenkins_dns_aws_route53.py -R local" +
                " delete_resources_record_sets:" +
                "\"${awsIamProfile}\"," +
                "\"${dnsRecordSetName}\"," +
                "\"${dnsRecordSetValue}\"," +
                "\"${dnsRecordSetComment}\"," +
                "\"${dnsRecordSetType}\"," +
                "\"${dnsHostedZoneId}\"," +
                "\"${awsRegion}\""
*/

        String bashCmd = "/bin/bash aws_route53_tests_int.sh delete_resources_record_sets " +
                "${dnsRecordSetComment} ${jenkinsModulesPath} ${dnsRecordSetType} ${dnsHostedZoneId} ${awsRegion} " +
                "${dnsRecordSetName} ${dnsRecordSetValue} ${awsIamProfile}"
        println "bashCmd: ${bashCmd}"
        executeBashCommand(bashCmd)
        return true

    } catch (Exception e) {
        echo "[ERROR] Error while running fabric with jenkinsModulesPath=${jenkinsModulesPath}" +
                ", dnsRecordSetComment=${dnsRecordSetComment}" +
                ", dnsRecordSetType=${dnsRecordSetType}" +
                ", dnsHostedZoneId=${dnsHostedZoneId}" +
                ", awsRegion=${awsRegion}" +
                ", dnsRecordSetName=${dnsRecordSetName}" +
                ", dnsRecordSetValue=${dnsRecordSetValue}"

        echo "[ERROR] Exception: ${e}"
    }
}

def dnsCheckDomainExists(String dnsRecordSetName) {

    String lookupResult = ""

    try {
        String bashCmd = "/bin/bash aws_route53_tests_int.sh dnsCheckDomainExists ${dnsRecordSetName}"
        println "lookupResult: ${bashCmd}"
        lookupResult = executeBashCommand(bashCmd)
        print "[DEBUG] nslookup ${dnsRecordSetName} output: ${lookupResult}\n"
        print "[DEBUG] if empty -> OK\n"

    } catch (Exception e) {
        print "[ERROR] Error while running nslookup with domain=${dnsRecordSetName}\n"
        print "[ERROR] Exception=${e}\n"
    }

    // Since the grep expression expects to find the negative case
    // eg: server can't find www.binbash.com.ca: NXDOMAIN), we test for an empty string here
    if (lookupResult == "") {
        print "[DEBUG] nslookup ${dnsRecordSetName} RESOLVING\n"
        return true
    }
    return false
}

def static executeBashCommand(shCmd){
    def proc = shCmd.execute()
    def outputStream = new StringBuffer()
    proc.waitForProcessOutput(outputStream, System.err)
    return outputStream.toString().trim()
}
