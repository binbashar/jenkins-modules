#!/usr/bin/python
#
# PostgreSQL helper functions.
#
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT
import logging


def connect(db_host, db_name, db_user, db_pass):
    logging.info('Connect to database={0}'.format(db_name))
    conn = None
    try:
        conn = psycopg2.connect(host=db_host, dbname=db_name, user=db_user, password=db_pass)
    except (Exception, psycopg2.DatabaseError) as error:
        logging.error('Unable to connect to database')
        raise Exception('Unable to connect to database')

    return conn


def close(db_conn):
    logging.info('Close database connection')
    if db_conn is not None:
        db_conn.close()


def show_version(db_conn):
    cur = db_conn.cursor()
    cur.execute("SELECT version()")
    return cur.fetchone()


def check_role_exists(db_conn, role_name):
    cur = db_conn.cursor()
    cur.execute("SELECT 1 FROM pg_roles WHERE rolname='{0}'".format(role_name))
    role_exists = cur.fetchone()
    if role_exists is None:
        return False
    return True


def create_role(db_conn, role_name):
    logging.info('Create role with role_name={0}'.format(role_name))
    db_conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = db_conn.cursor()
    cur.execute('CREATE ROLE {0}'.format(role_name))
    logging.debug('Role creation finished with status={0}'.format(cur.statusmessage))
    if cur.statusmessage != "CREATE ROLE":
        raise Exception('Unable to create role')


def check_database_exists(db_conn, db_name):
    cur = db_conn.cursor()
    cur.execute("SELECT 1 FROM pg_database WHERE datname='{0}'".format(db_name))
    db_exists = cur.fetchone()
    if db_exists is None:
        return False
    return True


def create_database(db_conn, db_name):
    logging.info('Create database with db_name={0}'.format(db_name))
    db_conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = db_conn.cursor()
    cur.execute("CREATE DATABASE {0}".format(db_name))
    logging.debug('Database creation finished with status={0}'.format(cur.statusmessage))
    if cur.statusmessage != "CREATE DATABASE":
        raise Exception('Unable to create database')


def grant_role_power_privileges(db_conn, role_name):
    logging.info('Grant privileges using role_name={0}'.format(role_name))
    db_conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = db_conn.cursor()
    cur.execute("GRANT USAGE ON SCHEMA public TO {0}".format(role_name));
    logging.debug('Grant usage finished with status={0}'.format(cur.statusmessage))
    cur.execute(
        "ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO {0}".format(
            role_name))
    logging.debug('Alter default tables privileges finished with status={0}'.format(cur.statusmessage))
    cur.execute("ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO {0}".format(role_name))
    logging.debug('Alter default sequences privileges finished with status={0}'.format(cur.statusmessage))


def revoke_role_power_privileges(db_conn, role_name):
    logging.info('Revoke privileges using role_name={0}'.format(role_name))
    db_conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = db_conn.cursor()
    cur.execute("REVOKE USAGE ON SCHEMA public FROM {0}".format(role_name));
    logging.debug('Revoke usage finished with status={0}'.format(cur.statusmessage))
    cur.execute("REVOKE ALL PRIVILEGES ON ALL TABLES IN SCHEMA public FROM {0}".format(role_name))
    logging.debug('Revoke all tables privileges finished with status={0}'.format(cur.statusmessage))
    cur.execute("REVOKE ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public FROM {0}".format(role_name))
    logging.debug('Revoke all sequences privileges finished with status={0}'.format(cur.statusmessage))


def create_user(db_conn, db_user, db_pass, role_name):
    logging.info('Create user with db_user={0}, role_name={1}'.format(db_user, role_name))
    db_conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = db_conn.cursor()
    cur.execute("CREATE USER {0} WITH PASSWORD '{1}'".format(db_user, db_pass))
    logging.debug('Create user finished with status={0}'.format(cur.statusmessage))
    if cur.statusmessage != "CREATE ROLE":
        raise Exception('Unable to create user')
    else:
        cur.execute("GRANT {0} TO {1}".format(role_name, db_user))
        logging.debug('Grant user finished with status={0}'.format(cur.statusmessage))
        if cur.statusmessage != "GRANT ROLE":
            raise Exception('Unable to grant role to user')


def delete_user(db_conn, db_user):
    logging.info('Delete user with db_user={0}'.format(db_user))
    db_conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = db_conn.cursor()
    cur.execute('DROP USER {0}'.format(db_user))
    logging.debug('User deletion finished with status={0}'.format(cur.statusmessage))
    if cur.statusmessage != "DROP ROLE":
        raise Exception('Unable to delete user')


def check_database_exists(db_conn, db_name):
    cur = db_conn.cursor()
    cur.execute("SELECT 1 FROM pg_database WHERE datname='{0}'".format(db_name))
    db_exists = cur.fetchone()
    if db_exists is None:
        return False
    return True


def delete_database(db_conn, db_name):
    logging.info('Delete database with db_name={0}'.format(db_name))
    db_conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = db_conn.cursor()
    cur.execute("DROP DATABASE {0}".format(db_name))
    logging.debug('Database deletion finished with status={0}'.format(cur.statusmessage))
    if cur.statusmessage != "DROP DATABASE":
        raise Exception('Unable to delete database')


def check_extension_exists(db_conn, extension):
    cur = db_conn.cursor()
    cur.execute("SELECT 1 FROM pg_extension WHERE extname='{0}'".format(extension))
    db_exists = cur.fetchone()
    if db_exists is None:
        return False
    return True


def create_extension(db_conn, extension):
    logging.info('Create extension with name={0}'.format(extension))
    db_conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = db_conn.cursor()
    cur.execute("CREATE EXTENSION {0}".format(extension))
    logging.debug('Extension creationg finished with status={0}'.format(cur.statusmessage))
    if cur.statusmessage != "CREATE EXTENSION":
        raise Exception('Unable to create extension')


def grant_role(db_conn, from_role, to_role):
    logging.info('Grant role with from_role={0}, to_role={1}'.format(from_role, to_role))
    db_conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = db_conn.cursor()
    cur.execute("GRANT {0} TO {1}".format(from_role, to_role))
    logging.debug('Grant role finished with status={0}'.format(cur.statusmessage))
    if cur.statusmessage != "GRANT ROLE":
        raise Exception('Unable to grant role')


def reassign_owned(db_conn, from_role, to_role):
    logging.info('Reassing owned by from_role={0}, to_role={1}'.format(from_role, to_role))
    db_conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = db_conn.cursor()
    cur.execute("REASSIGN OWNED BY {0} TO {1}".format(from_role, to_role))
    logging.debug('Reassign owned finished with status={0}'.format(cur.statusmessage))
    if cur.statusmessage != "REASSIGN OWNED":
        raise Exception('Unable to reassign')


def drop_owned(db_conn, role):
    logging.info('Drop owned by role={0}'.format(role))
    db_conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = db_conn.cursor()
    cur.execute("DROP OWNED BY {0}".format(role))
    logging.debug('Drop owned finished with status={0}'.format(cur.statusmessage))
    if cur.statusmessage != "DROP OWNED":
        raise Exception('Unable to drop owned')
