#!/usr/bin/env groovy
/*
 * Build a k8s secret YAML manifest using the given arguments.
 */
def buildSecret(String name, String namespace, def secrets) {
    String secretsYml = ""
    secrets.each { itemName, itemValue ->
        String encodedValue = itemValue.bytes.encodeBase64().toString()
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