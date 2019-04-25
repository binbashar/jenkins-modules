#!/usr/bin/python
#
# The script was designed to complement the implementation of push button
# environments. It is the counter-part of the 'create' script and can be used to
# delete the database and the user that were created by it.
#
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT
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
    opts, args = parser.parse_args()

    if (not opts.db_host or not opts.db_name or not opts.db_user
            or not opts.db_pass or not opts.new_db_name or not opts.new_db_role
            or not opts.new_db_user or not opts.new_db_pass):
        raise Exception("Mandatory arguments are missing")
        # Code below will be unreachable
        # sys.exit(1)

    try:
        # Connect to the target db in order remove privileges from the role
        target_db_name = opts.new_db_name
        db_conn = dbhelper.connect(opts.db_host, target_db_name, opts.new_db_user, opts.new_db_pass)

        # Remove power privileges that were added to the role
        target_role_name = opts.new_db_role
        dbhelper.revoke_role_power_privileges(db_conn, target_role_name)

        # Drop anything owned by the user we intend to delete
        dbhelper.drop_owned(db_conn, opts.new_db_user)

        dbhelper.close(db_conn)

        # Connect to db using a different user but still with enough privileges
        db_conn = dbhelper.connect(opts.db_host, opts.db_name, opts.db_user, opts.db_pass)

        # Reassign ownership first
        logging.info("Grant roles and reassign owned before removing user/database.")
        dbhelper.grant_role(db_conn, opts.new_db_role, opts.db_user)
        dbhelper.grant_role(db_conn, opts.new_db_user, opts.db_user)
        dbhelper.reassign_owned(db_conn, opts.new_db_user, opts.db_user)

        # Then drop the user
        target_db_user = opts.new_db_user
        if not dbhelper.check_role_exists(db_conn, target_db_user):
            logging.info("User DOES NOT exist. Skip.")
        else:
            dbhelper.delete_user(db_conn, target_db_user)

        # And finally drop the database
        if not dbhelper.check_database_exists(db_conn, target_db_name):
            logging.info("Database DOES NOT exist. Skip.")
        else:
            dbhelper.delete_database(db_conn, target_db_name)

        dbhelper.close(db_conn)
        sys.exit(0)

    except Exception as e:
        print(e)
        sys.exit(1)


if __name__ == "__main__":
    main()
