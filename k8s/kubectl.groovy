#!/usr/bin/env groovy
/*
 * Kubernetes Modules: kubectl basic wrapper.
 *
 * IMPORTANT: this module relies on the `kubectl` CLI to be properly configured
 * and ready to use.
 *
 * Dev Notes: the main point of this module is to help reduce code repetition
 * and to provide an interface that is more friendly than that of Jenkins Shell
 * plugin. This module DOES NOT attempt to become an exhaustive helper that
 * provides support to all kubectl commands/subcommands as that would become
 * hard to maintain rather easily.
 */


/*
 * Execute the given command on the given podId. Optionally you can pass a
 * namespace and override any command options you need.
 */
def exec(podId, cmd, namespace = "default", cmdOptions = "-it", context = null) {
    String useContext = (context) ? " --context \"${context}\"" : ""
    String podCmd = """
kubectl exec ${useContext} -n \"${namespace}\" ${cmdOptions} ${podId} -- ${cmd}
"""
    String podOut = sh(returnStdout: true, script: podCmd).trim()
    return podOut
}

/*
 * Get pod name and status for the given pod prefix and optional namespace.
 */
def getPod(podPrefix, namespace = "default", context = null) {
    def podData = [id: '', status: '']
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

/*
 * Get current context.
 */
def getContext() {
    String cmd = 'kubectl config current-context'
    String context = sh(returnStdout: true, script: cmd).trim()
    return context
}

/*
 * Set the given context as the current context.
 */
def setContext(context) {
    String cmd = "kubectl config use-context ${context}"
    String result = sh(returnStdout: true, script: cmd).trim()

    // Kubectl usually indicates the context switch happened through its output
    if (result.indexOf('Switched') >= 0) {
        return true
    }
    return false
}

/*
 * Wait for pod to match the provided target status. The waiting will be bounded
 * to a default timeout that can optionally be specified.
 * IMPORTANT: an exception will be thrown if the timeout is exceeded.
 */
def waitForPod(podPrefix, namespace, targetPodStatus = 'Running', waitTimeout = 30, context = null) {
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

return this