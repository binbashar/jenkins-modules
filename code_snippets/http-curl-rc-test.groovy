stage("Validating ${API_STATUS_URL} API STATUS"){
    try{
        API_STATUS = sh(
                script: "curl -o /dev/null --silent --head --write-out '%{http_code}\n' ${API_STATUS_URL} | grep 200",
                returnStdout: true
        ).trim()
    } catch (e) {
        // If there was an exception thrown, the build failed
        throw e as Throwable
    }

    if(API_STATUS == "200"){
        echo "API HTTP RESPONSE CODE ${API_STATUS}"
    } else {
        currentBuild.result = "FAILED"
    }
}