# DNS Helper

# Overview
These scripts were designed to complement the `/aws/route53` and `/dns` groovy modules.
They can be used for creating/deleting the PostgreSQL resources (database, role, user) that are generally needed for such dynamic environments.

# Requirements
- python >= 3.5
    - eg:
        ```
        $ python3 -V
        Python 3.5.2
        ```

- Run: `pip3 install -r /requirements.txt`

# Usage

```
#!/bin/bash

fab -f ${jenkinsModulesPath}/python/dns/jenkins_dns_aws_route53_ec2_profile.py -R local \
create_resources_record_sets:"${dnsRecordSetName}","${dnsRecordSetValue}","${dnsRecordSetComment}", \
"${dnsRecordSetType}","${dnsHostedZoneId}","${awsRegion}" \
```

