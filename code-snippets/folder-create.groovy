stage ('create folder if it does not exists') {
    String FOLDER_PATH = "/your/absolute/path/here"

    // Create a File object representing the folder 'A/B'
    def folder = new File("${FOLDER_PATH}")

    // If it doesn't exist
    if (!folder.exists()) {
        // Create all folders up-to and including B
        folder.mkdirs()
    }

}