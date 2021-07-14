#!/usr/bin/python3
#
# PostgreSQL helper functions.
#
import psycopg2
from psycopg2.extensions import ISOLATION_LEVEL_AUTOCOMMIT
import logging

def connect(db_host, db_name, db_user, db_pass):
    logging.debug(f"Connecting to database using host={db_host}, dbname={db_name}")
    conn = None
    try:
        conn = psycopg2.connect(host=db_host, dbname=db_name, user=db_user, password=db_pass)
    except (Exception, psycopg2.DatabaseError) as error:
        logging.error('Unable to connect to database')
        raise Exception('Unable to connect to database')
    return conn

def close(db_conn):
    logging.debug("Close database connection")
    if db_conn is not None:
        db_conn.close()

def execute(db_conn, sql):
    db_conn.set_isolation_level(ISOLATION_LEVEL_AUTOCOMMIT)
    cur = db_conn.cursor()
    cur.execute(sql)
    logging.debug(f"Statement [{sql}] finished with status={cur.statusmessage}")
    return cur.statusmessage

def show_version(db_conn):
    cur = db_conn.cursor()
    cur.execute("SELECT version()")
    return cur.fetchone()

def check_database_exists(db_conn, db_name):
    logging.debug(f"Checking if database {db_name} exists")
    cur = db_conn.cursor()
    cur.execute(f"SELECT 1 FROM pg_database WHERE datname='{db_name}'")
    db_exists = cur.fetchone()
    if db_exists is None:
        return False
    return True

def create_database(db_conn, db_name):
    logging.debug(f"Creating database with db_name={db_name} (...)")
    if check_database_exists(db_conn, db_name):
        logging.debug("Database already exists. (SKIP)")
        return
    
    sql = f"CREATE DATABASE {db_name}"
    if execute(db_conn, sql) != "CREATE DATABASE":
        raise Exception("Unable to create database")
    logging.debug(f"Created database with db_name={db_name} (OK)")

def revoke_public_permissions(db_conn, db_name):
    logging.debug(f"Revoking public permissions from db_name={db_name} (...)")
    sql = f"""
-- Revoke all default permissions from public schema;
REVOKE ALL ON DATABASE {db_name} FROM PUBLIC;
REVOKE CREATE ON SCHEMA public FROM PUBLIC;
"""
    if execute(db_conn, sql) != "REVOKE":
        raise Exception("Unable to revoke all")
    logging.debug(f"Revoked public permissions. (OK)")

def check_role_exists(db_conn, role_name):
    logging.debug(f"Checking if role_name={role_name} exists")
    cur = db_conn.cursor()
    cur.execute(f"SELECT 1 FROM pg_roles WHERE rolname='{role_name}'")
    role_exists = cur.fetchone()
    if role_exists is None:
        return False
    return True

def create_role(db_conn, db_name, role_name):
    logging.debug(f"Creating role with role_name={role_name} (...)")
    sql = f"CREATE ROLE {role_name}"
    if execute(db_conn, sql) != "CREATE ROLE":
        raise Exception("Unable to create role")
    logging.debug(f"Created role. (OK)")

def create_readonly_role(db_conn, db_name, role_name):
    logging.debug(f"Creating read-only role (...)")
    if not check_role_exists(db_conn, role_name):
        create_role(db_conn, db_name, role_name)
    else:
        logging.debug(f"Role {role_name} already exists. (SKIP)")

    logging.debug(f"Setting up read-only permissions to role_name={role_name} (...)")
    sql = f"""
-- Grant connect and usage to the role;
GRANT CONNECT ON DATABASE {db_name} TO {role_name};
GRANT USAGE ON SCHEMA public TO {role_name};
-- Grant select on all existing and future tables;
GRANT SELECT ON ALL TABLES IN SCHEMA public TO {role_name};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT ON TABLES TO {role_name};
"""
    if execute(db_conn, sql) != "ALTER DEFAULT PRIVILEGES":
        raise Exception("Unable to set up read-only permissions")
    logging.debug(f"Set up read-only permissions. (OK)")

def create_readowrite_role(db_conn, db_name, role_name):
    logging.debug(f"Creating read/write role (...)")
    if not check_role_exists(db_conn, role_name):
        create_role(db_conn, db_name, role_name)
    else:
        logging.debug(f"Role {role_name} already exists. (SKIP)")
    
    logging.debug(f"Setting up read/write permissions to role_name={role_name} (...)")
    sql = f"""
-- Grant connect, usage and create to the role;
GRANT CONNECT ON DATABASE {db_name} TO {role_name};
GRANT USAGE, CREATE ON SCHEMA public TO {role_name};
-- Grant write access on all existing and future tables;
GRANT SELECT, INSERT, UPDATE, DELETE ON ALL TABLES IN SCHEMA public TO {role_name};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT SELECT, INSERT, UPDATE, DELETE ON TABLES TO {role_name};
-- Also grant permissions to all existing and future sequences;
GRANT USAGE ON ALL SEQUENCES IN SCHEMA public TO {role_name};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT USAGE ON SEQUENCES TO {role_name};
-- Also grant permissions to all existing and future functions;
GRANT EXECUTE ON ALL FUNCTIONS IN SCHEMA public TO {role_name};
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT EXECUTE ON FUNCTIONS TO {role_name};
"""
    if execute(db_conn, sql) != "ALTER DEFAULT PRIVILEGES":
        raise Exception("Unable to set up read/write permissions")
    logging.debug(f"Set up read/write permissions. (OK)")

def check_user_exists(db_conn, db_user):
    return check_role_exists(db_conn, db_user)

def create_user(db_conn, db_user, db_pass, role_name):
    logging.debug(f"Creating user with db_user={db_user}, role_name={role_name} (...)")

    if not check_user_exists(db_conn, db_user):
        sql = f"CREATE USER {db_user} WITH PASSWORD '{db_pass}'"
        if execute(db_conn, sql) != "CREATE ROLE":
            raise Exception("Unable to create user")
        logging.debug(f"Created user. (OK)")
    else:
        logging.debug(f"User {db_user} already exists. (SKIP)")
    
    logging.debug(f"Granting role_name={role_name} to db_user={db_user} (...)")
    sql = f"GRANT {role_name} TO {db_user}"
    if execute(db_conn, sql) != "GRANT ROLE":
        raise Exception("Unable to grant role to user")
    logging.debug(f"Granted role permissions. (OK)")
