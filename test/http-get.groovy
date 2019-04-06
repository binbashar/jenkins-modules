#! /usr/bin/groovy
/**
 * Jenkins Module: HTTP get returning RC.
 */

/*
 ** Parameters:
 * @param String checkUrl   URL to be tested
 *
 ** Examples:
 * A) Sample usage from a Pipeline Stage(you must include the function)
 *
 *  node {
 *      stage('Https curl request') {
 *           print "https response code: " + call(https://www.myapp.com)
 *
 *      }
 *  }
 *
 *  B) Sample usage as a loaded groovy script
 *
 *   HTTPS_REQ = load "https_curl_request.groovy"
 *   def http_response_code = HTTPS_REQ("https://www.myapp.com")
 *   echo "http response code: " + http_response_code
 *
 * /

/*
 * This function returns the http response code for a http request
 */
def call(String checkUrl){
    // Just perform a GET requst to the provided URL

    try{
        def get = new URL(checkUrl).openConnection();
        def getRC = get.getResponseCode();

        println "URL: ${checkUrl}\tRC: ${getRC}"
        return getRC

    } catch (e) {
        throw e as Throwable
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this