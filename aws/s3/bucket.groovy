#!/usr/bin/env groovy

/*
 ** Jenkins Modules:
 * AWS S3 (Simple Storage Service) helper.
 *
 ** IMPORTANT:
 * This module relies on the AWS CLI to be configured to run without
 * any initial or additional setup.
 *
 * This module handle AWS IAM profile credentials.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
*/

/**
 ** Function:
 * Check whether given bucket exists.
 *
 ** Parameters:
 * @param String name       AWS S3 Bucket name.
 * @param String profile    AWS IAM profile name.
 *
 * @return Boolean          True if S3 bucket exists, False if not.
 */
def hasBucket(String name, String profile = null) {
    if (s3HeadBucket(name, profile) == 0)
        return true
    
    return false
}

/**
 ** Function:
 * Create bucket with given name, on the given region.
 *
 ** Parameters:
 * @param String name       AWS S3 Bucket name
 * @param String profile    AWS IAM profile name.
 * @param String region     AWS Region:
 *                          US East (Ohio):             us-east-2
 *                          US East (N. Virginia):      us-east-1
 *                          US West (N. California):    us-west-1
 *                          US West (Oregon):           us-west-2
 *                          Asia Pacific (Mumbai):  	ap-south-1
 *                          Asia Pacific (Osaka-Local): ap-northeast-3
 *                          Asia Pacific (Seoul):   	ap-northeast-2
 *                          Asia Pacific (Singapore):	ap-southeast-1
 *                          Asia Pacific (Sydney):  	ap-southeast-2
 *                          Asia Pacific (Tokyo):   	ap-northeast-1
 *                          Canada (Central):       	ca-central-1
 *                          China (Beijing):        	cn-north-1
 *                          China (Ningxia):        	cn-northwest-1
 *                          EU (Frankfurt):         	eu-central-1
 *                          EU (Ireland):           	eu-west-1
 *                          EU (London):            	eu-west-2
 *                          EU (Paris):             	eu-west-3
 *                          EU (Stockholm):         	eu-north-1
 *                          South America (São Paulo):	sa-east-1
 *
 * @return String contained in the GroovyMap["key"] then
 *          eg: createBucket == [Location:"http://my-bucket.s3.amazonaws.com/"]
 *              createdBucket["Location"] == "http://my-bucket.s3.amazonaws.com/"
 */
def createBucket(String name, String region, profile = null) {
    def createdBucket = s3CreateBucket(name, region, profile)
    if (createdBucket && createdBucket["Location"]) {
        return createdBucket["Location"]
    }
    return null
}

/**
 ** Function:
 * Delete bucket with the given name and region.
 *
 ** Parameters:
 * @param String name       AWS S3 Bucket name
 * @param String profile    AWS IAM profile name.
 * @param String region     AWS Region: same as in createBucket()
 *
 * @return NO return value. This call will execute the s3DeleteBucket() function included in this module.
 */
def deleteBucket(String name, String region, profile = null) {
    return s3DeleteBucket(name, region, profile)
}

/**
 ** Function:
 * Verifies access to a bucket named as declared in the passed input argument -> String name
 *
 ** IMPORTANT:
 * If the bucket exists and you have access to it, no output is returned.
 * Otherwise, an error message will be shown. For example:
 * 'A client error (404) occurred when calling the HeadBucket operation: Not Found'
 *
 ** Parameters:
 * @param String name       AWS S3 Bucket name.
 * @param String profile    AWS IAM profile name.
 *
 * @return Integer out      True if S3 bucket exists, False if not.
 */
def s3HeadBucket(String name, profile = null) {
    String cmd = "aws s3api head-bucket" +
        " --bucket=${name}"
    
    cmd += (profile) ? " --profile=${profile}" : ""
    
    // Redirect stderr as we need to capture it
    cmd += " 2>&1"
    
    String out = sh(returnStatus: true, script: cmd)
    return out.toInteger()
}


/**
 ** Function:
 * Create bucket with given name, on the given region and return a LinkedHashMap with the output of the
 * 'aws s3api create-bucket' command.
 *
 *
 ** Parameters:
 * @param String name                   AWS S3 Bucket name
 * @param String profile                AWS IAM profile name.
 * @param String region                 AWS Region: same as in createBucket()
 *
 * @return LinkedHashMap parseJson(out)   The returned object is LinkedHashMap from a converted json to a normal Map with
 *                                     String keys or a List of primitives or Map.
 *
 ** return Example:
 * The following command creates a bucket named my-bucket in the eu-west-1 region. Regions outside of us-east-1
 * require the appropriate LocationConstraint to be specified in order to create the bucket in the desired region:
 * $ aws s3api create-bucket --bucket my-bucket --region eu-west-1 --create-bucket-configuration LocationConstraint=eu-west-1
 *
 * json output:
 * {
 *     "Location": "http://my-bucket.s3.amazonaws.com/"
 * }
 *
 * parseJson(out) == [Location:"http://my-bucket.s3.amazonaws.com/"]
 */
def s3CreateBucket(String name, String region, profile = null) {
    String cmd = "aws s3api create-bucket" +
        " --bucket=${name}" +
        " --region=${region}" +
        " --create-bucket-configuration LocationConstraint=${region}" +
        " --acl private"
    
    cmd += (profile) ? " --profile=${profile}" : ""
    
    String out = sh(returnStdout: true, script: cmd).trim()
    return parseJson(out)
}

/**
 ** Function:
 * Delete bucket with the given name and region.
 *
 ** Parameters:
 * @param String name                   AWS S3 Bucket name
 * @param String profile                AWS IAM profile name.
 * @param String region                 AWS Region: same as in createBucket()
 *
 * @return LinkedHashMap parseJson(out)   The returned object is LinkedHashMap from a converted json to a normal Map with
 *                                     String keys or a List of primitives or Map.
 *
 * return Example:
 * The following rb command uses the --force parameter to first remove all of the objects in the bucket and then
 * remove the bucket itself. In this example, the user's bucket is mybucket and the objects in mybucket are
 * test1.txt and test2.txt:
 * aws s3 rb s3://mybucket --force
 *
 * Output:
 *      delete: s3://mybucket/test1.txt
 *      delete: s3://mybucket/test2.txt
 *      remove_bucket: mybucket
 *
 * parseJson(out) == [delete:["s3://mybucket/test1.txt", "s3://mybucket/test2.txt"], remove_bucket:"mybucket"]
 */
def s3DeleteBucket(String name, String region, profile = null) {
    String cmd = "aws s3 rb" +
        " s3://${name}" +
        " --force"
    
    cmd += (profile) ? " --profile=${profile}" : ""
    
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
 *Ref Link: https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readjson-read-json-from-files-in-the-workspace
 *
 ** Parameters:
 * @param String jsonString    A string containing the JSON formatted data. Data could be access as an array or a map.
 *
 * @return LinkedHashMap decodedJson
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

// Note: this line is crucial when you want to load an external groovy script
return this