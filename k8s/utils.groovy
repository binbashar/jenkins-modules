#!/usr/bin/env groovy
/*
 * Simple set of utility functions to work with Kubernetes.
 * IMPORTANT: it requires a `kubectl` that is set up and working.
 */


/*
 * Simple function to determine the existence of any given object by its name.
 * 
 * @param String objectType A valid k8s object type such as pod, po, service, svc, etc
 * @param String objectName The name of the object which is normally found in its metadata
 * @return Boolean
 */
def hasObject(String objectType, String objectName, String namespace = "default", String context = null) {
    String useContext = (context) ? " --context \"${context}\"" : ""
    String cmd = "kubectl get ${objectType} ${useContext} -n ${namespace} | grep ${objectName} | wc -l"
    String out = runCmd(cmd)
    if (out == "1")
        return true
    return false
}

def runCmd(String cmd) {
    return sh(returnStdout: true, script: cmd).trim()
}

return this