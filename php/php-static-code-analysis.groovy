#!/usr/bin/env groovy

/**
 ** Jenkins Modules:
 * PHP static code analysis module.
 *
 ** IMPORTANT:
 * This module relies on the proper versions of phpcpd and phpmd, as well as plugins PmdPublisher
 * and DryPublisher installed in the current jenkins server to be configured to run as-is, this module does not handle
 * that.
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 *
 ** Parameters:
 * @param rootDir   Jenkins workspace root context dir
 * @param dataDir   PHP app code root context dir
 *
 * @return NO return value. This call will execute the call() function declared in this module.
 */

def call(rootDir,dataDir) {

    def userInput_upload_sql_file
    try {
        echo 'PHP static code analysis'
        //sh "sudo phpcs --config-set ignore_warnings_on_exit 1 && sudo phpcs --report=checkstyle --report-file=checkstyle-result.xml -q ${rootDir}/${dataDir}"
        sh "sudo phpcpd --log-pmd=cpd.xml ${rootDir}/${dataDir}"
        sh "sudo phpmd ${rootDir}/${dataDir} xml cleancode,codesize --reportfile phpmd.xml --ignore-violations-on-exit"
        step([$class: 'hudson.plugins.pmd.PmdPublisher', pattern: 'phpmd*'])
        //step([$class: 'hudson.plugins.checkstyle.CheckStylePublisher', pattern: 'checkstyle-*'])
        step([$class: 'hudson.plugins.dry.DryPublisher', CopyPasteDetector: 'cpd.xml'])
    } catch (e) {
        throw e as Throwable
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this
