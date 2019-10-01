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
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */

/**
 ** Function:
 * K8s helper function to populate a secret.yml manifest with the proper arguments where a LinkedHashMap variable
 * will have a list of [key1:'value1',key2:'value2',...] secrets that could be retrieved from AWS SSM and
 * Hashicorp Vault KV modules.
 * Ref Link: https://kubernetes.io/docs/concepts/configuration/secret/
 *
 ** Parameters:
 * @param String name               K8s secret name.
 * @param String namespace          K8s namespace where your Pod is deployed.
 * @param LinkedHashMap secrets     LinkedHashMap with K8s namespace where your Pod is deployed.
 *
 * @return String template          It contains the the K8s secret manifest in .yml syntax.
 */
def buildSecret(String name, String namespace, def secrets = [:]) {
    String secretsYml = ""
    // eg: secrets == [db_pass_user:'s3cr3t_readonly', db_pass_root:'s3cr3t_root']
    secrets.each { itemName, itemValue ->
        String encodedValue = itemValue.toString().bytes.encodeBase64().toString()
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

/**
 * This is similar to buildSecret function except that it takes a file argument
 * that is typically used to define a filename that will be mounted to a Pod and
 * will hold the values built from the given secrets list.
 * 
 * @param String name               Name of the secret
 * @param String namespace          Namespace of the secret
 * @param LinkedHashMap secrets     A list of key/value pairs that will become the contents of your secrets file
 * @param String file               Name of the file that will hold your secrets
 * @return String                   A Kubernetes secrets manifest
 */
def buildSecretOnFile(String name, String namespace, def secrets = [:], String file = null) {
    String secretsYml = ""
    secrets.each { itemName, itemValue ->
        secretsYml += "" + itemName + ": " + itemValue + "\n"
    }
    secretsYml = secretsYml.bytes.encodeBase64().toString()

    String template = """
apiVersion: v1
kind: Secret
metadata:
  name: ${name}
  namespace: ${namespace}
type: Opaque
data:
  ${file}: ${secretsYml}
"""
    
    return template
}

// Note: this line is crucial when you want to load an external groovy script
return this