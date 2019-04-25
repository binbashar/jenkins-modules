#!/usr/bin/env groovy

/*
 * Jenkins Modules:
 * Docker-machine Passbolt CLI user creation.
 *
 * IMPORTANT:
 * This module relies docker and docker-machine installed in the current jenkins server to be configured to
 * run as-is, this module does not handle that.
 *
 * This module also expects a dockerized passbolt server running (https://hub.docker.com/r/passbolt/passbolt/)
 *
 * This module has to be load as shown in the root context README.md closely considering to meet the Pre-requisites section
 *
 ** HELPERS
 * $docker exec passbolt su -m -c "/var/www/passbolt/bin/cake passbolt register_user -u your@email.com -f yourname \
  * -l surname -r admin" -s /bin/sh www-data
 *
 * sh "#!/bin/bash \n" +
 *       "docker \$(docker-machine config ${DockerMachineName} | xargs) " +
 *       "exec -i su -s /bin/bash -c \"PASSBOLT_APP_BASE=https://${PassboltDomain} /var/www/passbolt/app/Console/cake passbolt register_user " +
 *       "-u it@tunubi.com -f IT -l TuNubi -r user\" nginx"
 *
 * Delete a user from DB
 * mysql> use passbolt
 * mysql> show tables
 * mysql> select * from users;
 * mysql> delete from users where username='info@binbash.com.ar' limit 1;
 */

/**
 ** Function:
 *
 *
 ** Parameters:
 * @param String DockerMachineName      docker-machine name where our passbolt server is currently deployed.
 * @param String ContNamePassbolt       passbolt docker container name.
 * @param String PassboltDomain         passbolt domain, eg: 'passbolt.aws.binbash.com.ar'       
 * @param String PassboltUserType       passbolt user type: 'user' or 'admin'.
 * @param String PassboltUserEmail      passbolt user email eg: 'name.lastname@binbash.com.ar'.
 * @param String PassboltUserFirstName  passbolt user 1st name eg: 'FirstName'.
 * @param String PassboltUserLastname   passbolt user lastname eg: 'LastName'.
 *
 * @return NO return value. This call will execute the call() function declared in this module.
 */

def call(String DockerMachineName,String ContNamePassbolt, String PassboltDomain, String PassboltUserType,
         String PassboltUserEmail, String PassboltUserFirstName, String PassboltUserLastname) {

    try {
        echo '✈ Passbolt User Creation ✅'
        sh "#!/bin/bash \n" +
                "COMPOSE_HTTP_TIMEOUT=300 docker \$(docker-machine config ${DockerMachineName} | xargs) " +
                "exec -i ${ContNamePassbolt} su -s /bin/sh " +
                "-m -c \"APP_FULL_BASE_URL=https://${PassboltDomain} /var/www/passbolt/bin/cake passbolt register_user " +
                "-u ${PassboltUserEmail} -f ${PassboltUserFirstName} -l ${PassboltUserLastname} -r ${PassboltUserType}\" www-data"

    } catch (e) {
        echo "[ERROR] Exception: ${e}"
        throw e as Throwable
    }
}

// Note: this line is crucial when you want to load an external groovy script
return this
