stage ('create folder if it does not exists') {

    String APP_URL = "https://www.domain.com"

    String s = "$APP_URL"
    echo "APP_ULR: ${APP_URL}"
    APP_URL = s.replace("/", "\\/")

    echo "APP_URL: ${APP_URL}"
}

stage ('backticks usage') {
    echo 'No quotes in single backticks'
    sh 'echo $BUILD_NUMBER'
    echo 'Double quotes are silently dropped'
    sh 'echo "$BUILD_NUMBER"'
    echo 'Even escaped with a single backslash they are dropped'
    sh 'echo \"$BUILD_NUMBER\"'
    echo 'Using two backslashes, the quotes are preserved'
    sh 'echo \\"$BUILD_NUMBER\\"'
    echo 'Using three backslashes still results in preserving the single quotes'
    sh 'echo \\\"$BUILD_NUMBER\\\"'
    echo 'To end up with \" use \\\\\\\" (yes, seven backticks)'
    sh 'echo \\\\\\"$BUILD_NUMBER\\\\\\"'
    echo 'This is fine and all, but we cannot substitute Jenkins variables in single quote strings'
    def foo = 'bar'
    sh 'echo "${foo}"'
    echo 'This does not interpolate the string but instead tries to look up "foo" on the command line, so use double quotes'
    sh "echo \"${foo}\""
    echo 'Great, more escaping is needed now. How about just concatenate the strings? Well that gets kind of ugly'
    sh 'echo \\\\\\"' + foo + '\\\\\\"'
    echo 'We still needed all of that escaping and mixing concatenation is hideous!'
    echo 'There must be a better way, enter dollar slashy strings (actual term)'
    def command = $/echo \\\"${foo}\\\"/$
    sh command
    echo 'String interpolation works out of the box as well as environment variables, escaped with double dollars'
    def vash = $/echo \\\"$$BUILD_NUMBER\\\" ${foo}/$
    sh vash
    echo 'It still requires escaping the escape but that is just bash being bash at that point'
    echo 'Slashy strings are the closest to raw shell input with Jenkins, although the non dollar variant seems to give an error but the dollar slash works fine'
}