#!/usr/bin/env groovy

/*
 ** Util Modules:
 * File format helper.
 *
 ** IMPORTANT:
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 *
 */

/**
 ** Function:
 * Convert a LinkedHashMap to a YAML-formatted string.
 *
 ** Parameters:
 * @param LinkedHashMap map    Groovy Map, eg:[key1:"value1",key2:"value2"] or [keys:[key1:"value1", key2:"value2"],state:"stateful"]
 *
 * @return String YAML-formatted
 */
def mapToYaml(LinkedHashMap map) {
    String yaml = ""
    map.each { name, value ->
        String newName = name.replaceAll('"', "")
        String newValue = ""
        if (value == "null" || value == "false" || value == "true" || value.isNumber()) {
            newValue = value
        } else {
            newValue = '"' + value + '"'
        }
        yaml += "    " + newName + ": " + newValue.replaceAll("\\n", "\\\\n") + "\n"
    }
    return yaml
}

/**
 ** Function:
 * Convert LinkedHashMap to a JS-formatted string.
 *
 ** Parameters:
 * @param LinkedHashMap map    Groovy Map, eg:[key1:"value1",key2:"value2"] or [keys:[key1:"value1", key2:"value2"],state:"stateful"]
 *
 * @return String JS-formatted
 */
def mapToJsConfig(LinkedHashMap map) {
    String jsOutput = "module.exports = {\n"
    int counter = 0
    for (item in map) {
        String newName = item.key.replaceAll('"', "")
        jsOutput += "    " + newName + ": '" + item.value + "'"

        counter++
        if (counter < map.size()) {
            jsOutput += ","
        }

        jsOutput += "\n"
    }
    jsOutput += "};"

    return jsOutput
}

/**
 ** Function:
 * Convert LinkedHashMap to a DotEnv (.env) formatted string.
 *
 ** Parameters:
 * @param LinkedHashMap map    Groovy Map, eg:[key1:"value1",key2:"value2"] or [keys:[key1:"value1", key2:"value2"],state:"stateful"]
 *
 * @return String DotEnv (.env) formatted.
 */
def mapToDotEnv(LinkedHashMap map) {
    String dotEnv = ""
    map.each { name, value ->
        dotEnv += name + "=" + value + "\n"
    }
    return dotEnv
}

def mapToDotEnv(def lazyMap) {
    def activeMap = [:]
    for (entry in lazyMap) {
        activeMap[entry.key] = entry.value
    }
    return mapToDotEnv(activeMap)
}

// Note: this line is crucial when you want to load an external groovy script
return this