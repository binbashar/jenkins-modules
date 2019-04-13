#!/usr/bin/env groovy
/*
 ** Jenkins Modules:
 * jenkins pipeline GPC VM type input parameter selection box.
 *
 * This module has to be load as shown in the root context README.md
 */

/**
 *
 ** Function:
 * This function will instanciate the std lib jenkins method to create an input parameter selection box
 *
 * Method declaration:
 *     method(name: 'input', type: 'Object', namedParams: [parameter(name: 'message', type: 'java.lang.String'),
 *     parameter(name: 'id', type: 'java.lang.String'), parameter(name: 'ok', type: 'java.lang.String'),
 *     parameter(name: 'parameters', type: 'Map'), parameter(name: 'submitter', type: 'java.lang.String'),
 *     parameter(name: 'submitterParameter', type: 'java.lang.String'), ], doc: 'Wait for interactive input')
 *
 ** Examples:
 *     // Docker Machine AWs Size
 *     String gcpVmSize = dockerMachinegcpVmSize()
 *     echo "GCP VM Size: ${gcpVmSize}"
 */
def call() {
    try {
        stage('\u2708 Enter GCP VM size \u2705') {
            String userInputVmSize = input(
                    id: 'userInputVmSize', message: 'Choose VM Size', ok: 'Submit', parameters: [
                    [$class: 'ChoiceParameterDefinition', choices:
                            'f1-micro\n' +
                            'g1-small\n' +
                            'n1-standard-1\n' +
                            'n1-standard-2', description: 'GCP VM Size:\n' +
                            'f1-micro Effective hourly rate $0.006/hr ||\n' +
                            'g-1small $0.019/hr ||\n' +
                            'n1-standard-1 $0.034/hr ||\n' +
                            'n1-standard-2 $0.067/hr ||\n' +
                            'Billing Details: https://cloud.google.com/compute/pricing', name: 'target']
            ])
            echo "GCP Virtual Machine Size: ${userInputVmSize}"
            return userInputVmSize
        }
    } catch (e) {
        echo "[ERROR] Exception: ${e}"
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this