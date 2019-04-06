#!/usr/bin/env groovy

/**
 * Jenkins Modules: git utilities.
 *
 * Important: most of these functions work on the current directory and will
 *  work based on the assumption that there is a git local repository set up.
 */


/*
 * Return the URL of the current repository in HTTPS format.
 */
def getRepositoryUrl() {
    String originUrl = sh(returnStdout: true, script: 'git config --get remote.origin.url').trim()
    String repoUrl = originUrl
    if (originUrl.indexOf("git@") == 0) {
        int separatorIndex = originUrl.indexOf(":")
        String repoPath = originUrl.substring(separatorIndex + 1)
        repoPath = repoPath.replace(".git", "")
        repoUrl = "https://github.com/" + repoPath
    }
    return repoUrl
}

def getCurrentCommitHash() {
    String currentCommitHash = sh(returnStdout: true, script: 'git rev-parse HEAD').trim()
    return currentCommitHash
}

/*
 * Get the single commit hash that matches the filter provided.
 */
def getCommitHashByTag(tagFilter) {
    String commitHashCmd = "git show-ref | grep ${tagFilter} | tail -n 1 | cut -d ' ' -f1"
    String commitHash = sh(returnStdout: true, script: commitHashCmd).trim()
    return commitHash
}

/*
 * Get the name of the last tag.
 */
def getLastTagName(tagFilter) {
    String tagCmd = "git show-ref | grep ${tagFilter} | tail -n 1 | cut -d ' ' -f2 | cut -d '/' -f3"
    String tagName = sh(returnStdout: true, script: tagCmd).trim()
    return tagName
}

/*
 * Get a list of diff messages between the given commit hashes. Optionally, you
 * can also limit the diff operation by path.
 */
def getDiffMessages(fromCommitHash, toCommitHash, pathLimit = '') {
    String diffMsgCmd = "git log --date=local --pretty=format:\"%an (%ad): %s %h\" ${fromCommitHash}..${toCommitHash}"

    if (pathLimit != '') {
        diffMsgCmd += " -- " + pathLimit
    }

    diffMessages = sh(returnStdout: true, script: diffMsgCmd).trim()

    // Replace problematic characters in diff message
    diffMessages = diffMessages.replace('"', '\'')

    return diffMessages
}

/*
 * Prettify given rawDiffMessages to make them suitable for messaging platforms (mainly Slack).
 */
def getPrettyDiffMessages(releaseTagFilter, currentCommitHash, rawDiffMessages) {
    String previousTagName = getLastTagName(releaseTagFilter)
    String lastChangesUrl = getRepositoryUrl() + "/compare/${previousTagName}...${currentCommitHash}"
    String lastChangesMsg = "Last Changes: <${lastChangesUrl}>  ```${rawDiffMessages}```"
    return lastChangesMsg
}

/*
 * Tag current release using the given tagPrefix and tagMessage.
 */
def tag(tagPrefix, tagMessage) {
    String currentDeployCommitHash = getCurrentCommitHash()
    String tagDate = sh(returnStdout: true, script: 'date +%Y-%m-%d-%H-%M-%S').trim()
    sh "git tag -a ${tagPrefix}_${tagDate} ${currentDeployCommitHash} -m \"${tagMessage}\""
    sh 'git push --tags'
}

return this