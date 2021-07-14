#!/usr/local/bin/python3
import click
import os
import sys
import logging
import utils
import db


@click.group()
def cli():
    pass


@cli.command()
@click.option('--service-name', '-s', prompt='Enter the service name', help='The name of the service')
@click.option('--database-name', '-d', default="", help='Set a custom database name')
@click.option('--readonly-role-name', '-r', default="", help='Set a custom readonly role name')
@click.option('--readwrite-role-name', '-w', default="", help='Set a custom readwrite role name')
@click.option('--user-name', '-u', default="", help='Set a custom user name')
@click.option('--verbose', '-v', default=False, is_flag=True, help='Show verbose output')
def create(service_name, database_name, readonly_role_name, readwrite_role_name, user_name, verbose):
    """Create database, roles and user."""

    # -------------------------------------------------------------------------
    # 0. Init
    # -------------------------------------------------------------------------
    utils.configureLogging(verbose)

    # Look for DB credentials in .env file
    logging.debug(f"Looking for DB credentials in .env file...")
    config = utils.loadConfig()

    # -------------------------------------------------------------------------
    # 1. Connect to a default database to create the service database
    # -------------------------------------------------------------------------
    # Connect to db server
    initial_db_conn = db.connect(config["DB_HOST"], config["DB_NAME"], config["DB_USER"], config["DB_PASS"])
    
    # Create a db for the service
    service_db_name = database_name if database_name else f"{service_name}"
    logging.info(f"Create a database with name={service_db_name} for service={service_name}")
    db.create_database(initial_db_conn, service_db_name)
    
    # Close the db connection
    db.close(initial_db_conn)

    # -------------------------------------------------------------------------
    # 2. Connect to new service db to create roles and a user for the service
    # -------------------------------------------------------------------------
    # Connect to the new service database
    db_conn = db.connect(config["DB_HOST"], service_db_name, config["DB_USER"], config["DB_PASS"])

    # Revoke default, public permissions from public schema
    logging.info(f"Revoke default, public permissions from public schema")
    db.revoke_public_permissions(db_conn, service_db_name)

    # Create a read-only role
    service_readonly_role = readonly_role_name if readonly_role_name else f"role_{service_name}_readonly"
    logging.info(f"Create a read-only role with name={service_readonly_role}")
    db.create_readonly_role(db_conn, service_db_name, service_readonly_role)
    
    # Create a read/write role
    service_readwrite_role = readwrite_role_name if readwrite_role_name else f"role_{service_name}_readwrite"
    logging.info(f"Create a read/write role with name={service_readwrite_role}")
    db.create_readowrite_role(db_conn, service_db_name, service_readwrite_role)

    # Create a user for the service with read/write role grants
    service_user = user_name if user_name else f"user_{service_name}_app"
    service_pass = utils.getRandomPassword(30)
    logging.info(f"Create a user with name={service_user}")
    db.create_user(db_conn, service_user, service_pass, service_readwrite_role)

    # Output user pass to a file
    utils.writeFile(".pass", service_pass)

    # Close the db connection
    db.close(db_conn)


@cli.command()
@click.option('--service-name', prompt='Enter the service name', help='The name of the service')
@click.option('--verbose', '-v', default=False, is_flag=True, help='Show verbose output')
def delete(service_name, verbose):
    """Delete database, roles and user."""
    pass


@cli.command()
@click.option('--service-name', prompt='Enter the service name', help='The name of the service')
def list(service_name):
    """List database, roles and user."""
    # >>
    # >> Database
    # >> --------
    # foo
    #
    # >>
    # >> Roles
    # >> -----
    # role_readonly_foo
    # role_readwrite_foo
    #
    # >>
    # >> User
    # >> ----
    # user_app_foo
    pass


@cli.command()
@click.option('--verbose', '-v', default=False, is_flag=True, help='Show verbose output')
def test(verbose):
    """Test database connectivity."""
    utils.configureLogging(verbose)
    config = utils.loadConfig()
    logging.debug(f"Testing DB connection...")
    db_conn = db.connect(config["DB_HOST"], config["DB_NAME"], config["DB_USER"], config["DB_PASS"])
    logging.info(db.show_version(db_conn))
    db.close(db_conn)


if __name__ == '__main__':
    cli(obj={})
