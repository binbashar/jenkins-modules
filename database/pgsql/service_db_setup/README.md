# Service DB Credentials Setup

## What's this?
This script helps enforcing the approach suggested by AWS to manage Postgres Users and Roles: https://aws.amazon.com/blogs/database/managing-postgresql-users-and-roles/

It basically helps you when you need to set up a new service that requires its own database and user credentials to use for connecting to it. But it also creates readonly and read/write roles so that it is easy to assign those roles to other users or to create other roles with different permissions.


## Pre-requisites
Make sure you have Python 3.8 or greater

## Quickstart (using virtualenv)
Install virtualenv (if you also have Python 2 installed, you may need to use `pip3` instead of `pip`):
```console
pip install virtualenv
```

Ok, assuming that you are in the same directory of this README, go ahead and create a virtualenv context:
```console
virtualenv venv
```
And now activate it:
```console
source venv/bin/activate
```

Install the dependencies needed for this script:
```console
pip install -r requirements.txt
```

Set up a .env file with the credentials that have to be used to connect to the DB:
```console
cp .env.example .env
```
Again, edit the .env file to set the credentials that the script will use to connect to the DB:
```console
vi .env
```

Run the script (you should see the script's usage, options and commands help text):
```console
python main.py
```


## Run a connectivity test
A quick way to verify that the script is able to connect to the DB is to do this (I'll be assumming that you went through the previous section and got the script ready to roll):
```console
python main.py test -v
```

Notice that I'm passing `-v` in the example above to show a verbose output. You can use that to get further details on what the script does for debugging or verification purposes.


## Run the script to set up credentials for a new service
Run the script like this:
```console
python main.py create
```

The script should prompt you for the service name. Once it gets that, it will define preset names for the roles and the user that it creates. The output should show the choices the script made.

The script also creates a random password for the service. Such password will be written to a `.pass` file in the same directory.

You can also override the name choices the script makes and set your own like this:
```console
python main.py create \
    --service-name foo \
    --database-name foo_db \
    --readonly-role-name foo_readonly \
    --readwrite-role-name foo_readwrite \
    --user-name foo_user
```
