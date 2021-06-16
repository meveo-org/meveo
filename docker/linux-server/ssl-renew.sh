#!/bin/bash -e

# DOMAIN_NAME and SERVER_NAME will be replaced by the correct value in install.sh

docker-compose stop nginx

certbot renew
kill $(ps aux | grep '[n]ginx' | awk '{print $2}')

cp /etc/letsencrypt/live/DOMAIN_NAME/fullchain.pem  /home/SERVER_NAME/conf/ssl/domain.crt
cp /etc/letsencrypt/live/DOMAIN_NAME/privkey.pem  /home/SERVER_NAME/conf/ssl/domain.key

docker-compose up -d nginx
