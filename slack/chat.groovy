#! /usr/bin/groovy
/*
 * IMPORTANT: this script relies on HTTP Request Plugin >= v1.8.22
 *
 * Send message to Slack channel.
 * API Spec: https://api.slack.com/methods/chat.postMessage
 *
 * @param String token Slack API token
 * @param String channel Channel name
 * @param String text Text message
 * @param String threadId Thread identifier that can be used for starting a thread
 * @return A Groovy map as specified here: https://api.slack.com/methods/chat.postMessage#response
 *
 * Example:
 *  slackResponse = sendMessage('MY-TOKEN', 'notifications', 'First message')
 *  ...
 *  sendMessage('MY-TOKEN', 'notifications', 'Another message on a thread', slackResponse['ts'])
 */
def sendMessage(String token, String channel, String text, String threadId = null) {
    def jsonBody = "channel=" + java.net.URLEncoder.encode(channel, "UTF-8") +
            "&text=" + java.net.URLEncoder.encode(text, "UTF-8") +
            "&username=Jenkins" +
            "&token=" + token

    if (threadId != null) {
        jsonBody += "&thread_ts=" + threadId
    }

    def response = httpRequest url: "https://slack.com/api/chat.postMessage",
        acceptType: 'APPLICATION_JSON',
        contentType: 'APPLICATION_FORM',
        httpMode: 'POST',
        requestBody: jsonBody

    if (response.status == 200) {
        def jsonParser = new groovy.json.JsonSlurper()
        return jsonParser.parseText(response.content)
    } else {
        println "[ERROR] Unexpected response with status=" + response.status + ", content=" + response.content
    }
    return null
}

return this