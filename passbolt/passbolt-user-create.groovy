#!/usr/bin/env groovy
/*
 * Jenkins Modules: docker-machine Passbolt CLI user creation.
 *
 * IMPORTANT: this module relies docker and docker-machine installed in the current jenkins server to be configured to
 * run as-is, this module does not handle that.
 */

def call(DockerMachineName,ContNamePassbolt,PassboltDomain,PassboltUserType,PassboltUserEmail,PassboltUserName,PassboltUserLastname) {

    try {
        echo '✈ Passbolt User Creation✅'
        sh "#!/bin/bash \n" +
                "COMPOSE_HTTP_TIMEOUT=300 docker \$(docker-machine config ${DockerMachineName} | xargs) " +
                "exec -i ${ContNamePassbolt} su -s /bin/sh " +
                "-m -c \"APP_FULL_BASE_URL=https://${PassboltDomain} /var/www/passbolt/bin/cake passbolt register_user " +
                "-u ${PassboltUserEmail} -f ${PassboltUserName} -l ${PassboltUserLastname} -r ${PassboltUserType}\" www-data"

    } catch (e) {
        throw e as Throwable
    }
}

return this



/** HELPERS
$docker exec passbolt su -m -c "/var/www/passbolt/bin/cake passbolt register_user -u your@email.com -f yourname -l surname -r admin" -s /bin/sh www-data

sh "#!/bin/bash \n" +
        "docker \$(docker-machine config ${DockerMachineName} | xargs) " +
        "exec -i su -s /bin/bash -c \"PASSBOLT_APP_BASE=https://${PassboltDomain} /var/www/passbolt/app/Console/cake passbolt register_user " +
        "-u it@tunubi.com -f IT -l TuNubi -r user\" nginx"

Delete a user from DB
mysql> use passbolt
mysql> show tables
mysql> select * from users;
mysql> delete from users where username='info@binbash.com.ar' limit 1;
*/
