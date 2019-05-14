from dyn.tm.session import DynectSession
from dyn.tm.zones import Zone

from fabric.api import run, settings, env
from termcolor import colored

env.user = 'jenkins'
env.roledefs = {
    'local': ['localhost'],
}


def dynect_public_azure(machine_name, api_key, node_subdomain):
    with settings(warn_only=False):
        client_address = run('nslookup ' + machine_name + '-lb.eastus.cloudapp.azure.com | grep "Address: ." |'
                                                          ' cut -d\':\' -f2')
        client_address.strip()
        print(colored('IP Address: ' + client_address, 'red', attrs=['bold']))

        DynectSession("your-dynect-group", "your-dynect-username", api_key)

        my_zone = Zone('your-dynect-domain')
        my_zone.add_record(machine_name + '.' + node_subdomain, 'A', client_address)
        my_zone.ttl = 30

        node = my_zone.get_node(machine_name + '.' + node_subdomain)
        print(colored(node.get_any_records(), 'blue', attrs=['bold']))
        my_zone.publish()

        print(colored('===========================================', 'red'))
        print(colored(machine_name + '.feeds A-Record IP: ' + client_address, 'red', attrs=['bold']))
        print(colored('===========================================', 'red'))


def dynect_private(machine_name, api_key, node_subdomain):
    with settings(warn_only=False):
        client_address = run('docker-machine ip ' + machine_name)
        print(colored('IP Address: ' + client_address, 'red', attrs=['bold']))

        DynectSession("your-dynect-group", "your-dynect-username", api_key)

        my_zone = Zone('your-dynect-domain')
        my_zone.add_record(machine_name + '.' + node_subdomain, 'A', client_address)
        my_zone.ttl = 30

        node = my_zone.get_node(machine_name + '.' + node_subdomain)
        print(colored(node.get_any_records(), 'blue', attrs=['bold']))
        my_zone.publish()

        print(colored('===========================================', 'red'))
        print(colored(machine_name + '.feeds A-Record IP: ' + client_address, 'red', attrs=['bold']))
        print(colored('===========================================', 'red'))


def dynect_rm(machine_name, api_key, node_subdomain):
    with settings(warn_only=False):
        DynectSession("your-dynect-group", "your-dynect-username", api_key)
        my_zone = Zone('your-dynect-domain')
        print(machine_name + '.' + node_subdomain)
        node = my_zone.get_node(machine_name + '.' + node_subdomain)
        print(colored('DELETING :' + machine_name + '.' + node_subdomain + ' and its Records', 'blue', attrs=['bold']))
        node.delete()
        my_zone.publish()
