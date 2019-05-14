#!/usr/bin/env bash

#ENV VARS
FUNC_TO_CALL=$1

dnsRecordSetComment=$2
jenkinsModulesPath=$3
dnsRecordSetType=$4
dnsHostedZoneId=$5
awsRegion=$6
dnsRecordSetName=$7
dnsRecordSetValue=$8
awsIamProfile=$9

function create_resources_record_sets () {
    fab -f ${jenkinsModulesPath}/python/dns/jenkins_dns_aws_route53.py -R local \
    create_resources_record_sets:"$awsIamProfile","$dnsRecordSetName","$dnsRecordSetValue","$dnsRecordSetComment","$dnsRecordSetType","$dnsHostedZoneId","$awsRegion"
}

function update_resources_record_sets () {
    fab -f ${jenkinsModulesPath}/python/dns/jenkins_dns_aws_route53.py -R local \
    update_resources_record_sets:"$awsIamProfile","$dnsRecordSetName","$dnsRecordSetValue","$dnsRecordSetComment","$dnsRecordSetType","$dnsHostedZoneId","$awsRegion"
}

function delete_resources_record_sets () {
    fab -f ${jenkinsModulesPath}/python/dns/jenkins_dns_aws_route53.py -R local \
    delete_resources_record_sets:"$awsIamProfile","$dnsRecordSetName","$dnsRecordSetValue","$dnsRecordSetComment","$dnsRecordSetType","$dnsHostedZoneId","$awsRegion"
}

function dnsCheckDomainExists () {
    touch tmp_result.txt
    nslookup ${dnsRecordSetComment} > tmp_result.txt
    cat tmp_result.txt | grep 't find'
    rm tmp_result.txt
}

# MAIN
$FUNC_TO_CALL