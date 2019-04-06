#!/usr/bin/env groovy
/**
 * Created by Exequiel Barrirero & Marcos Pagnucco on 26/05/17.
 */

def AUTH_KEY
def SECURE_AUTH_KEY
def LOGGED_IN_KEY
def NONCE_KEY
def AUTH_SALT
def SECURE_AUTH_SALT
def LOGGED_IN_SALT
def NONCE_SALT

def extMain(currentDir,userInputRepo) {

    try {
        echo 'Extracting binbash.com.ar Secrets from Vault & create binbash.com.ar-variables.env'
        def secrets_binbash = [
                [$class: 'VaultSecret', path: "secret/${userInputRepo}", secretValues: [
                        [$class: 'VaultSecretValue', envVar: 'AUTH_KEY', vaultKey: 'AUTH_KEY'],
                        [$class: 'VaultSecretValue', envVar: 'SECURE_AUTH_KEY', vaultKey: 'SECURE_AUTH_KEY'],
                        [$class: 'VaultSecretValue', envVar: 'LOGGED_IN_KEY', vaultKey: 'LOGGED_IN_KEY'],
                        [$class: 'VaultSecretValue', envVar: 'NONCE_KEY', vaultKey: 'NONCE_KEY'],
                        [$class: 'VaultSecretValue', envVar: 'AUTH_SALT', vaultKey: 'AUTH_SALT'],
                        [$class: 'VaultSecretValue', envVar: 'SECURE_AUTH_SALT', vaultKey: 'SECURE_AUTH_SALT'],
                        [$class: 'VaultSecretValue', envVar: 'LOGGED_IN_SALT', vaultKey: 'LOGGED_IN_SALT'],
                        [$class: 'VaultSecretValue', envVar: 'NONCE_SALT', vaultKey: 'NONCE_SALT']
                ]]
        ] as Object

        wrap([$class: 'VaultBuildWrapper', vaultSecrets: secrets_binbash]) {
            sh "#!/bin/sh +x \n" +
                    "echo AUTH_KEY=${AUTH_KEY} > ${WORKSPACE}/binbash.com.ar-variables.env"
            sh "#!/bin/sh +x \n" +
                    "echo SECURE_AUTH_KEY=${SECURE_AUTH_KEY} >> ${WORKSPACE}/binbash.com.ar-variables.env"
            sh "#!/bin/sh \n" +
                    "echo LOGGED_IN_KEY=${LOGGED_IN_KEY} >> ${WORKSPACE}/binbash.com.ar-variables.env"
            sh "#!/bin/sh \n" +
                    "echo NONCE_KEY=${NONCE_KEY} >> ${WORKSPACE}/binbash.com.ar-variables.env"
            sh "#!/bin/sh \n" +
                    "echo AUTH_SALT=${AUTH_SALT} >> ${WORKSPACE}/binbash.com.ar-variables.env"
            sh "#!/bin/sh \n" +
                    "echo SECURE_AUTH_SALT=${SECURE_AUTH_SALT} >> ${WORKSPACE}/binbash.com.ar-variables.env"
            sh "#!/bin/sh \n" +
                    "echo LOGGED_IN_SALT=${LOGGED_IN_SALT} >> ${WORKSPACE}/binbash.com.ar-variables.env"
            sh "#!/bin/sh \n" +
                    "echo NONCE_SALT=${NONCE_SALT} >> ${WORKSPACE}/binbash.com.ar-variables.env"

            sh "#!/bin/bash +x \n" +
                    "echo ${AUTH_KEY} > resultfile"
            AUTH_KEY = readFile('resultfile').trim()

            sh "#!/bin/bash +x \n" +
                    "echo ${SECURE_AUTH_KEY} > resultfile"
            SECURE_AUTH_KEY = readFile('resultfile').trim()

            sh "#!/bin/bash +x \n" +
                    "echo ${LOGGED_IN_KEY} > resultfile"
            LOGGED_IN_KEY = readFile('resultfile').trim()

            sh "#!/bin/bash +x \n" +
                    "echo ${NONCE_KEY} > resultfile"
            NONCE_KEY = readFile('resultfile').trim()

            sh "#!/bin/bash +x \n" +
                    "echo ${AUTH_SALT} > resultfile"
            AUTH_SALT = readFile('resultfile').trim()

            sh "#!/bin/bash +x \n" +
                    "echo ${SECURE_AUTH_SALT} > resultfile"
            SECURE_AUTH_SALT = readFile('resultfile').trim()

            sh "#!/bin/bash +x \n" +
                    "echo ${LOGGED_IN_SALT} > resultfile"
            LOGGED_IN_SALT = readFile('resultfile').trim()

            sh "#!/bin/bash +x \n" +
                    "echo ${NONCE_SALT} > resultfile"
            NONCE_SALT = readFile('resultfile').trim()

            sh "#!/bin/bash \n" +
                    "rm -rf resultfile"
        }
    } catch (e) {
        throw e as Throwable
    }
}

return this

def returnAuthKey() {
    try {
        return AUTH_KEY
    } catch (e) {
        throw e as Throwable
    }
}

def returnSecureAuthKey() {
    try {
        return SECURE_AUTH_KEY

    } catch (e) {
        throw e as Throwable
    }
}

def returnLoggedInKey() {
    try {
        return LOGGED_IN_KEY
    } catch (e) {
        throw e as Throwable
    }
}

def returnNonceKey() {
    try {
        return NONCE_KEY
    } catch (e) {
        throw e as Throwable

    }
}

def returnAuthSalt() {
    try {
        return AUTH_SALT
    } catch (e) {
        throw e as Throwable

    }
}

def returnSecureAuthSalt() {
    try {
        return SECURE_AUTH_SALT
    } catch (e) {
        throw e as Throwable
    }
}

def returnLoggedInSalt() {
    try {
        return LOGGED_IN_SALT
    } catch (e) {
        throw e as Throwable
    }
}

def returnNonceSalt() {
    try {return NONCE_SALT

    } catch (e) {
        throw e as Throwable
    }
}