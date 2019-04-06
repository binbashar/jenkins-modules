/**
 *  Created by Exequiel Barrirero & Marcos Pagnucco on 03/02/17.
 */

def appRepo() {

    try {

        echo '✈ Choose Application ✅'
        userInput_repository = input(
                id: ' userInput_repository', message: 'Select the App to work with', ok: 'Submit', parameters: [
                [$class: 'ChoiceParameterDefinition', choices: 'app-name-1\napp-name-2\napp-name-3', description: 'Modify Env', name: 'target']
        ])

        return userInput_repository

    } catch (e) {
        throw e as Throwable
    }
}

return this