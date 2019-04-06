#!/usr/bin/env groovy
/*
 * Jenkins Modules: Mysql restore database from uploaded file.
 *
 * Important: this module relies on MYSQL CLI, installed in the current jenkins server and of course the dbHost to be
 * reacheable to be configured to run as-is, this module does not handle that.
 *
 * @param mysqlUser     mysql user to be passed as parameter in the cli
 * @param mysqlRootPass mysql user password (with the necessary permissions) to be passed as parameter in the cli
 * @param dbHost        mysql db server host where the db from file will be restored
 */

def call(mysqlUser,mysqlRootPass,dbHost) {

    String userInput_upload_sql_file = ""
    try {
            stage('Would you like to upload a .sql file to run on the database?') {
                //noinspection GroovyAssignabilityCheck
                userInput_upload_sql_file = input(
                        id: 'userInput_upload_sql_file', message: 'Would you like to upload .sql file?', ok: 'Submit', parameters: [
                        [$class: 'ChoiceParameterDefinition', choices: 'Yes\nNo', description: 'Database update file', name: 'target']
                ])
                echo "SQL file is going to be uploaded: ${userInput_upload_sql_file}"
            }

            if (userInput_upload_sql_file == 'Yes') {
              stage('file input') {
                  // Get file using input step, will put it in build directory
                  //noinspection GroovyAssignabilityCheck
                  def inputFile = input message: 'Upload file', parameters: [file(name: 'migration.sql')]
                  // Read contents and write to workspace
                  writeFile(file: 'migration.sql', text: inputFile.readToString())
                  // Stash it for use in a different part of the pipeline
                  stash name: 'data', includes: 'migration.sql'

                  def mysqlFile = readFile 'migration.sql'
                  echo "Content of input file: ${mysqlFile}"

                  File mysql_file = new File("${env.WORKSPACE}/migration.sql")

                 if (!mysql_file.exists()) {
                      echo ("File does not exist")
                 } else {
                      echo ("File does EXISTS")
                 }
              }

                stage("run SQL file on MYSQL dbHost: ${dbHost} and to DB_NAME: ${DB_NAME}") {
                    sh "#!/bin/bash \n" +
                            "cat migration.sql | mysql -h ${dbHost} -u ${mysqlUser} --password=${mysqlRootPass} ${DB_NAME}"
                }
            }
        } catch (e) {
            throw e as Throwable
        }
    }

return this

