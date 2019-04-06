#!/usr/bin/env groovy
/**
 * Jenkins Module: Slack notification utilities.
 */

/*
 * Send message with given color.
 *
 *  @param message      Text message
 *  @param colorCode    Hexadecimal color code
 */
def send(message, colorCode) {
    colorCode = colorCode ?: '#000000'
    message = message ?: ''

    if (message != '') {
        slackSend(color: colorCode, message: message)
    }
}

/*
 * Send build status message using predefined template.
 *
 *  @param message      Text message
 *  @param colorCode    Hexadecimal color code
 */
def sendBuildStatus(status, secondaryMessage = "") {
    def color = getColorByBuildStatus(status)
    def message = "${status}: Job ${env.JOB_NAME} <${env.BUILD_URL}|#${env.BUILD_NUMBER}>. "
    def lastChanges = "\n Last Changes: (${env.BUILD_URL}last-changes/)" as Object

    message += lastChanges
    message += secondaryMessage

    send(message, color)
}

def getColorByBuildStatus(status) {
    String redColor = '#DC3545'
    String greenColor = '#A9D071'
    String blueColor = '#3C8FD3'

    String colorCode = '#000000'
    if (status == 'STARTED') {
        colorCode = blueColor
    } else if (status == 'SUCCESS') {
        colorCode = greenColor
    } else if (status == 'FAILURE') {
        colorCode = redColor
    }

    return colorCode
}

return this