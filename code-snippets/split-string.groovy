import groovy.transform.Field

//@Field def ssl_cert_url_1 = ["domain.domain.com", "currnet_cname_or_ip", "dns_record_type eg: A"]
@Field def ssl_cert_url_1_sr = ["prometheus.domain.com", "172.23.32.132", "A"]
@Field def ssl_cert_url_2_sr = ["grafana.domain.com", "172.23.32.132", "A"]
@Field def ssl_cert_url_3_sr = ["passbolt.domain.com", "172.23.32.152","A"]
@Field def ssl_cert_url_4_sr = ["passbolt2.domain.com", "172.23.32.160","A"]

def ssl_certs_domains_list_size = 0
def ssl_certs_domains_list = []
def ssl_certs_domains_dns_list = []
def ssl_cert_domains_app_list = []
def ssl_certs_domains_dns_type_list = []


if (ssl_cert_url_1.size() >=1 && ssl_cert_url_2.size() >=1){
    ssl_certs_domains_list = ["${ssl_cert_url_1[1]}", "${ssl_cert_url_2[1]}"]
    ssl_certs_domains_app_env = ["${ssl_cert_url_1[0]}", "${ssl_cert_url_2[0]}"]
    ssl_certs_domains_dns_list = ["${ssl_cert_url_1[2]}", "${ssl_cert_url_2[2]}"]

    ssl_certs_domains_list_size = ssl_certs_domains_list.size()
} else if (ssl_cert_url_1_sr.size() >=1){
    ssl_certs_domains_list = ["${ssl_cert_url_1_sr[0]}","${ssl_cert_url_2_sr[0]}","${ssl_cert_url_3_sr[0]}","${ssl_cert_url_4_sr[0]}"]
    echo "ssl_certs_domains_list: ${ssl_certs_domains_list}"

    ssl_certs_domains_dns_list = ["${ssl_cert_url_1_sr[1]}","${ssl_cert_url_2_sr[1]}","${ssl_cert_url_3_sr[1]}","${ssl_cert_url_4_sr[1]}"]
    echo "ssl_certs_domains_dns_list: ${ssl_certs_domains_dns_list}"

    ssl_certs_domains_dns_type_list = ["${ssl_cert_url_1_sr[2]}","${ssl_cert_url_2_sr[2]}","${ssl_cert_url_3_sr[2]}","${ssl_cert_url_4_sr[2]}"]
    echo "ssl_certs_domains_dns_type_list: ${ssl_certs_domains_dns_type_list}"
    echo "ssl_certs_domains_dns_type_0: ${ssl_certs_domains_dns_type_list[0]}"
    echo "ssl_certs_domains_dns_type_1: ${ssl_certs_domains_dns_type_list[1]}"

    def ssl_cert_app_temp = ssl_cert_url_1_sr[0].split('\\.')
    ssl_cert_domains_app_list[0] = ssl_cert_app_temp[0]
    ssl_cert_app_temp = ssl_cert_url_2_sr[0].split('\\.')
    ssl_cert_domains_app_list[1] = ssl_cert_app_temp[0]
    ssl_cert_app_temp = ssl_cert_url_3_sr[0].split('\\.')
    ssl_cert_domains_app_list[2] = ssl_cert_app_temp[0]
    ssl_cert_app_temp = ssl_cert_url_4_sr[0].split('\\.')
    ssl_cert_domains_app_list[3] = ssl_cert_app_temp[0]
    echo "ssl_cert_domains_app_list: ${ssl_cert_domains_app_list}"
    echo "ssl_cert_domains_app_list: ${ssl_cert_domains_app_list[0]}"
    echo "ssl_cert_domains_app_list: ${ssl_cert_domains_app_list[1]}"

    ssl_certs_domains_list_size = ssl_certs_domains_list.size()
    echo "ssl_certs_domains_list_size: ${ssl_certs_domains_list_size}"
}
