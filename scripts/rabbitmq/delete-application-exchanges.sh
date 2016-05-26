
#Delete Claim Exchanges
for i in `sudo rabbitmqctl list_exchanges | grep claim | tr -s "\t" " " |cut -d " " -f 1`; do curl -i -u guest:guest -H "content-type:application/json" -XDELETE "http://localhost:15672/api/exchanges/%2f/$i";done;
