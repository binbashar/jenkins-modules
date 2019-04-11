#!/usr/bin/env groovy
/*
 ** Jenkins Modules:
 * AWS Route53 Hosted Zones helper.
 *
 ** Important:
 * This module relies on the AWS CLI to be configured to run without
 * any initial or additional setup.
 *
 * This module handle AWS IAM profile credentials.
 *
 * This module has to be load as shown in the root context README.md
 *
 ** Examples:
 * stage ("Delete Queue") {
 *       String queueUrl = sqsHelper.getQueue(queueName, awsJenkinsRole)
 *
 *      if (queueUrl) {
 *           println "[INFO] Queue was found with queueName=${queueName}, queueUrl=${queueUrl}"
 *          def deleteResult = sqsHelper.deleteQueue(queueUrl, awsJenkinsRole)
 *           println "[INFO] Delete queue returned with result=${deleteResult}"
 *       } else {
 *           println "[INFO] Queue was not found with queueName=${queueName}"
 *       }
 *   }
 */

/**
 ** Function:
 * Get AWS SQS queue's main identifier which is its URL.
 *
 ** Parameters:
 * @param String queueName  AWS SQS queue name
 * @param String profile    AWS IAM profile
 */
def getQueue(String queueName, String profile = null) {
    def queuesList = ecrListQueues(queueName, profile)
    
    if (queuesList && queuesList["QueueUrls"]) {
        for (queueUrl in queuesList["QueueUrls"]) {
            if (queueUrl.indexOf(queueName) != -1)
                return queueUrl
        }
    }
    return null
}

/**
 ** Function:
 * Create AWS SQS queue with given name and attributes.
 *
 ** Parameters:
 * @param String queueName  AWS SQS queue name
 * @param String queueAttr  AWS SQS queue attributes (map). A map of attributes to set
 *                          The following lists the names, descriptions, and values of the special request parameters
 *                          that the SetQueueAttributes action uses:
 *                          - DelaySeconds: An integer from 0 to 900 (15 minutes). Default: 0.
 *                          - MaximumMessageSize: An integer from 1,024 bytes (1 KiB) up to 262,144 bytes (256 KiB).
 *                            Default: 262,144 (256 KiB).
 *                          - MessageRetentionPeriod: An integer representing seconds, from 60 (1 minute) to 1,209,600
 *                            (14 days). Default: 345,600 (4 days).
 *                          - Policy: The queue's policy
 *                          - ReceiveMessageWaitTimeSeconds: An integer from 0 to 20 (seconds). Default: 0.
 *                          - RedrivePolicy - The string that includes the parameters for the dead-letter queue
 *                            functionality of the source queue.
 *                          - deadLetterTargetArn - The Amazon Resource Name (ARN) of the dead-letter queue to which
 *                             Amazon SQS moves messages after the value of maxReceiveCount is exceeded.
 *                          - maxReceiveCount - The number of times a message is delivered to the source queue before
 *                            being moved to the dead-letter queue.
 *                          - VisibilityTimeout - The visibility timeout for the queue, in seconds. Valid values: an
 *                            integer from 0 to 43,200 (12 hours). Default: 30.
 *                          - KmsMasterKeyId - The ID of an AWS-managed customer master key (CMK) for Amazon SQS or a
 *                            custom CMK.
 *                          - KmsDataKeyReusePeriodSeconds: An integer representing seconds, between 60 seconds
 *                            (1 minute) and 86,400 seconds (24 hours). Default: 300 (5 minutes).
 *
 *                           The following attribute applies only to FIFO (first-in-first-out) queues :
 *                          - ContentBasedDeduplication - Enables content-based deduplication.
 *                          Ref Link: https://docs.aws.amazon.com/cli/latest/reference/sqs/set-queue-attributes.html
 * @param String profile    AWS IAM profile
 */
def createQueue(String queueName, String queueAttr, String profile = null) {
    def createdQueue = ecrCreateQueue(queueName, queueAttr, profile)
    if (createdQueue && createdQueue["QueueUrl"]) {
        return createdQueue["QueueUrl"]
    }
    return null
}

/**
 ** Function:
 * Delete AWS SQS queue by URL.
 *
 * Parameters:
 * @param String queueUrl   AWS SQS queue URL
 * @param String profile    AWS IAM profile name
 */
def deleteQueue(String queueUrl, String profile = null) {
    return ecrDeleteQueue(queueUrl, profile)
}

/**
 ** Function:
 * Execute List AWS SQS queue's having it's queue name prefix as argument.
 *
 ** Parameters:
 * @param String queueNamePrefix    AWS SQS queue name
 * @param String profile            AWS IAM profile
 */
def ecrListQueues(String queueNamePrefix, String profile = null) {
    String cmd = "aws sqs list-queues" +
        " --queue-name-prefix ${queueNamePrefix}" +
        " --output=json"
    
    cmd += (profile) ? " --profile ${profile}" : ""
    
    String out = sh(returnStdout: true, script: cmd).trim()
    return parseJson(out)
}

/**
 ** Function:
 * Execute creation of AWS SQS queue with given name and attributes.
 *
 ** Parameters:
 * @param String queueName  AWS SQS queue name
 * @param String queueAttr  AWS SQS queue attributes (map). A map of attributes to set
 *                          The following lists the names, descriptions, and values of the special request parameters
 *                          Ref Link: https://docs.aws.amazon.com/cli/latest/reference/sqs/set-queue-attributes.html
 */
def ecrCreateQueue(String queueName, String queueAttr, String profile = null) {
    String cmd = "aws sqs create-queue" +
        " --queue-name ${queueName}" +
        " --attributes '${queueAttr}'"
        " --output=json"
    
    cmd += (profile) ? " --profile ${profile}" : ""
    
    String out = sh(returnStdout: true, script: cmd).trim()
    return parseJson(out)
}

/**
 ** Function:
 * Execute delete AWS SQS queue by URL.
 *
 * Parameters:
 * @param String queueUrl   AWS SQS queue URL
 * @param String profile    AWS IAM profile name
 */
def ecrDeleteQueue(String queueUrl, String profile = null) {
    String cmd = "aws sqs delete-queue" +
        " --queue-url ${queueUrl}" +
        " --output=json"
    
    cmd += (profile) ? " --profile ${profile}" : ""
    
    String out = sh(returnStdout: true, script: cmd).trim()
    return parseJson(out)
}

/**
 ** Function:
 * Parse the given JSON encoded string. It uses Jenkins' readJSON utility which is so much better
 * than Groovy's JSONSluper.
 *
 *Ref Link: https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readjson-read-json-from-files-in-the-workspace
 *
 ** Parameters:
 * @param String jsonString    A string containing the JSON formatted data. Data could be access as an array or a map.
 */
def parseJson(String jsonString) {
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

// Note: this line is crucial when you want to load an external groovy script
return this