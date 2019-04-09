#!/usr/bin/env groovy
/*
 ** Jenkins Modules:
 * AWS ECR helper.
 *
 ** Important:
 * This module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 *
 * This module has to be load as shown in the root context README.md
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
 * @param String repositoryName   AWS ECR repository name
 * @param String imagePrefix      AWS ECR docker image prefix
 *
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

/**
 ** Function:
 * Delete all images in the given list.
 *
 ** Parameters:
 * @param String      repositoryName    AWS ECR repository name
 * @param ArrayList   imagesList        AWS ECR image-names list
 */
def deleteImages(String repositoryName, ArrayList imagesList) {
    def imageIds = []
    for (img in imagesList) {
        imageIds.add("imageTag=" + img["tag"])
    }
    println imageIds
    return ecrDeleteImages(repositoryName, imageIds.join(" "))
}

/**
 ** Function:
 *  Delete all images in the given list.
 *
 ** Parameters:
 * @param String    repositoryName   AWS ECR repository name
 * @param ArrayList imageIds         AWS ECR list of image ID references that correspond to images to delete.
 *                                   The format of the imageIds reference is imageTag=tag or imageDigest=digest.
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
 * @param repositoryName   AWS ECR repository name
 */
def ecrGetImages(repositoryName) {
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
 *Ref Link: https://jenkins.io/doc/pipeline/steps/pipeline-utility-steps/#readjson-read-json-from-files-in-the-workspace
 *
 ** Parameters:
 * @param jsonString    A string containing the JSON formatted data. Data could be access as an array or a map.
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