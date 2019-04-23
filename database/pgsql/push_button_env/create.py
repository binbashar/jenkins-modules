#!/usr/bin/python
#
# The script was designed to complement the implementation of push button
# environments. It can be used for creating a new database, a role with power
# privileges on that db, and a user with that role.
#

# Uncomment below presented libraries if needed
# import psycopg2
# from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT
# import string
# from random import *

import dbhelper
import optparse
import logging
import sys


def main():
    logging.basicConfig(format='[%(levelname)s] %(message)s', level=logging.DEBUG)

    parser = optparse.OptionParser()
    parser.add_option('-s', '--dbhost', dest="db_host", help="DB Host")
    parser.add_option('-d', '--dbname', dest="db_name", help="DB Name")
    parser.add_option('-u', '--dbuser', dest="db_user", help="DB User")
    parser.add_option('-p', '--dbpass', dest="db_pass", help="DB Password")

    parser.add_option('-w', '--newdbname', dest="new_db_name", help="New DB Name")
    parser.add_option('-x', '--newdbrole', dest="new_db_role", help="New DB Role")
    parser.add_option('-y', '--newdbuser', dest="new_db_user", help="New DB User")
    parser.add_option('-z', '--newdbpass', dest="new_db_pass", help="New DB Pass")

    parser.add_option('-e', '--extensions', dest="db_extensions", help="DB Extensions")
    opts, args = parser.parse_args()

    if (not opts.db_host or not opts.db_name or not opts.db_user
            or not opts.db_pass or not opts.new_db_name or not opts.new_db_role
            or not opts.new_db_user or not opts.new_db_pass):
        raise Exception("Mandatory arguments are missing")
        sys.exit(1)

    # Connect to db using a user with enough privileges
    db_conn = dbhelper.connect(opts.db_host, opts.db_name, opts.db_user, opts.db_pass)

    # Create a permanent role so we can grant/revoke privileges from it
    role_name = opts.new_db_role
    if dbhelper.check_role_exists(db_conn, role_name):
        logging.info("Role ALREADY exists. Skip.")
    else:
        dbhelper.create_role(db_conn, role_name)

    # Create the database
    new_db_name = opts.new_db_name
    if dbhelper.check_database_exists(db_conn, new_db_name):
        logging.info("Database ALREADY exists. Skip.")
    else:
        dbhelper.create_database(db_conn, new_db_name)

    dbhelper.close(db_conn)

    # Connect to the new database
    db_conn = dbhelper.connect(opts.db_host, new_db_name, opts.db_user, opts.db_pass)

    # Update role privileges on the new database
    dbhelper.grant_role_power_privileges(db_conn, role_name)

    # Create a user that has the privileges of the role above
    new_db_user = opts.new_db_user
    new_db_pass = opts.new_db_pass
    if dbhelper.check_role_exists(db_conn, new_db_user):
        logging.info("User ALREADY exists. Skip.")
    else:
        dbhelper.create_user(db_conn, new_db_user, new_db_pass, role_name)

    # Add extensions if any was provided
    if opts.db_extensions is not None:
        db_extensions = opts.db_extensions.split(",")
        for db_extension in db_extensions:
            if dbhelper.check_extension_exists(db_conn, db_extension):
                logging.info("Extension ALREADY exists with name={0}. Skip.".format(db_extension))
            else:
                dbhelper.create_extension(db_conn, db_extension)

    dbhelper.close(db_conn)
    sys.exit(0)


if __name__ == "__main__":
    main()
