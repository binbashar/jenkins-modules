#!/usr/bin/env groovy
/*
 * Jenkins Modules: AWS Route53 Hosted Zones helper.
 *
 * Important: this module relies on the AWS CLI to be configured to run without
 * any initial or additional setup.
 *
 * This module handle AWS IAM profile credentials.
 */


/*
 * Get Queue's main identifier which is its URL.
 */
def getQueue(queueName, profile = null) {
    def queuesList = ecrListQueues(queueName, profile)
    
    if (queuesList && queuesList["QueueUrls"]) {
        for (queueUrl in queuesList["QueueUrls"]) {
            if (queueUrl.indexOf(queueName) != -1)
                return queueUrl
        }
    }
    return null
}

/*
 * Create queue with given name and attributes.
 */
def createQueue(queueName, queueAttr, profile = null) {
    def createdQueue = ecrCreateQueue(queueName, queueAttr, profile)
    if (createdQueue && createdQueue["QueueUrl"]) {
        return createdQueue["QueueUrl"]
    }
    return null
}

/*
 * Delete queue by URL.
 */
def deleteQueue(queueUrl, profile = null) {
    return ecrDeleteQueue(queueUrl, profile)
}

def ecrListQueues(queueNamePrefix, profile = null) {
    String cmd = "aws sqs list-queues" +
        " --queue-name-prefix ${queueNamePrefix}" +
        " --output=json"
    
    cmd += (profile) ? " --profile ${profile}" : ""
    
    String out = sh(returnStdout: true, script: cmd).trim()
    return parseJson(out)
}

def ecrCreateQueue(queueName, queueAttr, profile = null) {
    String cmd = "aws sqs create-queue" +
        " --queue-name ${queueName}" +
        " --attributes '${queueAttr}'"
        " --output=json"
    
    cmd += (profile) ? " --profile ${profile}" : ""
    
    String out = sh(returnStdout: true, script: cmd).trim()
    return parseJson(out)
}

def ecrDeleteQueue(queueUrl, profile = null) {
    String cmd = "aws sqs delete-queue" +
        " --queue-url ${queueUrl}" +
        " --output=json"
    
    cmd += (profile) ? " --profile ${profile}" : ""
    
    String out = sh(returnStdout: true, script: cmd).trim()
    return parseJson(out)
}

/*
 * Parse a JSON string. It uses Jenkins' readJSON utility which is so much better
 * than Groovy's JSONSluper.
 */
def parseJson(jsonString) {
    def decodedJson = null
    if (jsonString) {
        try {
            decodedJson = readJSON text: jsonString
        } catch (ex) {
            println "[ERROR] Unable to parse JSON using jsonString=" + jsonString
            println ex
        }
    }
    return decodedJson
}

return this