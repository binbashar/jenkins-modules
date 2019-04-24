#!/usr/bin/env groovy

import groovy.json.JsonSlurper

/*
 ** Jenkins Modules:
 * AWS SSM Parameter Store helper.
 *
 ** IMPORTANT:
 * This module relies on the AWS CLI to be configured to run as-is
 * (either via AWS EC2 Roles or AWS default credentials), this module does not
 * handle that. The module also uses 'jq' to parse JSON output.
 *
 * At this moment this module can only handle values of type String and SecureString.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */


/**
 ** Function:
 * Return a map of parameter names/values.
 *
 ** Parameters:
 * @param String    paramPrefix     AWS SSM parameter prefix, eg: '/app/env/'
 * @param ArrayList paramTypesList  The type of parameter that you want to add to the system.
 *                                  Items in a StringList must be separated by a comma (,).
 *                                  If you have a parameter value that requires a comma, then use the String data type.
 *                                  Possible values:
 *                                  - String
 *                                  - StringList (NOT YET SUPPORTED)
 *                                  - SecureString
 *
 * @return LinkedHashMap allParams     Map with a Map containing parameter name and value,
 *                                  eg: [unseal_key_1: 123kjaaskl6763, unseal_key_2:1534j2j2k4j3, unseal_key_3:ssf!$3skl6763]
 *
 ** Examples:
 *  A)
 *  def allValues = getParameters('/app/env/')
 *  allValues.each { name, value ->
 *      echo name + ': ' + value
 *  }
 *
 *  B)
 *  stage ("Get Vault Unseal Keys") {
 *          def allParams = parameterStoreHelper.getParameters("/devops/vault/")
 *         // https://jenkins.io/doc/pipeline/steps/credentials-binding/
 *         withCredentials([string(credentialsId: "jenkins-vault-unseal-fake-credentials",
 *                          variable: "jenkinsVaultUnsealFakeCredentials")]) {
 *         sh """
 *               set +x
 *               export VAULT_ADDR='http://0.0.0.0:8200'
 *               vault operator unseal ${allParams['unseal_key_1']}
 *               vault operator unseal ${allParams['unseal_key_2']}
 *               vault operator unseal ${allParams['unseal_key_3']}
 *               # This next line is only to force Jenkins to hide all commands,
 *               # their outputs should be covered by the +x
 *               #echo ${jenkinsVaultUnsealFakeCredentials} > /dev/null
 *            """
 *     }
 *  }
 *
 *  C) getParameters function + /jenkins-modules/-util/file-format.groovy to create a parameters-base.yml file
 *  stage ('Build Parameters File') {
 *      // Fetch all parameters names/values of this app/env from SSM
 *     def allParams = parameterStoreHelper.getParameters('/wordpress/prod/')
 *
 *     // Format all parameters accordingly
 *     String paramsFileData = "parameters: \n"
 *     paramsFileData += fileFormatHelper.mapToYaml(allParams)
 *
 *     // And save it to a temporary file on the workspace
 *     writeFile file: "${WORKSPACE}/parameters-base.yml", text: "${paramsFileData}"
 * }
 *
 *  D) getParameters function for K8s business-intelligence cronjobs and as post-step replace placeholders in ${cronJob}
 *    file with sed command.
 *  stage ('Retrieve Parameters') {
 *     String paramPrefix = "/k8s/cronjobs/bi/"
 *     def cronJobConfigParams = parameterStoreHelper.getParameters(paramPrefix)
 *     println "Found " + cronJobConfigParams.size() + " config parameters"
 *
 *     // Replace all placeholders with values retrieved from param store
 *     cronJobConfigParams.each { paramName, paramValue ->
 *         sh "sed -i \"s/{${paramName}}/${paramValue}/\" ${cronJob}"
 *    }
 *  }
 */
def getParameters(String paramPrefix, ArrayList paramTypesList = []) {
    LinkedHashMap allParams = [:]

    // By default, we'll process String & SecureString if none is provided
    if (paramTypesList.size() == 0) {
        paramTypesList.add('String')
        paramTypesList.add('SecureString')
    }

    // Go through every param type
    for (paramType in paramTypesList) {
        def paramNames = getParameterNames(paramPrefix, paramType as String)
        def paramValues = getParameterValues(paramNames as ArrayList, true)
        for (paramVal in paramValues) {
            String name = stripParameterPrefix(paramPrefix, paramVal.key as String)
            allParams[name] = paramVal.value
        }
    }

    // It returns a LinkedHashMap [:] with al the parameters
    return allParams
}

/**
 ** Function:
 * Get parameter name without prefix.
 *
 * NOTE: static def functionName
 * Methods which may safely be made static . A method may be static if it is not synchronized, it does not reference
 * any of its class' non static methods and non static fields and is not overridden
 * in a sub class.
 *
 ** Parameters:
 * @param String paramPrefix    AWS SSM parameter prefix, eg: '/app/env/'
 * @param String rawName        Parameter value without prefix.
 *
 * @return String rawName       parameter name without prefix. eg: '/app/env/db_pass' to 'db_pass'
 */
static def stripParameterPrefix(String paramPrefix, String rawName) {
    return rawName.replaceAll(paramPrefix, '')
}

/**
 ** Function:
 * Return the values that match the given parameter names. Values can be
 * decrypted if specified.
 *
 ** Parameters:
 * @param ArrayList paramNames      AWS SSM parameters list
 * @param boolean   decryptValue    Decrypting values boolean flag, if Type: SecureString exists in the list you'll need
 *                                  to set it to 'true'
 *
 * @return LinkedHashMap paramValues   A Map with:
 *                                  [paramName1 : 'paramValue1', paramName2 : 'paramValue2', ..., paramNameN : 'paramValueN']
 */
def getParameterValues(ArrayList paramNames, boolean decryptValue = false) {
    // [:] is shorthand notation for creating a Groovy LinkedHashMap. To add keys and values to it then:
    // LinkedHashMap foo = [bar: 'baz', qux: 'quy']
    LinkedHashMap paramValues = [:]

    // Note: ssm get-parameters only processes up to 10 items per call
    ArrayList slicedNamesList = sliceList(paramNames, 10)

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

/**
 ** Function:
 * Get the values of the given SSM parameter names.
 *
 ** Parameters:
 * @param String    names           Names of the parameters for which you want to query information.
 *                                  Syntax: "string" "string" ...
 * @param boolean   decryptValue    Decrypting values boolean flag, if Type: SecureString exists in the list you'll need
 *                                  to set it to 'true'
 *                                  NOTE: This funciton relies on AWS CLI -> aws ssm --with-decryption | --no-with-decryption (boolean)
 *                                  Return decrypted secure string value. Return decrypted values for secure string
 *                                  parameters. This flag is ignored for String and StringList parameter types.
 *                                  Ref Link: https://docs.aws.amazon.com/cli/latest/reference/ssm/get-parameters.html
 *
 * @return String paramValues       String containing a json with the AWS SSM Parameters key and values.
 *
 * Command:
 * aws ssm get-parameters --names key1 key2 --query "Parameters[*].{Name:Name,Value:Value}"
 * Output json:
 *
 * [
 *   {
 *       "Name": "key1",
 *       "Value": "value1"
 *   },
 *   {
 *       "Name": "key2",
 *       "Value": "val"
 *   }
 * ]
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

/**
 ** Function:
 * Return a list of parameter names that match the given type and prefix.
 *
 ** IMPORTANT:
 * It was only tested with values of type String and SecureString.
 *
 ** Parameters:
 * @param String    paramPrefix     AWS SSM parameter prefix, eg: '/app/env/'
 * @param String    paramType       The type of parameter that you want to add to the system.
 *                                  Possible supported values:
 *                                  - String
 *                                  - SecureString
 *
 * @return ArrayList paramsList    ArrayList with the AWS SSM parameters name (not it's value)
 *                                 eg: [unseal_key_1, unseal_key_2, unseal_key_3]
 */
def getParameterNames(String paramPrefix, String paramType) {
    ArrayList paramsList = []
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

/**
 ** Function:
 * Retrieve a list of parameter names that match the given type and prefix.
 *
 ** IMPORTANT:
 * This function relies on AWS CLI
 * Command:
 * aws ssm describe-parameters
 * Output:
 *
 * {
 *   "Parameters": [
 *       {
 *           "LastModifiedUser": "arn:aws:iam::809632081692:user/admin",
 *           "LastModifiedDate": 1487880325.324,
 *           "Type": "String",
 *           "Name": "welcome"
 *       }
 *   ]
 * }
 *
 * To list all Parameters matching specific metadata
 * This example lists all parameters matching a filter.
 * Command:
 * aws ssm describe-parameters --filters "Key=Name,Values=helloWorld"
 *
 ** Parameters:
 * @param String    paramPrefix     AWS SSM parameter prefix, eg: '/app/env/'
 * @param String    paramType       The type of parameter that you want to add to the system.
 *                                  Possible supported values:
 *                                  - String
 *                                  - SecureString
 *
 * @return String   cmdOutput       String containing the json with the parameters details.
 *
 * json output:
 * {
 *   "Parameters": [
 *       {
 *           "LastModifiedUser": "arn:aws:iam::809632081692:user/admin",
 *           "LastModifiedDate": 1487880325.324,
 *           "Type": "SecureString",
 *           "Name": "/app/env/db_pass_root"
 *       },
 *       {
 *           "LastModifiedUser": "arn:aws:iam::809632081692:user/admin",
 *           "LastModifiedDate": 1487880325.324,
 *           "Type": "SecureString",
 *           "Name": "/app/env/db_pass_user"
 *       }
 *   ]
 * }
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

/**
 ** Function:
 * Helper used to join a list of strings from an ArrayList.
 *
 * NOTE: static def functionName
 * Methods which may safely be made static . A method may be static if it is not  synchronized, it does not reference
 * any of its class' non static methods and non static fields and is not overridden
 * in a sub class.
 *
 ** Parameters:
 * @param ArrayList params  List of parameters
 *
 * @return String names     AWS SSM parameter Name
 */
static def joinParams(ArrayList params) {
    String names = ""
    for (param in params) {
        names += "\"${param}\" "
    }
    return names
}

/**
 ** Function:
 * Slice the given Groovy list by the given slice size. It returns a list of of slices.
 *
 * NOTE: static def functionName
 * Methods which may safely be made static . A method may be static if it is not  synchronized, it does not reference
 * any of its class' non static methods and non static fields and is not overridden
 * in a sub class.
 *
 ** Parameters:
 * @param ArrayList list            Groovy ArrayList for our use case it will contain the AWS SSM parameters names
 * @param int       sliceSize       Integer to slice the ArrayList considering our use-case where ssm get-parameters only
 *                                  processes up to 10 items per call
 *
 * @return ArrayList listOfSlices   return a list slice with max 10 items
 */
static def sliceList(ArrayList list, int sliceSize) {
    ArrayList listOfSlices = []

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
 * IMPORTANT:
 * Reads a file in the current working directory or a String as a plain text JSON file.
 * The returned object is a normal Map with String keys or a List of primitives or Map.
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
    def jsonParser = new JsonSlurper()
    try {
        decodedJson = jsonParser.parseText(jsonString)
    } catch (ex) {
        println "ERROR Could not parse JSON using jsonString=" + jsonString
        println ex
    }
    return decodedJson
}

return this