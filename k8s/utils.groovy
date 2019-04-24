#!/usr/bin/env groovy

/*
 ** Kubernetes Modules:
 * Simple set of utility functions to work with Kubernetes.
 *
 ** IMPORTANT:
 * This module relies on the `kubectl` CLI to be properly configured
 * and ready to use.
 *
 ** Dev Notes:
 * The main point of this module is to help reduce code repetition
 * and to provide an interface that is more friendly than that of Jenkins Shell
 * plugin. This module DOES NOT attempt to become an exhaustive helper that
 * provides support to all kubectl commands/sub-commands as that would become
 * hard to maintain rather easily.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */


/**
 ** Function:
 * Simple function to determine the existence of any given object by its name.
 * 
 ** Parameters:
 * @param String objectType     A valid k8s object type such as pod (po), service (svc), namespace (ns), replicaset (rs),
 *                              deployment (deploy), statefulset, daemonset (ds) and jobs (job).
 * @param String objectName     The name of the object which is normally found in its metadata
 *
 * @return Boolean              True if K8s object exists, else false
 *                              eg: True
 *                              // objectType = job & objectName = 'spinnaker-install-using-hal'
 *                              $ kubectl get jobs
 *                              NAME                                    COMPLETIONS   DURATION   AGE
 *                              spinnaker-install-using-hal             1/1           26m        59d
 *                              spinnaker-spinnaker-cleanup-using-hal   1/1           31s        59d
 *                              // function cmd
 *                              $ kubectl get job --context minikube -n spinnaker | grep spinnaker-install-using-hal | wc -l
 *                              1
 */
def hasObject(String objectType, String objectName, String namespace = "default", String context = null) {
    String useContext = (context) ? " --context \"${context}\"" : ""
    String cmd = "kubectl get ${objectType} ${useContext} -n ${namespace} | grep ${objectName} | wc -l"
    String out = runCmd(cmd)
    if (out == "1")
        return true
    return false
}

/**
 ** Function:
 * This function will return the standard output of a command execution that has been passed as argument
 *
 * @param String cmd    Command to be executed
 *
 * @return String with the StdOut of the command passed as argument.
 */
def runCmd(String cmd) {
    return sh(returnStdout: true, script: cmd).trim()
}

// Note: this line is crucial when you want to load an external groovy script
return this