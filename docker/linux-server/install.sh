#!/bin/bash
if [ -f .env ]; then
source .env
else 
 echo ".env file not found"
 exit 1
fi

#check what is the local distribution
if [ -f /etc/os-release ]; then
    # freedesktop.org and systemd
    . /etc/os-release
    OS=$NAME
    VER=$VERSION_ID
elif type lsb_release >/dev/null 2>&1; then
    # linuxbase.org
    OS=$(lsb_release -si)
    VER=$(lsb_release -sr)
fi
echo "OS = $OS."
#update this script when you want to perform some action on the server (need to implement something like liquibase)


#Docker installation
which docker
if [ $? -eq 0 ]
then
  echo "docker already installed"
  #TODO test docker version
else
  echo "we install docker"
  curl -fsSL https://get.docker.com -o get-docker.sh
  sh get-docker.sh
  rm get-docker.sh
fi

#Docker compose installation
which docker-compose
if [ $? -eq 0 ]
then
  echo "docker-compose already installed"
  #TODO test docker-compose version
else
  echo "we install docker-compose"
  curl -L https://github.com/docker/compose/releases/download/1.25.5/docker-compose-Linux-x86_64 -o /usr/local/bin/docker-compose
  chmod +x /usr/local/bin/docker-compose
  #TODO check the install is successfull
fi


#replace the variables in nginx config
sed -i "s/DOMAIN_NAME/$DOMAIN_NAME/g" ./conf/nginx/nginx.conf

if [ ! -r /home/${SERVER_NAME}/conf/ssl/domain.key ] || [ ! -r /home/${SERVER_NAME}/conf/ssl/domain.crt ]
then
   #certbot certonly --standalone
      if [ ! -e /etc/letsencrypt/live/$DOMAIN_NAME/cert.pem ]
      then
         echo "Generating ssl certificate for $DOMAIN_NAME using letsencrypt"
         apt-get update
         if [ "$OS" == "Ubuntu" ] 
         then
               apt-get install software-properties-common
               add-apt-repository universe
               add-apt-repository ppa:certbot/certbot
               apt-get update
         fi
         apt-get install certbot python3-certbot-nginx
         certbot certonly --nginx -d $DOMAIN_NAME --non-interactive --agree-tos -m $ADMIN_EMAIL
      fi
      if [ -e /etc/letsencrypt/live/$DOMAIN_NAME/cert.pem ]
      then
        echo "Generating ssl certificate for $DOMAIN_NAME using letsencrypt"
        #openssl x509 -outform der -in /etc/letsencrypt/live/$DOMAIN_NAME/cert.pem -out ./ssl/domain.crt
        #openssl pkey -in /etc/letsencrypt/live/$DOMAIN_NAME/privkey.pem -out ./ssl/domain.key
        cp /etc/letsencrypt/live/$DOMAIN_NAME/fullchain.pem  /home/${SERVER_NAME}/conf/ssl/
        mv /home/${SERVER_NAME}/conf/ssl/fullchain.pem /home/${SERVER_NAME}/conf/ssl/domain.crt
        cp /etc/letsencrypt/live/$DOMAIN_NAME/privkey.pem  /home/${SERVER_NAME}/conf/ssl/
        mv /home/${SERVER_NAME}/conf/ssl/privkey.pem /home/${SERVER_NAME}/conf/ssl/domain.key
      else
         echo "Error generating the certificate,  /etc/letsencrypt/live/$DOMAIN_NAME/cert.pem not found" 
         exit 1
      fi
fi
if [ ! -r /home/${SERVER_NAME}/conf/ssl/domain.key ] || [ ! -r /home/${SERVER_NAME}/conf/ssl/domain.crt ]
then
     echo "Error while converting the certificate, we cant find ./ssl/domain.key or ./ssl/domain.crt" 
     exit 1
fi

