#!/usr/bin/python3
import os, sys
from yaenv.core import Env
import logging
import random
import string


def loadConfig(config_file = ".env"):
    if not os.path.exists(config_file):
        print("Missing .env file!")
        sys.exit(1)
    
    config = Env(config_file)
    return config

def configureLogging(verbose, format = '[%(levelname)s] %(message)s', encoding='utf-8'):
    logging_level = logging.DEBUG if verbose else logging.INFO
    logging.basicConfig(format=format, encoding=encoding, level=logging_level)

def getRandomPassword(password_length = 20):
    random_source = string.ascii_letters + string.digits #+ string.punctuation

    # Choose the uppercase/lowercase letters, numbers and symbols to include
    password = random.choice(string.ascii_lowercase)
    password += random.choice(string.ascii_uppercase)
    password += random.choice(string.digits)
    # password += random.choice(string.punctuation)

    # Generate other characters
    for i in range(password_length - 3):
        password += random.choice(random_source)

    password_list = list(password)

    # Shuffle all characters
    random.SystemRandom().shuffle(password_list)
    password = ''.join(password_list)

    return password

def writeFile(filename, content):
    f = open(filename, "w+")
    f.write(content)
    f.close()
