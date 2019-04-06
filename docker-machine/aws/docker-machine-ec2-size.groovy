#!/usr/bin/env groovy
/*
 * Jenkins Modules: jenkins pipeline AWS EC2 type input selection box parameter.
 *
 */

def call() {

    try {
        stage('\u2708 Enter AWS Instance Size \u2705') {
            EC2_SIZE = input(
                    id: 'userInputInstanceType', message: 'Choose instance type', ok: 'Submit', parameters: [
                    [$class: 'ChoiceParameterDefinition', choices: 't2.micro\nt2.small\nt2.medium\nt2.large\nt2.xlarge', description: 'AWS instance type:\n' +
                            't2.small $0.023/hr ||\n' +
                            't2.medium $0.047/hr ||\n' +
                            't2.large $0.094/hr ||\n' +
                            't2.xlarge $0.186/hr ||\n' +
                            'Billing Details: https://aws.amazon.com/ec2/pricing/', name: 'target']
            ])
            echo "AWS instance type: ${EC2_SIZE}"
            return EC2_SIZE
        }
    } catch (e) {
        throw e as Throwable
    }
}

return this