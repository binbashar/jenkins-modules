#!/usr/bin/env groovy

/**
 ** Jenkins Modules:
 * git utilities.
 *
 ** IMPORTANT:
 * Most of these functions work on the current directory and will
 * work based on the assumption that there is a git local repository set up.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */

/**
 ** Function:
 * Return the URL of the current repository in HTTPS format.
 *
 ** Parameters:
 * @return String repoUrl   github repository url, eg: https://github.com/binbashar/jenkins-modules
 *
 * NOTE: construction steps
 * 1. git@github.com:binbashar/jenkins-modules.git
 * 2. binbashar/jenkins-modules.git
 * 3. binbashar/jenkins-modules
 * 4. https://github.com/binbashar/jenkins-modules
 */
def getRepositoryUrl() {
    String originUrl = sh(returnStdout: true, script: 'git config --get remote.origin.url').trim()
    // eg: git@github.com:binbashar/jenkins-modules.git
    String repoUrl = originUrl
    if (originUrl.indexOf("git@") == 0) {
        int separatorIndex = originUrl.indexOf(":")
        String repoPath = originUrl.substring(separatorIndex + 1)
        repoPath = repoPath.replace(".git", "")
        repoUrl = "https://github.com/" + repoPath
    }
    return repoUrl
}

/**
 ** Function:
 * Get the current latest single commit hash.
 *
 ** Parameters:
 * @param String additionalArguments    You can pass extra arguments such '--short'
 * @return String currentCommitHash     current latest single commit hash, eg: '0dd359340a407669fe2f1564aa794ddc61da6a1d'
 *
 ** Return Example:
 * $ git log
 * commit 0dd359340a407669fe2f1564aa794ddc61da6a1d
 * Author: exequielrafaela <exequiel.barrirero@binbash.com.ar>
 * Date:   Tue Apr 23 11:38:43 2019 -0300
 *
 *     BBL-56 passbolt and php modules minor updates (wip).
 *
 * $ git rev-parse HEAD
 * 0dd359340a407669fe2f1564aa794ddc61da6a1d
 */
def getCurrentCommitHash(String additionalArguments = '') {
    String gitCmd = "git rev-parse ${additionalArguments} HEAD"
    String currentCommitHash = sh(returnStdout: true, script: gitCmd).trim()
    return currentCommitHash
}

/**
 ** Function:
 * Get the single commit hash that matches the filter provided.
 *
 ** Parameters:
 * @param String tagFilter          String that identifies the tag to be used as filter for the 'git show-ref' cmd,
 *                                  eg: 'v0.0.1' or 'v0.5' among any other version tag your project uses.
 *
 * @return String commitHash        Single commit hash associated with the tagFilter provided, eg: tagFilter == "v0.0.1"
 *                                  then -> commitHash == "987fe51cea0dfe547337c0bde706532795d778ae"
 *
 * Return Example:
 * $ git show-ref | grep v0.0.1
 * 987fe51cea0dfe547337c0bde706532795d778ae refs/tags/v0.0.1
 *
 * $ git show-ref | grep v0.0.1 | tail -n 1 | cut -d' ' -f1
 * 987fe51cea0dfe547337c0bde706532795d778ae
 */
def getCommitHashByTag(String tagFilter) {
    String commitHashCmd = "git show-ref | grep ${tagFilter} | tail -n 1 | cut -d ' ' -f1"
    String commitHash = sh(returnStdout: true, script: commitHashCmd).trim()
    return commitHash
}

/**
 ** Function:
 * Get the latest tag name value.
 *
 ** Parameters:
 * @param String tagFilter          String that identifies the tag to be used as filter for the 'git show-ref' cmd,
 *                                  eg: 'v0.0.1' or 'v0.5' among any other version tag your project uses.
 *
 * @return String tagName           latest tag associated with the tagFilter provided, eg: tagFilter == "v0.0.1"
 *                                  then -> tagName == "v0.0.1"
 *
 * Return Example:
 * $ git show-ref | grep v0.0.1
 * 987fe51cea0dfe547337c0bde706532795d778ae refs/tags/v0.0.1
 *
 * $ git show-ref | grep v0.0.1 | tail -n 1 | cut -d' ' -f2 | cut -d'/' -f3
 * v0.0.1
 */
def getLastTagName(String tagFilter) {
    String tagCmd = "git show-ref | grep ${tagFilter} | tail -n 1 | cut -d ' ' -f2 | cut -d '/' -f3"
    String tagName = sh(returnStdout: true, script: tagCmd).trim()
    return tagName
}

/**
 ** Function:
 * Get a list of diff messages between the given commit hashes. Optionally, you
 * can also limit the diff operation by path.
 *
 ** Parameters:
 * @return String fromCommitHash    Single commit hash from which 'git log' will start bringing logs
 *                                  then -> fromCommitHash == "987fe51cea0dfe547337c0bde706532795d778ae"
 * @return String toCommitHash      Single commit hash where 'git log' will end bringing logs
 *                                  then -> toCommitHash == "0dd359340a407669fe2f1564aa794ddc61da6a1d"
 * @param String pathLimit          repository path to limit the scope of 'git log' command, eg: aws/, aws/ecr, aws/ssm,
 *                                  etc.
 *
 * @return String diffMessages      Multi-line string containing the git log messages based on the input arguments.
 *
 * Return Example:
 * String fromCommitHash = getCommitHashByTag('v0.0.1') -> 987fe51cea0dfe547337c0bde706532795d778ae
 * String toCommitHash   = getCurrentCommitHash()       -> 0dd359340a407669fe2f1564aa794ddc61da6a1d
 * String pathLimit      = "aws/ssm"
 *
 * $ git log --date=local --pretty=format:"%an (%ad): %s %h" 987fe51cea0dfe547337c0bde706532795d778ae..0dd359340a407669fe2f1564aa794ddc61da6a1d -- aws/ssm
 *
 * diffMessages == "exequielrafaela (Tue Apr 23 11:30:13 2019): BBL-62 updating parameter section with return value for aws modules d6cf65e
 *                  exequielrafaela (Tue Apr 16 01:54:50 2019): BBL-56 return values docstring added for several function modules 02dc27f
 *                  ...
 *                  exequielrafaela (Tue Apr 9 18:01:00 2019): BBL-56 several docstring and readme updates a6eed3a"
 */
def getDiffMessages(String fromCommitHash, String toCommitHash, String pathLimit = '') {
    String diffMsgCmd = "git log --date=local --pretty=format:\"%an (%ad): %s %h\" ${fromCommitHash}..${toCommitHash}"

    if (pathLimit != '') {
        diffMsgCmd += " -- " + pathLimit
    }

    String diffMessages = sh(returnStdout: true, script: diffMsgCmd).trim()

    // Replace problematic characters in diff message
    diffMessages = diffMessages.replace('"', '\'')

    return diffMessages
}

/**
 ** Function:
 * Prettify given rawDiffMessages to make them suitable for messaging platforms (mainly Slack).
 *
 ** Parameters:
 * @param String releaseTagFilter   String that identifies the tag to be used eg: 'v0.0.1' or 'v0.5' among any other version
 *                                  tag your project uses.
 * @param String currentCommitHash  Current latest single commit hash, eg: '0dd359340a407669fe2f1564aa794ddc61da6a1d'
 * @param String rawDiffMessages
 *
 * @return String lastChangesMsg    eg: 'Last Changes: <https://github.com/binbashar/jenkins-modules/compare/v0.0.1...d6cf65e6153bed61712be77a58adcfd8cab4a48b>
 *                                  rawDiffMessage1
 *                                  rawDiffMessage2
 *                                  ...
 *                                  rawDiffMessageN'
 */
def getPrettyDiffMessages(String releaseTagFilter, String currentCommitHash, String rawDiffMessages) {
    String previousTagName = getLastTagName(releaseTagFilter)
    String lastChangesUrl = getRepositoryUrl() + "/compare/${previousTagName}...${currentCommitHash}"
    String lastChangesMsg = "Last Changes: <${lastChangesUrl}>  ```${rawDiffMessages}```"
    return lastChangesMsg
}

/*
 * Check if current working directory is a Git repository.
 */
def isRepository() {
    String out = sh(returnStatus: true, script: 'git status')
    return out.toInteger() == 0
}


/**
 ** Function:
 * Tag current release using the given tagPrefix and tagMessage.
 *
 ** Parameters:
 * @param String releaseTagPrefix   prefix for our 'git tag', eg: dev, qa, stg or prod.
 * @param String tagMessage         Message to be added to this release tag.
 *
 * @return NO return value. This call will execute the tagReleaseWithLastChanges() function declared in this module.
 */
def tagRelease(tagPrefix, tagMessage) {
    String currentDeployCommitHash = getCurrentCommitHash()
    String tagDate = sh(returnStdout: true, script: 'date +%Y-%m-%d-%H-%M-%S').trim()
    sh "git tag -a ${tagPrefix}_${tagDate} ${currentDeployCommitHash} -m \"${tagMessage}\""
    sh 'git push --tags'
}

/**
 ** Function:
 * Tag current release using the given releaseTagPrefix, previous tagFilter to compare with to generate the Last Changes
 * Diff Message and an extra tagMessage.
 *
 ** Parameters:
 * @param String releaseTagPrefix   prefix for our 'git tag', eg: dev, qa, stg or prod.
 * @param String tagMessage         Message to be added to this release tag.
 * @param String tagFilter          String that identifies the tag to be used as filter for the 'git show-ref' cmd,
 *                                  eg: 'dev', 'prod', 'v0.0.1' or 'v0.5' among any other version tag your project uses.
 *
 * @return NO return value. This call will execute the tagReleaseWithLastChanges() function declared in this module.
 *
 * Return execution example:
 * $ groovy code-tests/scm_git_tests.groovy
 * releaseTagPrefix_date: v0.0.1_2019-04-23-21-59-53
 * fromCommitHash: 987fe51cea0dfe547337c0bde706532795d778ae
 * currentDeployCommitHash: 6abc66c8b781466ce7dc89143bf6e7b2e78f2437
 * lastChangesDiffMsg: exequielrafaela (Tue Apr 23 11:30:13 2019): BBL-62 updating parameter    ... d6cf65e
 * exequielrafaela (Tue Apr 16 01:54:50 2019): BBL-56 return values docstring added for several ... 02dc27f
 * exequielrafaela (Tue Apr 9 18:01:00 2019): BBL-56 several docstring and readme updates       ... a6eed3a
 *
 * $ gitTagCmd: git tag -a v0.0.1_2019-04-23-21-59-53 6abc66c8b781466ce7dc89143bf6e7b2e78f2437
 * -m 'jenkins_test exequielrafaela (Tue Apr 23 11:30:13 2019): BBL-62 updating ... d6cf65e
 * exequielrafaela (Tue Apr 16 01:54:50 2019): BBL-56 return values             ... 02dc27f
 * exequielrafaela (Tue Apr 9 18:01:00 2019): BBL-56 several docstring          ... a6eed3a'
 *
 * $ git tag -l 'v0.0.1*'
 * v0.0.1
 * v0.0.1_2019-04-23-21-58-03
 *
 * $ git push --tags
 * ...
 * To git@github.com:binbashar/jenkins-modules.git
 *      [new tag]         v0.0.1_2019-04-23-21-58-03 -> v0.0.1_2019-04-23-21-58-03
 */
def tagReleaseWithLastChanges(String releaseTagPrefix, String tagFilter, String tagMessage) {

    String fromCommitHash = getCommitHashByTag(tagFilter)
    String currentDeployCommitHash = getCurrentCommitHash()
    String tagDate = sh(returnStdout: true, script: 'date +%Y-%m-%d-%H-%M-%S').trim()
    String lastChangesDiffMsg = getDiffMessages(fromCommitHash, currentDeployCommitHash)

    println "releaseTagPrefix_date: ${releaseTagPrefix}_${tagDate}"
    println "fromCommitHash: ${fromCommitHash}"
    println "currentDeployCommitHash: ${currentDeployCommitHash}"
    println "lastChangesDiffMsg: ${lastChangesDiffMsg}"
    
    sh "git tag -a ${releaseTagPrefix}_${tagDate} ${currentDeployCommitHash} -m \"${tagMessage} ${lastChangesDiffMsg}\""
    sh "git tag -l '${releaseTagPrefix}*'"
    sh 'git push --tags'
}

/*
 * Similar to getDiffMessages function but it will return a list of objects,
 * each containing datetime, author, message and hash fields.
 */
def getCommitsDelta(fromCommitHash, toCommitHash, pathLimit = "") {
    def commitsDelta = []
    String gitLogCmd = "git log --date=local --pretty=format:\"%ad ///%an ///%s ///%h \" ${fromCommitHash}..${toCommitHash}"

    if (pathLimit != "") {
        gitLogCmd += " -- " + pathLimit
    }

    String gitLogCmdOut = sh(returnStdout: true, script: gitLogCmd).trim()
    if (gitLogCmdOut != "") {
        def lines = gitLogCmdOut.split('\n')
        for (String line in lines) {
            if (line.trim() == "")
                continue

            def fields = line.trim().split('///')
            def commitInfo = [
                    "datetime": fields[0],
                    "author": fields[1],
                    "message": fields[2],
                    "hash": fields[3],
            ]
            commitsDelta.add(commitInfo)
        }
    }
    return commitsDelta
}

// Note: this line is crucial when you want to load an external groovy script
return this