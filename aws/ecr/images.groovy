#!/usr/bin/env groovy
/*
 * Jenkins Modules: AWS ECR helper.
 *
 * Important: this module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that.
 */

/*
 * Get a list of images that match the given prefix.
 *
 * @param repositoryName   AWS ECR repository name
 * @param imagePrefix      AWS ECR docker image prefix
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
 * Delete all images in the given list.
 *
 * @param repositoryName   AWS ECR repository name
 * @param imagePrefix      AWS ECR image-names list
 */
def deleteImages(repositoryName, imagesList) {
    def imageIds = []
    for (img in imagesList) {
        imageIds.add("imageTag=" + img["tag"])
    }
    println imageIds
    return ecrDeleteImages(repositoryName, imageIds.join(" "))
}

/*
 * Delete all images in the given list.
 *
 * @param repositoryName   AWS ECR repository name
 * @param imageIds         AWS ECR list of image ID references that correspond to images to delete.
 *                         The format of the imageIds reference is imageTag=tag or imageDigest=digest.
 */
def ecrDeleteImages(repositoryName, imageIds) {
    String cmd = "aws ecr batch-delete-image" +
        " --repository ${repositoryName}" +
        " --image-ids ${imageIds}"
    String out = sh(returnStdout: true, script: cmd).trim()
    return parseJson(out)
}

/*
 * Get all images in the given AWS ECR repo.
 *
 * @param repositoryName   AWS ECR repository name
 */
def ecrGetImages(repositoryName) {
    String cmd = "aws ecr list-images" +
        " --repository ${repositoryName}" +
        " --output=json"

    String out = sh(returnStdout: true, script: cmd).trim()
    return parseJson(out)
}

/*
 * Parse the given JSON encoded string.
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