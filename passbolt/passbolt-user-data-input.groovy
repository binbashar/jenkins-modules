#!/usr/bin/env groovy

/*
 ** Jenkins Modules:
 * Passbolt selection box input parameters module.
 *
 ** IMPORTANT:
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 */

/**
 ** Function:
 *  Jenkins pipeline input user type from combo-box (ChoiceParameterDefinition class).
 *
 ** Parameters:
 *  @return String passboltUserType      passbolt user type: 'user' or 'admin'.
 */
def inputPassboltUserType() {
    try {
            String passboltUserType = input(
                    id: 'passboltUserType', message: 'Select Passbolt user type', ok: 'Submit', parameters: [
                    [$class: 'ChoiceParameterDefinition',
                     choices: 'user\nadmin',
                     description: 'Passbolt User',
                     name: 'target']
            ])
            echo("Passbolt user type: ${passboltUserType}")
            return passboltUserType

    } catch (e) {
        echo "[ERROR] Exception: ${e}"
        throw e as Throwable
    }
}

/**
 ** Function:
 *  Jenkins pipeline input user email from combo-box (ChoiceParameterDefinition class).
 *
 ** Parameters:
 *  @return String passboltUserEmail    passbolt user email eg: 'name.lastname@binbash.com.ar'
 */
def inputPassboltUserEmail() {
    try {
        String passboltUserEmail = input(
                id: 'passboltUserEmail', message: "Please type passbolt user email user@domain.com:", parameters: [
                [$class: 'TextParameterDefinition',
                 defaultValue: '',
                 description: 'Passbolt user email',
                 name: 'passbolt_user@email.com']
        ])
        echo("Passbolt user email: ${passboltUserEmail}")
        return passboltUserEmail

    } catch (e) {
        echo "[ERROR] Exception: ${e}"
        throw e as Throwable
    }
}

/**
 ** Function:
 *  Jenkins pipeline input user name from combo-box (ChoiceParameterDefinition class).
 *
 ** Parameters:
 *  @return String passboltUserName      passbolt user 1st name eg: 'FirstName'
 */
def inputPassboltUserName() {
    try {
        String passboltUserName = input(
                id: 'passboltUserName', message: "Please type passbolt user name eg: John", parameters: [
                [$class: 'TextParameterDefinition',
                 defaultValue: '',
                 description: 'Passbolt user name',
                 name: 'passboltUserName']
        ])
        echo("Passbolt user email: ${passboltUserName}")
        return passboltUserName

    } catch (e) {
        echo "[ERROR] Exception: ${e}"
        throw e as Throwable
    }
}

/**
 ** Function:
 *  Jenkins pipeline input last name from combo-box (ChoiceParameterDefinition class).
 *
 ** Parameters:
 *  @return String passboltUserLastname      passbolt user lastname eg: 'LastName'.
 */
def inputPassboltUserLastName() {
    try {
        String passboltUserLastname = input(
                id: 'passboltUserLastname', message: "Please type passbolt user lastname eg Doe:", parameters: [
                [$class: 'TextParameterDefinition',
                 defaultValue: '',
                 description: 'Passbolt user lastname',
                 name: 'passboltUserLastname']
        ])
        echo("Passbolt user email: ${passboltUserLastname}")
        return passboltUserLastname

    } catch (e) {
        echo "[ERROR] Exception: ${e}"
        throw e as Throwable
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this

