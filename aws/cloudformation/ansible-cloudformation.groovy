#!/usr/bin/env groovy

/**
 ** Jenkins Modules:
 * AWS Ansible Cloudformation helper.
 *
 ** IMPORTANT:
 * This module relies on Ansible Jenkins Plugin, plus the ansible installed in the current jenkins server
 * to be configured to run as-is, this module does not handle that.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 *
 ** Function:
 * Execute an Ansible Playbook with --vault-password-file
 *
 ** Parameters:
 * @param String ansiblePlayPath           Ansible playbook root context path
 * @param String ansiblePlayFile           Ansible playbook setup.yml main file
 * @param String AnsiblePlayRole           Ansible playbook cloudformation role to be executed
 * @param String ansiblePlayRoleVarsPrefix Ansible playbook cloudformation role variables prefix to to be executed
 * @param String ansibleVaultPass          Ansible vault password
 *
 * @return NO return value. This call will execute the stages declared in this module function.
 */

def call(String ansiblePlayPath, String ansiblePlayFile, String AnsiblePlayRole,
         String ansiblePlayRoleVarsPrefix, String ansibleVaultPass) {

    stage("\u2708 Select CF Stack to work with\u2705") {

        String CfStackCmd = "cd ${ansiblePlayPath} && cat ${ansiblePlayFile}|grep 'role: ${AnsiblePlayRole}'|cut -d'\"' -f2|cut -d' ' -f2"
        CF_STACK_NAME = sh(returnStdout: true, script: CfStackCmd).trim()

        CF_STACK_NAME = input(
                id: 'userInputChoice', message: "CF stack to WORK WITH", ok: 'Submit', parameters: [
                [$class     : 'ChoiceParameterDefinition', choices: "${CF_STACK_NAME}",
                 description: 'cf orchestration', name: 'ec_stack_cf']
        ])
    }

    stage('Configure setup.yml playbook code to be executed') {
        echo "## CF_STACK_NAME: ${CF_STACK_NAME}"

        sh "sed -i -e 's/#    - { role: ${AnsiblePlayRole}, ${ansiblePlayRoleVarsPrefix}: \"{{ ${CF_STACK_NAME} }}\" }" +
                "/    - { role: ${AnsiblePlayRole}, ${ansiblePlayRoleVarsPrefix}: \"{{ ${CF_STACK_NAME} }}\" }/g' " +
                "${ansiblePlayPath}/${ansiblePlayFile}"
    }

    stage('Execute Ansible + AWS Cloudformation') {

        sh "#!/bin/sh +x \n" +
                "echo ${ansibleVaultPass} > ${ansiblePlayPath}/.vault_pass"

        ansiblePlaybook(
                playbook: "${ansiblePlayPath}/${ansiblePlayFile}",
                credentialsId: 'jenkins.tunubi.com_id_rsa',
                inventory: "${ansiblePlayPath}/.hosts",
                extras: "--vault-password-file='${ansiblePlayPath}/.vault_pass'",
                colorized: true
        )
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this