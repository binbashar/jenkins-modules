stage ('Build Parameters File') {
    // Fetch all parameters names/values of this app/env from SSM
    def allParams = parameterStoreHelper.getParameters('/blackmailed/prod/')

    // Format all parameters accordingly
    String paramsFileData = "parameters: \n"
    paramsFileData += fileFormatHelper.mapToYaml(allParams)

    // And save it to a temporary file on the workspace
    writeFile file: "${WORKSPACE}/parameters-base.yml", text: "${paramsFileData}"
}

/////////////////////////////////

stage ('Retrieve Parameters') {
    String paramPrefix = "/stan/cronjobs/${params.targetEnvironment}/"
    cronJobConfigParams = parameterStoreHelper.getParameters(paramPrefix)
    println "Found " + cronJobConfigParams.size() + " config parameters"

    // Replace all placeholders with values retrieved from param store
    cronJobConfigParams.each { paramName, paramValue ->
        sh "sed -i \"s/{${paramName}}/${paramValue}/\" ${cronJob}"
    }
}

/////////////////////////////////

stage ('Build Parameters File') {
    // Fetch all parameters names/values of this app/env from SSM
    def allParams = parameterStoreHelper.getParameters('/ssm_prefix/ssm_env/')

    // Format all parameters accordingly
    String paramsFileData = "parameters: \n"
    paramsFileData += fileFormatHelper.mapToYaml(allParams)

    // And save it to a temporary file on the workspace
    writeFile file: "${WORKSPACE}/parameters-base.yml", text: "${paramsFileData}"
}

/////////////////////////////////

stage ('Build Parameters File') {
    // Fetch all parameters names/values
    String paramsPrefix = '/ssm_prefix/ssm_env/'
    def allParamValues = parameterStoreHelper.getParameterValues(paramsPrefix)

    def newName = ""
    String paramsFileData = "parameters: \n"
    allParamValues.each { name, value ->
        newName = name.replaceAll('"', '')
        paramsFileData += "    " + newName + ': ' + value + "\n"
    }

    writeFile file: "${WORKSPACE}/parameters-base.yml", text: "${paramsFileData}"
}