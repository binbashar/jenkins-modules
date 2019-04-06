def keys = [mysql, nginx-http, nginx-https, apache-http, apache-https, sesnu-client, php-fpm]
def values = ['3306', '80', '443', '80', '443', '3030', '9000' ]
[keys,values].transpose().collectEntries()