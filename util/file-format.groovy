#!/usr/bin/env groovy
/*
 * Util Modules: file format helper.
 */


/*
 * Convert a Groovy Map to a YAML-formatted string.
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

/*
 * Convert Groovy Map to a JS-formatted string.
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

def mapToDotEnv(LinkedHashMap map) {
    String dotEnv = ""
    map.each { name, value ->
        dotEnv += name + "=" + value + "\n"
    }
    return dotEnv
}

return this