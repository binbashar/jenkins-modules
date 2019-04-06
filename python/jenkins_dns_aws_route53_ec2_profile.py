from fabric.decorators import task
from fabric.api import settings, env

import boto3
import sys

reload(sys)
sys.setdefaultencoding('utf8')

env.user = 'jenkins'
env.roledefs = {
    'local': ['localhost'],
}


@task
def list_hostedzones(region_name_arg='us-east-1'):
    """
Retrieves a list of the public and private hosted zones that are associated with the current AWS account.
The response includes a HostedZones child element for each hosted zone.

Amazon Route 53 returns a maximum of 100 items in each response.
If you have a lot of hosted zones, you can use the maxitems parameter to list them in groups of up to 100.

    :param region_name_arg: (string) -- AWS account region

    eg: $ fab -R local aws_route53_fab.list_hostedzones:"profile company","us-east-1"
    """
    with settings(warn_only=False):
        # from the [profile btr-tunubi] section of ~/.aws/credentials.
        session = boto3.Session(region_name=region_name_arg)

        print("Connecting to Route53")
        aws_dns = session.client('route53')
        try:
            response = aws_dns.list_hosted_zones(
                MaxItems='100'
            )

            for hosted_zones in response['HostedZones']:

                hostedzone_id = hosted_zones.get('Id')
                hostedzone_name = hosted_zones.get('Name')
                hostedzone_caller_ref = hosted_zones.get('CallerReference')
                hostedzone_conf_privzone = hosted_zones.get('Config').get('PrivateZone')
                hostedzone_record_count = hosted_zones.get('ResourceRecordSetCount')

                hostedzone_linkedserv_serv = ''
                if hosted_zones.get('LinkedService') is not None:
                    hostedzone_linkedserv_serv = hosted_zones.get('LinkedService').get('ServicePrincipal')

                print "Route53 Hosted Zones:"
                print 'hostedzone_id: ' + str(hostedzone_id)
                print 'hostedzone_name: ' + str(hostedzone_name)
                print 'hostedzone_caller_ref: ' + str(hostedzone_caller_ref)
                print 'hostedzone_conf_privzone: ' + str(hostedzone_conf_privzone)
                print 'hostedzone_record_count: ' + str(hostedzone_record_count)
                print 'hostedzone_linkedserv_serv: ' + str(hostedzone_linkedserv_serv)

        except Exception as error:
            # print colored(error, 'red')
            print "exception :" + str(error)


@task
def list_resources_record_sets(hosted_zone_id_arg, region_name_arg='us-east-1'):
    """
Lists the resource record sets in a specified hosted zone.

    :param hosted_zone_id_arg: (string) The ID of the hosted zone that contains the resource record sets that you want
     to list.

    :param region_name_arg: (string) -- AWS account region

    eg: $ fab -R local aws_route53_fab.list_resources_record_sets:"profile company","/hostedzone/Z2WI7FSN6LUJNR",
    "us-east-1"
    """
    with settings(warn_only=False):
        # from the [profile btr-tunubi] section of ~/.aws/credentials.
        session = boto3.Session(region_name=region_name_arg)

        print("Connecting to Route53")
        aws_dns = session.client('route53')

        hosted_zone_id = hosted_zone_id_arg

        try:
            response = aws_dns.list_resource_record_sets(
                HostedZoneId=hosted_zone_id,
                # StartRecordName='string',
                # StartRecordType='SOA' | 'A' | 'TXT' | 'NS' | 'CNAME' | 'MX' | 'NAPTR' | 'PTR' | 'SRV' | 'SPF' | 'AAAA'
                #  | 'CAA',
                # StartRecordIdentifier='string',
                MaxItems='100'
            )

            for resource_record_sets in response['ResourceRecordSets']:

                resource_record_sets_name = resource_record_sets.get('Name')
                resource_record_sets_type = resource_record_sets.get('Type')
                resource_record_sets_region = resource_record_sets.get('Region')
                resource_record_sets_ttl = resource_record_sets.get('TTL')

                resource_record_sets_record_value = ""
                for record_values in resource_record_sets.get('ResourceRecords'):
                    resource_record_sets_record_value = record_values.get('Value')

                print ""
                print "Route53 Rosource record sets for zone: " + hosted_zone_id
                print 'resource_record_sets_name: ' + str(resource_record_sets_name)
                print 'resource_record_sets_type: ' + str(resource_record_sets_type)
                print 'resource_record_sets_region: ' + str(resource_record_sets_region)
                print 'resource_record_sets_ttl: ' + str(resource_record_sets_ttl)
                print 'resource_record_sets_record_value: ' + str(resource_record_sets_record_value)

        except Exception as error:
            # print colored(error, 'red')
            print "exception :" + str(error)


@task
def check_resources_record_sets(record_set_name_arg, hosted_zone_id_arg, region_name_arg='us-east-1'):
    """
Lists the resource record sets in a specified hosted zone and check if record_set_name_arg exists.

    :param record_set_name_arg: (string) -- record set name argument eg: yoursubdomain.yourdomain.com

    :param hosted_zone_id_arg: (string) The ID of the hosted zone that contains the resource record sets that you want
     to list.

    :param region_name_arg: (string) -- AWS account region

    eg: $ fab -R local aws_route53_fab.check_resources_record_sets:"profile company","passbolt.example.com.ar.",
    "/hostedzone/Z2WI7FSN6LUJNR","us-east-1"

    """
    with settings(warn_only=False):
        # from the [profile btr-tunubi] section of ~/.aws/credentials.
        session = boto3.Session(region_name=region_name_arg)

        print("Connecting to Route53")
        aws_dns = session.client('route53')

        hosted_zone_id = hosted_zone_id_arg
        record_set_name = record_set_name_arg

        try:
            response = aws_dns.list_resource_record_sets(
                HostedZoneId=hosted_zone_id,
                # StartRecordName='string',
                # StartRecordType='SOA' | 'A' | 'TXT' | 'NS' | 'CNAME' | 'MX' | 'NAPTR' | 'PTR' | 'SRV' | 'SPF' | 'AAAA'
                #  | 'CAA',
                # StartRecordIdentifier='string',
                MaxItems='100'
            )

            resource_record_sets_name = ""
            for resource_record_sets in response['ResourceRecordSets']:

                resource_record_sets_name = resource_record_sets.get('Name')

                if resource_record_sets_name == record_set_name:
                    print ""
                    print "Route53 Rosource record sets for zone: " + hosted_zone_id
                    print 'resource_record_sets_name: ' + str(resource_record_sets_name) + ' EXISTS!'
                    return True

            print ""
            print "Route53 Rosource record sets for zone: " + hosted_zone_id
            print 'resource_record_sets_name: ' + str(resource_record_sets_name) + ' does NOT exists!'
            return False

        except Exception as error:
            # print colored(error, 'red')
            print "exception :" + str(error)


@task
def update_resources_record_sets(record_set_name_arg, record_set_value_arg, record_set_comment_arg,
                                 record_set_type_arg, hosted_zone_id_arg, region_name_arg='us-east-1'):
    """
Updates a resource record set, which contains authoritative DNS information for a specified domain
name or subdomain name. For example, you can use ChangeResourceRecordSets to create a resource record set that routes
traffic for test.example.com to a web server that has an IP address of 192.0.2.44.

Use ChangeResourceRecordsSetsRequest to perform the following actions:
UPSERT : If a resource record set does not already exist, AWS creates it.
If a resource set does exist, Amazon Route 53 updates it with the values in the request.

    :param record_set_name_arg: (string) --  The name of the domain you want to perform the action on.
    Enter a fully qualified domain name, for example, www.example.com . You can optionally include a trailing dot.
    If you omit the trailing dot, Amazon Route 53 still assumes that the domain name that you specify is fully
    qualified. This means that Amazon Route 53 treats www.example.com (without a trailing dot) and www.example.com.
    (with a trailing dot) as identical.

    For information about how to specify characters other than a-z , 0-9 , and - (hyphen) and how to specify
    internationalized domain names, see DNS Domain Name Format in the Amazon Route 53 Developer Guide .

    You can use the asterisk (*) wildcard to replace the leftmost label in a domain name, for example, *.example.com .
    Note the following:

    The * must replace the entire label. For example, you can't specify *prod.example.com or prod*.example.com .
    The * can't replace any of the middle labels, for example, marketing.*.example.com.
    If you include * in any position other than the leftmost label in a domain name, DNS treats it as an * character
    (ASCII 42), not as a wildcard.

    :param record_set_value_arg: (string) -- The current or new DNS record value, not to exceed 4,000 characters.
    For descriptions about how to format Value for different record types, see Supported DNS Resource Record Types
    in the Amazon Route 53 Developer Guide .

    You can specify more than one value for all record types except CNAME and SOA .
    Note: If you're creating an alias resource record set, omit Value

    :param record_set_comment_arg: (string) -- Record set comment

    :param record_set_type_arg: (string) -- The DNS record type. For information about different record types and how
    data is encoded for them, see Supported DNS Resource Record Types in the Amazon Route 53 Developer Guide .

    Valid values for basic resource record sets: A | CNAME.
    NOTE: AWS support more types, however we do not for the moment.

    :param hosted_zone_id_arg: (string) The ID of the hosted zone that contains the resource record sets that you want
     to list.

    :param region_name_arg: (string) -- AWS account region

    eg: $ fab -R local aws_route53_fab.update_resources_record_sets:"profile company","passbolt-test.company.com.ar.",
    "34.203.224.136","passbolt test record","A","/hostedzone/Z2WI7FSN6LUJNR","us-east-1"
    """
    with settings(warn_only=False):
        # from the [profile btr-tunubi] section of ~/.aws/credentials.
        session = boto3.Session(region_name=region_name_arg)

        print("Connecting to Route53")
        aws_dns = session.client('route53')

        region_name = str(region_name_arg)
        hosted_zone_id = hosted_zone_id_arg
        record_set_name = str(record_set_name_arg)
        record_set_value = str(record_set_value_arg)
        record_set_comment = str(record_set_comment_arg)
        # 'Type': 'SOA' | 'A' | 'TXT' | 'NS' | 'CNAME' | 'MX' | 'NAPTR' | 'PTR' | 'SRV' | 'SPF' | 'AAAA' | 'CAA',
        record_set_type = str(record_set_type_arg)

        if (record_set_type == 'A' or record_set_type == 'CNAME') and \
                check_resources_record_sets(record_set_name, hosted_zone_id_arg, region_name):
            print ""
            print "SUPPORTED RECORD TYPE and EXISTS"
            print ""

            try:
                response = aws_dns.change_resource_record_sets(
                    HostedZoneId=hosted_zone_id,
                    ChangeBatch={
                        'Comment': record_set_comment,
                        'Changes': [
                            {
                                'Action': 'UPSERT',
                                'ResourceRecordSet': {
                                    'Name': record_set_name,
                                    'Type': record_set_type,
                                    'TTL': 60,
                                    'ResourceRecords': [
                                        {
                                            'Value': record_set_value
                                        },
                                    ]
                                }
                            },
                        ]
                    }
                )

                print response
                print ''
                print 'record set: ' + str(record_set_name) + ' SUCCESSFULLY UPDATED'

            except Exception as error:
                # print colored(error, 'red')
                print "exception :" + str(error)

        else:
            print ""
            print "NOT SUPPORTED RECORD TYPE OR RECORD NAME DOES NOT EXISTS"
            print ""

@task
def create_resources_record_sets(record_set_name_arg, record_set_value_arg, record_set_comment_arg,
                                 record_set_type_arg, hosted_zone_id_arg, region_name_arg='us-east-1'):
    """
Creates a resource record set, which contains authoritative DNS information for a specified domain
name or subdomain name. For example, you can use ChangeResourceRecordSets to create a resource record set that routes
traffic for test.example.com to a web server that has an IP address of 192.0.2.44.

Use ChangeResourceRecordsSetsRequest to perform the following actions:
CREATE : Creates a resource record set that has the specified values.

    :param record_set_name_arg: (string) --  The name of the domain you want to perform the action on.
    Enter a fully qualified domain name, for example, www.example.com . You can optionally include a trailing dot.
    If you omit the trailing dot, Amazon Route 53 still assumes that the domain name that you specify is fully
    qualified. This means that Amazon Route 53 treats www.example.com (without a trailing dot) and www.example.com.
    (with a trailing dot) as identical.

    For information about how to specify characters other than a-z , 0-9 , and - (hyphen) and how to specify
    internationalized domain names, see DNS Domain Name Format in the Amazon Route 53 Developer Guide .

    You can use the asterisk (*) wildcard to replace the leftmost label in a domain name, for example, *.example.com .
    Note the following:

    The * must replace the entire label. For example, you can't specify *prod.example.com or prod*.example.com .
    The * can't replace any of the middle labels, for example, marketing.*.example.com.
    If you include * in any position other than the leftmost label in a domain name, DNS treats it as an * character
    (ASCII 42), not as a wildcard.

    :param record_set_value_arg: (string) -- The current or new DNS record value, not to exceed 4,000 characters.
    For descriptions about how to format Value for different record types, see Supported DNS Resource Record Types
    in the Amazon Route 53 Developer Guide .

    You can specify more than one value for all record types except CNAME and SOA .
    Note: If you're creating an alias resource record set, omit Value

    :param record_set_comment_arg: (string) -- Record set comment

    :param record_set_type_arg: (string) -- The DNS record type. For information about different record types and how
    data is encoded for them, see Supported DNS Resource Record Types in the Amazon Route 53 Developer Guide .

    Valid values for basic resource record sets: A | CNAME.
    NOTE: AWS support more types, however we do not for the moment.

    :param hosted_zone_id_arg: (string) The ID of the hosted zone that contains the resource record sets that you want
     to list.

    :param region_name_arg: (string) -- AWS account region

    eg: $ fab -R local aws_route53_fab.create_resources_record_sets:"profile binbash","passbolt-test.binbash.com.ar.",
    "35.190.149.186","passbolt test record","A","/hostedzone/Z2WI7FSN6LUJNR","us-east-1"
    """
    with settings(warn_only=False):
        # from the [profile btr-tunubi] section of ~/.aws/credentials.
        session = boto3.Session(region_name=region_name_arg)

        print("Connecting to Route53")
        aws_dns = session.client('route53')

        region_name = str(region_name_arg)
        hosted_zone_id = hosted_zone_id_arg
        record_set_name = str(record_set_name_arg)
        record_set_value = str(record_set_value_arg)
        record_set_comment = str(record_set_comment_arg)
        # 'Type': 'SOA' | 'A' | 'TXT' | 'NS' | 'CNAME' | 'MX' | 'NAPTR' | 'PTR' | 'SRV' | 'SPF' | 'AAAA' | 'CAA',
        record_set_type = str(record_set_type_arg)

        if record_set_type == 'A' or record_set_type == 'CNAME' and \
                check_resources_record_sets(record_set_name, hosted_zone_id_arg, region_name):
            print ""
            print "SUPPORTED RECORD TYPE and RECORD ALREADY EXISTS"
            print ""

        elif record_set_type == 'A' or record_set_type == 'CNAME':

            print ""
            print "SUPPORTED RECORD TYPE and RECORD WILL BE CREATED"
            print ""

            try:
                response = aws_dns.change_resource_record_sets(
                    HostedZoneId=hosted_zone_id,
                    ChangeBatch={
                        'Comment': record_set_comment,
                        'Changes': [
                            {
                                'Action': 'CREATE',
                                'ResourceRecordSet': {
                                    'Name': record_set_name,
                                    'Type': record_set_type,
                                    'TTL': 60,
                                    'ResourceRecords': [
                                        {
                                            'Value': record_set_value
                                        },
                                    ]
                                }
                            },
                        ]
                    }
                )

                print response

                record_set_status = 'PENDING'
                while record_set_status == 'PENDING':
                    if check_resources_record_sets(record_set_name, hosted_zone_id_arg, region_name):
                        record_set_status = 'INSYNC'

                if record_set_status == 'INSYNC':
                    print ''
                    print 'record set: ' + str(record_set_name) + ' SUCCESSFULLY CREATED'

                else:
                    print 'record set: ' + str(record_set_name) + ' NOT CREATED'

            except Exception as error:
                # print colored(error, 'red')
                print "exception :" + str(error)

        else:
            print ""
            print "NOT SUPPORTED RECORD TYPE"
            print ""

@task
def delete_resources_record_sets(record_set_name_arg, record_set_value_arg, record_set_comment_arg,
                                 record_set_type_arg, hosted_zone_id_arg, region_name_arg='us-east-1'):
    """
Delete a resource record set, which contains authoritative DNS information for a specified domain
name or subdomain name. For example, you can use ChangeResourceRecordSets to create a resource record set that routes
traffic for test.example.com to a web server that has an IP address of 192.0.2.44.

Use ChangeResourceRecordsSetsRequest to perform the following actions:
DELETE : Deletes an existing resource record set that has the specified values.

    :param record_set_name_arg: (string) --  The name of the domain you want to perform the action on.
    Enter a fully qualified domain name, for example, www.example.com . You can optionally include a trailing dot.
    If you omit the trailing dot, Amazon Route 53 still assumes that the domain name that you specify is fully
    qualified. This means that Amazon Route 53 treats www.example.com (without a trailing dot) and www.example.com.
    (with a trailing dot) as identical.

    For information about how to specify characters other than a-z , 0-9 , and - (hyphen) and how to specify
    internationalized domain names, see DNS Domain Name Format in the Amazon Route 53 Developer Guide .

    You can use the asterisk (*) wildcard to replace the leftmost label in a domain name, for example, *.example.com .
    Note the following:

    The * must replace the entire label. For example, you can't specify *prod.example.com or prod*.example.com .
    The * can't replace any of the middle labels, for example, marketing.*.example.com.
    If you include * in any position other than the leftmost label in a domain name, DNS treats it as an * character
    (ASCII 42), not as a wildcard.

    :param record_set_value_arg: (string) -- The current or new DNS record value, not to exceed 4,000 characters.
    For descriptions about how to format Value for different record types, see Supported DNS Resource Record Types
    in the Amazon Route 53 Developer Guide .

    You can specify more than one value for all record types except CNAME and SOA .
    Note: If you're creating an alias resource record set, omit Value

    :param record_set_comment_arg: (string) -- Record set comment

    :param record_set_type_arg: (string) -- The DNS record type. For information about different record types and how
    data is encoded for them, see Supported DNS Resource Record Types in the Amazon Route 53 Developer Guide .

    Valid values for basic resource record sets: A | CNAME.
    NOTE: AWS support more types, however we do not for the moment.

    :param hosted_zone_id_arg: (string) The ID of the hosted zone that contains the resource record sets that you want
     to list.

    :param region_name_arg: (string) -- AWS account region

    eg: $ fab -R local aws_route53_fab.delete_resources_record_sets:"profile company","passbolt.company.com.ar.",
    "35.190.149.186","passbolt test record","A","/hostedzone/Z2WI7FSN6LUJNR","us-east-1"
    """
    with settings(warn_only=False):
        # from the [profile btr-tunubi] section of ~/.aws/credentials.
        session = boto3.Session(region_name=region_name_arg)

        print("Connecting to Route53")
        aws_dns = session.client('route53')

        region_name = str(region_name_arg)
        hosted_zone_id = hosted_zone_id_arg
        record_set_name = str(record_set_name_arg)
        record_set_value = str(record_set_value_arg)
        record_set_comment = str(record_set_comment_arg)
        # 'Type': 'SOA' | 'A' | 'TXT' | 'NS' | 'CNAME' | 'MX' | 'NAPTR' | 'PTR' | 'SRV' | 'SPF' | 'AAAA' | 'CAA',
        record_set_type = str(record_set_type_arg)

        if (record_set_type == 'A' or record_set_type == 'CNAME') and \
                check_resources_record_sets(record_set_name, hosted_zone_id_arg, region_name):
            print ""
            print "SUPPORTED RECORD TYPE and EXISTS it's going to be DELETED"
            print ""

            try:
                response = aws_dns.change_resource_record_sets(
                    HostedZoneId=hosted_zone_id,
                    ChangeBatch={
                        'Comment': record_set_comment,
                        'Changes': [
                            {
                                'Action': 'DELETE',
                                'ResourceRecordSet': {
                                    'Name': record_set_name,
                                    'Type': record_set_type,
                                    'TTL': 60,
                                    'ResourceRecords': [
                                        {
                                            'Value': record_set_value
                                        },
                                    ]
                                }
                            },
                        ]
                    }
                )

                print response
                print ''
                print 'record set: ' + str(record_set_name) + ' SUCCESSFULLY DELETED'

            except Exception as error:
                # print colored(error, 'red')
                print "exception :" + str(error)

        else:
            print ""
            print "NOT SUPPORTED RECORD TYPE OR RECORD NAME DOES NOT EXISTS NOT POSSIBLE TO DELETE"
            print ""
