#!/usr/bin/env groovy

// NOTE: File names have '_' instead of '-' because of the issue detailed in the link below:
// https://stackoverflow.com/questions/36461121/groovy-calling-a-method-with-def-parameter-fails-with-illegal-class-name

// TEST-1
tagReleaseWithLastChanges("v0.0.1","v0.0.1","jenkins_test" )

// FUNCTION-1
def tagReleaseWithLastChanges(String releaseTagPrefix, String tagFilter, String tagMessage) {

    String fromCommitHash = "987fe51cea0dfe547337c0bde706532795d778ae"
    String currentDeployCommitHash = "6abc66c8b781466ce7dc89143bf6e7b2e78f2437"

//    String tagDate = sh(returnStdout: true, script: 'date +%Y-%m-%d-%H-%M-%S').trim()
    String dateCmd = 'date +%Y-%m-%d-%H-%M-%S'
    String tagDate = executeCurlCommand(dateCmd)

//    String lastChangesDiffMsg = getDiffMessages(fromCommitHash, currentDeployCommitHash)
    lastChangesDiffMsg = "exequielrafaela (Tue Apr 23 11:30:13 2019): BBL-62 updating parameter section with return value for aws modules d6cf65e " +
            "exequielrafaela (Tue Apr 16 01:54:50 2019): BBL-56 return values docstring added for several function modules 02dc27f " +
            "exequielrafaela (Tue Apr 9 18:01:00 2019): BBL-56 several docstring and readme updates a6eed3a"

    // Replace problematic characters in diff message
    lastChangesDiffMsg = lastChangesDiffMsg.replace('"', '\'')

    println "releaseTagPrefix_date: ${releaseTagPrefix}_${tagDate}"
    println "fromCommitHash: ${fromCommitHash}"
    println "currentDeployCommitHash: ${currentDeployCommitHash}"
    println "lastChangesDiffMsg:\n ${lastChangesDiffMsg}"

//    sh "git tag -a ${releaseTagPrefix}_${tagDate} ${currentDeployCommitHash} -m \"${tagMessage} ${lastChangesDiffMsg}\""
    String gitTagCmd = "git tag -a ${releaseTagPrefix}_${tagDate} ${currentDeployCommitHash} -m \"${tagMessage} ${lastChangesDiffMsg}\""
    println "gitTagCmd: ${gitTagCmd}"
    executeCurlCommand(gitTagCmd)

//    sh 'git push --tags'
    String gitTagPush = 'git push --tags'
    executeCurlCommand(gitTagPush)
}

def static executeCurlCommand(shCmd){
    def proc = shCmd.execute()
    def outputStream = new StringBuffer()
    proc.waitForProcessOutput(outputStream, System.err)
    return outputStream.toString().trim()
}