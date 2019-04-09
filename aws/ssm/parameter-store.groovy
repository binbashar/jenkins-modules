#!/usr/bin/env groovy
/*
 * Jenkins Modules: AWS SSM Parameter Store helper.
 *
 * Important: this module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that. The module also uses 'jq' to parse JSON output.
 *
 * Important: at this moment this module can only handle values of type String
 * and SecureString.
 */


/*
 * Return a map of parameter names/values.
 * Sample usage:
 *      def allValues = getParameters('/app/env/')
 *      allValues.each { name, value ->
 *          echo name + ': ' + value
 *      }
 */
def getParameters(String paramPrefix, paramTypesList = []) {
    def allParams = [:]

    // By default, we'll process String & SecureString if none is provided
    if (paramTypesList.size() == 0) {
        paramTypesList.add('String')
        paramTypesList.add('SecureString')
    }

    // Go through every param type
    for (paramType in paramTypesList) {
        def paramNames = getParameterNames(paramPrefix, paramType)
        def paramValues = getParameterValues(paramNames, true)
        for (paramVal in paramValues) {
            String name = stripParameterPrefix(paramPrefix, paramVal.key)
            allParams[name] = paramVal.value
        }
    }

    return allParams
}

/*
 * Get parameter name without prefix.
 */
def stripParameterPrefix(String paramPrefix, String rawName) {
    return rawName.replaceAll(paramPrefix, '')
}

/*
 * Return the values that match the given parameter names. Values can be
 * decrypted if specified.
 */
def getParameterValues(ArrayList paramNames, boolean decryptValue = false) {
    def paramValues = [:]

    // Note: ssm get-parameters only processes up to 10 items per call
    def slicedNamesList = sliceList(paramNames, 10)

    // Process each slice of names in order to get their corresponding values
    for (namesSlice in slicedNamesList) {
        // Get the values from SSM
        String names = joinParams(namesSlice)
        def rawValues = ssmGetParameters(names, decryptValue)
        def parsedNameValues = parseJson(rawValues)
        if (parsedNameValues instanceof List) {
            for (nameValue in parsedNameValues) {
                if (nameValue.containsKey('Name') && nameValue.containsKey('Value')) {
                    paramValues[nameValue['Name']] = nameValue['Value']
                }
            }
        }
    }

    return paramValues
}

/*
 * Get the values of the given SSM parameter names.
 */
def ssmGetParameters(String names, boolean decryptValue = false) {
    String ssmCmd = "aws ssm get-parameters --names ${names}" +
        " --query \"Parameters[*].{Name:Name,Value:Value}\"" +
        " --output=json"

    if (decryptValue) {
        ssmCmd += " --with-decryption"
    }

    String paramValues = sh(returnStdout: true, script: ssmCmd).trim()
    return paramValues
}

/*
 * Return a list of parameter names that match the given type and prefix.
 * Important: it was only tested with values of type String and SecureString.
 */
def getParameterNames(String paramPrefix, String paramType) {
    def paramsList = []
    def rawOutput = ssmDescribeParameter(paramPrefix, paramType)

    if (rawOutput != "" && rawOutput != null) {
        def parsedParams = parseJson(rawOutput)
        if (parsedParams instanceof List) {
            for (item in parsedParams) {
                if (item.containsKey('Name')) {
                    paramsList.add(item.Name)
                }
            }
        }
    }

    return paramsList
}

/*
 * Retrieve a list of parameter names that match the given type and prefix.
 */
def ssmDescribeParameter(String paramPrefix, String paramType) {
    String ssmCmd = "aws ssm describe-parameters" +
        " --query \"Parameters[*].{Name:Name}\"" +
        " --parameter-filters \"Key=Type,Values=${paramType}\"" +
        " \"Key=Name,Option=BeginsWith,Values=${paramPrefix}\"" +
        " --output=json"

    String cmdOutput = sh(returnStdout: true, script: ssmCmd).trim()
    return cmdOutput
}

/*
 * Helper used to join a list of strings.
 */
def joinParams(ArrayList params) {
    String names = ""
    for (param in params) {
        names += "\"${param}\" "
    }
    return names
}

/*
 * Slice the given Groovy list by the given slice size. It returns a list of
 * of slices.
 */
def sliceList(ArrayList list, int sliceSize) {
    def listOfSlices = []

    def slice = []
    int listSize = list.size()
    for (int i = 0; i < listSize; i++) {
        slice.add(list.get(i))

        if (((i + 1) % sliceSize) == 0) {
            listOfSlices.add(slice)
            slice = []
            continue
        }

        if (i == (listSize - 1)) {
            listOfSlices.add(slice)
        }
    }

    return listOfSlices
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
    def jsonParser = new groovy.json.JsonSlurper()
    try {
        decodedJson = jsonParser.parseText(jsonString)
    } catch (ex) {
        println "ERROR Could not parse JSON using jsonString=" + jsonString
        println ex
    }
    return decodedJson
}

return this