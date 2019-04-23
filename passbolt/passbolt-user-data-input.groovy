#!/usr/bin/env groovy
/*
 * Jenkins Modules: Passbolt selection box input parameters module.
 *
 * IMPORTANT: this module relies docker and docker-machine installed in the current jenkins server to be configured to
 * run as-is, this module does not handle that.
 *
 ** Example:
 * PASSBOLT_USER_DATA = load "${JENKINSFILES_MODS}/passbolt/jenkins_pipeline-passbolt_user_data.groovy"
 * PASSBOLT_USER_CREATION = load "${JENKINSFILES_MODS}/passbolt/jenkins_pipeline-passbolt_user_creation_2.x.groovy"
 * PASSBOLT_USER_DATA.extMain()
 * PASSBOLT_USER_TYPE = PASSBOLT_USER_DATA.returnPassboltUserType()
 * PASSBOLT_USER_EMAIL = PASSBOLT_USER_DATA.returnPassboltUserEmail()
 * PASSBOLT_USER_NAME = PASSBOLT_USER_DATA.returnPassboltUserName()
 * PASSBOLT_USER_LASTNAME = PASSBOLT_USER_DATA.returnPassboltUserLastname()
 */

String passboltUserType = ""
String passboltUserEmail = ""
String passboltUserName = ""
String passboltUserLastname = ""

def call() {

    try {
        stage('\u2708 Choose Passbolt User Type\u2705') {
            passboltUserType = input(
                    id: 'passboltUserType', message: 'Select Passbolt user type', ok: 'Submit', parameters: [
                    [$class: 'ChoiceParameterDefinition', choices: 'user\nadmin', description: 'Passbolt User', name: 'target']
            ])
        }

        stage('\u2708 Type Passbolt User email\u2705') {

            passboltUserEmail = input(
                    id: 'passboltUserEmail', message: "Please type passbolt user email user@domain.com:", parameters: [
                    [$class: 'TextParameterDefinition', defaultValue: '', description: 'Passbolt user email', name: 'passbolt_user@email.com']
            ])
            echo("Passbolt user email: ${passboltUserEmail}")
        }

        stage('\u2708 Type Passbolt User name\u2705') {

            passboltUserName = input(
                    id: 'passboltUserName', message: "Please type passbolt user name eg: John", parameters: [
                    [$class: 'TextParameterDefinition', defaultValue: '', description: 'Passbolt user name', name: 'passboltUserName']
            ])
            echo("Passbolt user email: ${passboltUserName}")
        }

        stage('\u2708 Type Passbolt User lastname\u2705') {

            passboltUserLastname = input(
                    id: 'passboltUserLastname', message: "Please type passbolt user lastname eg Doe:", parameters: [
                    [$class: 'TextParameterDefinition', defaultValue: '', description: 'Passbolt user lastname', name: 'passboltUserLastname']
            ])
            echo("Passbolt user email: ${passboltUserLastname}")
        }
    } catch (e) {
        throw e as Throwable
    }
}

return this

def returnPassboltUserType() {
    try {
        return passboltUserType
    } catch (e) {
        throw e as Throwable
    }
}

def returnPassboltUserEmail() {
    try {
        return passboltUserEmail
    } catch (e) {
        throw e as Throwable
    }
}

def returnPassboltUserName() {
    try {
        return passboltUserName
    } catch (e) {
        throw e as Throwable

    }
}

def returnPassboltUserLastname() {
    try {
        return passboltUserLastname
    } catch (e) {
        throw e as Throwable

    }
}

