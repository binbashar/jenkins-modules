#!/usr/bin/env groovy

/*
 ** Jenkins Modules:
 * AWS ECR helper.
 *
 ** IMPORTANT:
 * This module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */

/*
** Example:
 *   stage ("Find/remove Image") {
 *       // List all images with matching prefix
 *       String repositoryName = params.repositoryName
 *       String imagePrefix = params.imagePrefix
 *
 *       imagesList = ecrHelper.getImagesByPrefix(repositoryName, imagePrefix)
 *
 *       // Remove any matching images
 *       println "[INFO] Found " + imagesList.size() + " images to delete"
 *       if (imagesList.size() == 0) {
 *           println "[INFO] No images to delete from repository=${repositoryName} with prefix=${imagePrefix}"
 *       } else {
 *           def deleteResult = ecrHelper.deleteImages(repositoryName, imagesList)
 *           println deleteResult
 *       }
 *   }
 */

/**
 ** Function:
 * Get a list of images that match the given prefix.
 *
 ** Parameters:
 * @param String    repositoryName   AWS ECR repository name
 * @param String    imagePrefix      AWS ECR docker image prefix
 *
 * @return ArrayList matches         ArrayList of AWS ECR images tag and digest
 *                                   - imageDigest  -> (string) - The sha256 digest of the image manifest.
 *                                   - imageTag     -> (string) - The tag used for the image.
 */
def getImagesByPrefix(repositoryName, imagePrefix) {
    def matches = []
    def images = ecrGetImages(repositoryName)
    if (images["imageIds"]) {
        for (img in images["imageIds"]) {
            if (img["imageTag"].indexOf(imagePrefix) != -1) {
                matches.add([
                        "tag": img["imageTag"],
                        "digest": img["imageDigest"]
                ])
            }
        }
    }
    return matches
}

/*
 * Get the most recent image from the given repository that matches the given
 * image prefix.
 *
 * @param String repositoryName The name of the repository
 * @param String imagePrefix    An image tag prefix
 * @return String The most recent image tag
 */
def getMostRecentImageTag(String repositoryName, String imagePrefix) {
    String cmd = "aws ecr list-images" +
        " --repository-name ${repositoryName}" +
        " --query 'imageIds[?starts_with(imageTag, `${imagePrefix}`) == `true`]|[].imageTag|sort(@)|[-1]'"
    String imageTag = sh(returnStdout: true, script: cmd).trim()
    return imageTag.replaceAll(/["']/, "")
}

/**
 ** Function:
 * Delete all images in the given list.
 *
 ** Parameters:
 * @param String      repositoryName    AWS ECR repository name
 * @param ArrayList   imagesList        AWS ECR image-names list
 *
 * @return LinkedHashMap from a call to ecrDeleteImages() function
 */
def deleteImages(String repositoryName, ArrayList imagesList) {
    def imageIds = []
    for (img in imagesList) {
        imageIds.add("imageTag=" + img["tag"])
    }
    println imageIds
    return ecrDeleteImages(repositoryName, imageIds.join(" ") as ArrayList)
}

/**
 ** Function:
 *  Delete all images in the given list.
 *
 ** Parameters:
 * @param String    repositoryName   AWS ECR repository name
 * @param ArrayList imageIds         AWS ECR list of image ID references that correspond to images to delete.
 *                                   The format of the imageIds reference is imageTag=tag or imageDigest=digest.
 *
 * @return LinkedHashMap with the output of the 'aws ecr batch-delete-image' aws cli command
 *
 * eg:
 * Deletes an image with the tag precise in a repository called ubuntu in the default registry for an account.
 * Command: aws ecr batch-delete-image --repository-name ubuntu --image-ids imageTag=precise
 * Output:
 *
 * {
 *     "failures": [],
 *     "imageIds": [
 *         {
 *             "imageTag": "precise",
 *             "imageDigest": "sha256:19665f1e6d1e504117a1743c0a3d3753086354a38375961f2e665416ef4b1b2f"
 *         }
 *     ]
 * }
 *
 * returned value -> [imageTag:"precise", imageDigest:"sha256:19665f1e6d1e504117a1743c0a3d3753086354a38375961f2e665416ef4b1b2f"]
 */
def ecrDeleteImages(String repositoryName, ArrayList imageIds) {
    String cmd = "aws ecr batch-delete-image" +
        " --repository ${repositoryName}" +
        " --image-ids ${imageIds}"
    String out = sh(returnStdout: true, script: cmd).trim()
    return parseJson(out)
}

/**
 ** Function:
 *  Get all images in the given AWS ECR repo.
 *
 ** Parameters:
 * @param String repositoryName   AWS ECR repository name
 *
 * @return LinkedHashMap with the list of image IDs for the requested AWS ECR repository.
 * Ref Link: https://docs.aws.amazon.com/cli/latest/reference/ecr/list-images.html
 */
def ecrGetImages(String repositoryName) {
    String cmd = "aws ecr list-images" +
        " --repository ${repositoryName}" +
        " --output=json"

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
 * eg:
 *      def props = readJSON text: '{ "key": "value" }'
 *      assert props['key'] == 'value'
 *      assert props.key == 'value'
 *
 *Ref Link: https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readjson-read-json-from-files-in-the-workspace
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