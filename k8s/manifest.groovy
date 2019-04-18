#!/usr/bin/env groovy
/*
 ** Kubernetes Modules:
 * Build a k8s secret YAML manifest using the given arguments.
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
 * This module has to be load as shown in the root context README.md
 */

/**
 ** Function:
 * K8s helper function to populate a secret.yml manifest with the proper arguments where a Groovy Map variable
 * will have a list of [key1:'value1',key2:'value2',...] secrets that could be retrieved from AWS SSM and
 * Hashicorp Vault KV modules.
 * Ref Link: https://kubernetes.io/docs/concepts/configuration/secret/
 *
 ** Parameters:
 * @param String name       K8s secret name.
 * @param String namespace  K8s namespace where your Pod is deployed.
 * @param Map   secrets     Groovy map with K8s namespace where your Pod is deployed.
 */
def buildSecret(String name, String namespace, def secrets = [:]) {
    String secretsYml = ""
    // eg: secrets == [db_pass_user:'s3cr3t_readonly', db_pass_root:'s3cr3t_root']
    secrets.each { itemName, itemValue ->
        String encodedValue = itemValue.bytes.encodeBase64().toString()
        // eg: secretsYml ==    db_user: s3cr3t_readonly
        //                      db_pass: s3cr3t_root
        secretsYml += "  " + itemName + ": " + encodedValue + "\n"
    }

    String template = """
apiVersion: v1
kind: Secret
metadata:
  name: ${name}
  namespace: ${namespace}
type: Opaque
data:
${secretsYml}
"""
    
    return template
}

return this