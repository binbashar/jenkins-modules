#!/usr/bin/env groovy

/*
 ** Kubernetes Modules:
 * kubectl basic wrapper.
 *
 ** IMPORTANT:
 * This module relies on the `kubectl` CLI to be properly configured
 * and ready to use.
 *
 ** Dev Notes:
 * The main point of this module is to help reduce code repetition
 * and to provide an interface that is more friendly than that of Jenkins Shell
 * plugin. This module DOES NOT attempt to become an exhaustive helper that
 * provides support to all kubectl commands/subcommands as that would become
 * hard to maintain rather easily.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */

/**
 ** Function:
 * Execute the given command on the given podId. Optionally you can pass a
 * namespace and override any command options you need.
 *
 ** Parameters:
 * @param String podId      K8s Pod ID.
 * @param String cmd        Bash command to be executed in the K8s pod identified by 'podId' argument.
 * @param String namespace  K8s namespace where your Pod is deployed.
 * @param String cmdOptions K8s kubectl cmd options
 *                          Options:
 *                          -c, --container='': Container name. If omitted, the first container in the pod will be chosen
 *                          -i, --stdin=false: Pass stdin to the container
 *                          -t, --tty=false: Stdin is a TTY
 * @param String context    K8s kubectl kubeconfig context (KUBECONFIG=~/.kube/config) to be used.
 *                          useful kubectl context helper cmds:
 *                          $kubectl config get-contexts                # display list of contexts
 *                          $kubectl config current-context             # display the current-context
 *                          $kubectl config use-context my-cluster-name # set the default context to my-cluster-name
 *
 * @return String podOut    Return the standard output of the command executed inside the Pod
 * eg:
 * String podOutCmd = exec("spin-deck-7fb595fdbc-tbdbm", "spinnaker","-it,minikube")
 * // func will exec: kubectl exec --context minikube -n spinnaker -it spin-deck-7fb595fdbc-tbdbm -- pwd
 * podOutCmd == "/opt/deck"
 */
def exec(String podId, String cmd, String namespace = "default", String cmdOptions = "-it", String context = null) {
    // BooleanExpression ?If_True_Use_This_Expression :If_False_Use_This_Expression
    String useContext = (context) ? " --context \"${context}\"" : ""
    String podCmd = """
kubectl exec ${useContext} -n \"${namespace}\" ${cmdOptions} ${podId} -- ${cmd}
"""
    String podOut = sh(returnStdout: true, script: podCmd).trim()
    return podOut
}

/*
 ** Function:
 * Get current K8s kubectl kubeconfig context (KUBECONFIG=~/.kube/config).
 */
def getContext() {
    String cmd = 'kubectl config current-context'
    String context = sh(returnStdout: true, script: cmd).trim()
    return context
}

/**
 ** Function:
 * Get pod name and status for the given pod prefix and optional namespace.
 *
 ** Parameters:
 * @param String podPrefix  K8s Pod name prefix in order to filter with bash grep cmd.
 * @param String namespace  K8s namespace where your Pod is deployed.
 * @param String context    K8s kubectl kubeconfig context (KUBECONFIG=~/.kube/config) to be used.
 *                          useful kubectl context helper cmds:
 *                          $kubectl config get-contexts                # display list of contexts
 *                          $kubectl config current-context             # display the current-context
 *                          $kubectl config use-context my-cluster-name # set the default context to my-cluster-name
 *
 * @return podData          LinkedHashMap containing the 1st K8s podID listed through 'get pods' kubectl cmd as key and
 *                          it's status as value.
 *                          Example:
 *                          // Kubectl Cmd: kubectl get pods --context minikube -n spinnaker| grep spin-deck | head -1 | awk '{print $1, $3}'
 *                          LinkedHashMap returned: [spin-deck-7fb595fdbc-tbdbm:'Running']
 *                          Possible Pod Status:
 *                          - Pending	The Pod has been accepted by the Kubernetes system, but one or more of the Container images has not been created.
 *                          - Running	The Pod has been bound to a node, and all of the Containers have been created.
 *                          - Succeeded	All Containers in the Pod have terminated in success, and will not be restarted.
 *                          - Failed	All Containers in the Pod have terminated, and at least one Container has terminated in failure.
 *                          - Unknown	For some reason the state of the Pod could not be obtained, typically due to an error in communicating with the host of the Pod.
 *                          - Completed	The pod has run to completion as there’s nothing to keep it running eg. Completed Jobs.
 *                          - CrashLoopBackOff	This means that one of the containers in the pod has exited unexpectedly, and perhaps with a non-zero error
 *                          code even after restarting due to restart policy.
 */
def getPod(String podPrefix, String namespace = "default", String context = null) {
    def podData = [id: '', status: '']
    // BooleanExpression ?If_True_Use_This_Expression :If_False_Use_This_Expression
    String useContext = (context) ? " --context \"${context}\"" : ""
    String cmd = """
kubectl get pods ${useContext} -n \"${namespace}\" \
| grep \"${podPrefix}\" \
| head -1 \
| awk '{print \$1,\$3}'
"""
    String podOut = sh(returnStdout: true, script: cmd).trim()

    String[] podValues = podOut.split(" ")
    if (podValues.length > 1) {
        podData['id'] = podValues[0]
        podData['status'] = podValues[1]
    }

    return podData
}

/**
 ** Function:
 * Get job name and status for the given job prefix and namespace.
 *
 ** Parameters:
 * @param String jobPrefix  K8s job name prefix
 * @param String namespace  K8s namespace
 * @param String context    K8s context
 *
 * @return jobData
 */
def getJob(String jobPrefix, String namespace = "default", String context = null) {
    def jobData = [id: '', status: '']
    String useContext = (context) ? " --context \"${context}\"" : ""
    String cmd = """
kubectl get pods ${useContext} -n \"${namespace}\" \
| grep \"${jobPrefix}\" \
| head -1 \
| awk '{print \$1,\$3}'
"""
    String jobOut = sh(returnStdout: true, script: cmd).trim()

    String[] jobValues = jobOut.split(" ")
    if (jobValues.length > 1) {
        jobData['id'] = jobValues[0]
        jobData['status'] = jobValues[1]
    }

    return jobData
}

/**
 ** Function:
 * Get replicaset name and status for the given replicaset prefix and namespace.
 *
 ** Parameters:
 * @param String rsPrefix  K8s replicaset name prefix
 * @param String namespace  K8s namespace
 * @param String context    K8s context
 *
 * @return rsData
 */
def getReplicaSet(String rsPrefix, String namespace = "default", String context = null) {
    def rsData = [id: '', status: '']
    String useContext = (context) ? " --context \"${context}\"" : ""
    String cmd = """
kubectl get pods ${useContext} -n \"${namespace}\" \
| grep \"${rsPrefix}\" \
| head -1 \
| awk '{print \$1,\$3}'
"""
    String rsOut = sh(returnStdout: true, script: cmd).trim()

    String[] rsValues = rsOut.split(" ")
    if (rsValues.length > 1) {
        rsData['id'] = rsValues[0]
        rsData['status'] = rsValues[1]
    }

    return rsData
}

/**
 ** Function:
 *  Set the given K8s kubectl kubeconfig context (KUBECONFIG=~/.kube/config) as the current context.
 *
 ** Parameters:
 * @param String context    K8s kubectl kubeconfig context (KUBECONFIG=~/.kube/config) to be used.
 *
 * @return Boolean          If K8s kubectl context switch successfully happened return true, else return false.
 */
def setContext(String context) {
    String cmd = "kubectl config use-context ${context}"
    String result = sh(returnStdout: true, script: cmd).trim()

    // Kubectl usually indicates the context switch happened through its output
    if (result.indexOf('Switched') >= 0) {
        return true
    }
    return false
}

/**
 ** Function:
 * Wait for pod to match the provided target status. The waiting will be bounded
 * to a default timeout that can optionally be specified.
 *
 ** IMPORTANT: an exception will be thrown if the timeout is exceeded.
 *
 ** Parameters:
 * @param String podPrefix          K8s Pod name prefix in order to filter with bash grep cmd.
 * @param String namespace          K8s namespace where your Pod is deployed.
 * @param String targetPodStatus    K8s Pod status (defaults to 'Running')
 *                                  - Pending	The Pod has been accepted by the Kubernetes system, but one or more of the Container images has not been created.
 *                                  - Running	The Pod has been bound to a node, and all of the Containers have been created.
 *                                  - Succeeded	All Containers in the Pod have terminated in success, and will not be restarted.
 *                                  - Failed	All Containers in the Pod have terminated, and at least one Container has terminated in failure.
 *                                  - Unknown	For some reason the state of the Pod could not be obtained, typically due to an error in communicating with the host of the Pod.
 *                                  - Completed	The pod has run to completion as there’s nothing to keep it running eg. Completed Jobs.
 *                                  - CrashLoopBackOff	This means that one of the containers in the pod has exited unexpectedly, and perhaps with a non-zero error
 *                                    code even after restarting due to restart policy.
 * @param String context            K8s kubectl kubeconfig context (KUBECONFIG=~/.kube/config) to be used.
 *                                  useful kubectl context helper cmds:
 *                                  $kubectl config get-contexts                # display list of contexts
 *                                  $kubectl config current-context             # display the current-context
 *                                  $kubectl config use-context my-cluster-name # set the default context to my-cluster-name
 * @param Integer waitTimeout       Integer value of seconds to wait for a K8s Pod to be in Running state.
 *
 * @return LinkedHashMap pod        Contains the 1st K8s podID listed through 'get pods' kubectl cmd as key and
 *                                  it's status as value which should be 'Running' after the wait for pod period
 *                                  eg: [spin-deck-7fb595fdbc-tbdbm:'Running']
 */
def waitForPod(String podPrefix, String namespace, String targetPodStatus = 'Running', Integer waitTimeout = 30,
               String context = null) {
    // LinkedHashMap returned by getPod() function. eg: [spin-deck-7fb595fdbc-tbdbm:'Running']
    def pod = getPod(podPrefix, namespace, context)
    if (pod.status != targetPodStatus) {
        timeout(time: waitTimeout, unit: 'SECONDS') {
            waitUntil {
                pod = getPod(podPrefix, namespace, context)
                println "=> Waiting for pod to become available using status=${pod.status}"
                return pod.status == targetPodStatus
            }
        }
    }

    return pod
}

/*
 * Get the full data about any k8s object.
 */
def getObject(objectType, objectName, namespace = "default", context = null) {
    String useContext = (context) ? " --context \"${context}\"" : ""

    String cmd = """
kubectl get ${objectType} ${useContext} \
    -n \"${namespace}\" \
    -o json \
    --ignore-not-found \
    ${objectName}
"""
    String podOut = sh(returnStdout: true, script: cmd).trim()
    return parseJson(podOut)
}

/*
 * Check whether the given object exists.
 */
def checkObject(objectType, objectName, namespace = "default", context = null) {
    def obj = getObject(objectType, objectName, namespace, context)
    return obj != null
}

/*
 * Parse JSON string.
 */
def parseJson(jsonString) {
    def decodedJson = null
    if (jsonString != null) {
        try {
            decodedJson = readJSON text: jsonString
        } catch (ex) {
            println "[WARN] Unable to parse JSON using jsonString=" + jsonString
            println ex
        }
    }
    return decodedJson
}

// Note: this line is crucial when you want to load an external groovy script
return this