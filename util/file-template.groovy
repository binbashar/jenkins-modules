#!/usr/bin/env groovy

/*
 ** Template Files helper.
 * This module may be useful when you need to work with template files whose contents
 * usually have placeholders that need to be replaced with any given values.
 */

/**
 ** Function:
 * Use the given source file contents as a template, replace all placeholders with
 * the given placeholder values and save it to the given destination file.
 *
 ** Parameters:
 * @param String srcFile Path to a file whose contents will be used as a template for replacing placeholders
 * @param String destFile Path to a file that will hold the contents of the template after replacing its placeholders
 * @param Map placeholderValues A list of key/value pairs that will be used for replacing placeholders
 * @return
 */
def replace(String sourceFile, String destFile, HashMap placeholderValues, String placeholderDelimiter = '%') {
    String fileContents = readFile sourceFile
    
    // Go through all placeholder items
    for (def placeholderObj in placeholderValues) {
        // Find and replace placeholders with its corresponding values
        String placeholder = placeholderDelimiter + placeholderObj.key + placeholderDelimiter
        fileContents = fileContents.replaceAll(/$placeholder/, placeholderObj.value)
    }
    
    writeFile file: destFile, text: fileContents
}