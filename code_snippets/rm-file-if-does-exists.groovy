stage ('rm file if it does exists') {

    String FILE_PATH = "${AWS_EB_CONFIG_PATH}/config.yml"

    // Create a File object representing the file 'A/B'
    def file = new File("${FILE_PATH}")

    // If it doesn't exist
    if (file.exists()) {
        sh "rm -rf ${AWS_EB_CONFIG_PATH}/config.yml"

    }

}