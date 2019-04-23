#!/usr/bin/env groovy
// NOTE: File names have '_' instead of '-' because of the issue detailed in the link below:
// https://stackoverflow.com/questions/36461121/groovy-calling-a-method-with-def-parameter-fails-with-illegal-class-name
import groovy.json.JsonSlurper

// TEST
String testResult = getEnvEndpoint("my-env")
if (testResult == "my-env.elasticbeanstalk.com") {
    print "return value: ${testResult} \n"
    print "TEST PASSED!!!\n"
} else {
    print "TEST FAILED\n"
}

// FUNCTION
def getEnvEndpoint(String envName){
    String retvar = null

/*    cmd = "aws elasticbeanstalk describe-environments --environment-names ${envName}" +
            " --profile ${awsProfile} --region ${awsRegion}"

    envDescriptionRaw = sh (
            script: "${cmd}",
            returnStdout: true
    ).trim()*/

    // Json mock for the before commented aws cli cmd
    envDescriptionRaw= '''
{
    "Environments": [
        {
            "ApplicationName": "my-app",
            "EnvironmentName": "my-env",
            "VersionLabel": "7f58-stage-150812_025409",
            "Status": "Ready",
            "EnvironmentId": "e-rpqsewtp2j",
            "EndpointURL": "awseb-e-w-AWSEBLoa-1483140XB0Q4L-109QXY8121.us-west-2.elb.amazonaws.com",
            "SolutionStackName": "64bit Amazon Linux 2015.03 v2.0.0 running Tomcat 8 Java 8",
            "CNAME": "my-env.elasticbeanstalk.com",
            "Health": "Green",
            "AbortableOperationInProgress": false,
            "Tier": {
                "Version": " ",
                "Type": "Standard",
                "Name": "WebServer"
            },
            "DateUpdated": "2015-08-12T18:16:55.019Z",
            "DateCreated": "2015-08-07T20:48:49.599Z"
        }
    ]
}'''

def parsed = new JsonSlurper().parseText( envDescriptionRaw )
retvar = parsed.Environments['CNAME'][0].toString()

return retvar
}