# Push Button Environment - PostgreSQL Helper

# Overview
These scripts were designed to complement the implementation of push button environments. They can be used for creating/deleting the PostgreSQL resources (database, role, user) that are generally needed for such dynamic environments.

# Requirements
- python >= 3.7
- Run: `pip3 install -r /requirements.txt`

# Usage
Run `python3 create.py [...]` to create PostgreSQL resources -- inspect the script to identify all arguments you must provide to it. Then you can run `python3 delete.py [...]` to delete the resources.
Both scripts implement safety checks to avoid creating existing resources or deleting non-existing ones.
