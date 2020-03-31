Meveo
=====

Meveo is a platform that allow to develop and execute enterprise back and front applications
It is intended to be run by Wildfly 15 with full Jakarta EE stack under licence : AGPLv3.0

Local Installation
-------------------
You can build the war and deploy on wildfly 15 after configuring keycloack, postgres, infinispan, neo4j(optional), elasticsearch(optional),...

Or simply execute the docker images from docker hub.

For a simple postgres localhost deployment:

make sure you have docker and docker-compose installed

put the file https://raw.githubusercontent.com/meveo-org/meveo/master/docker-compose.yml on some directory

and run

'docker-compose up -d'

then meveo is running on https://localhost:8080/meveo

Installation with embedded keycloack and nginx
----------------------------------------------
Let assume you want to deploy meveo on a domain https://mydomain.org, that you already have a linux server with 4Go of Ram a public IP and that your domain DNS configuration correctly route to your public IP.

Let assume that you also have an ssl certificate in the form of 2 files certificate.crt and certificate.key (you might have to merge the crt files into one if several are provided, for instance you should concatenate certificate.crt and ca-bundled.crt provided by sslforfree)

Make sure you have docker and docker compose installed.

put the file https://raw.githubusercontent.com/meveo-org/meveo/master/install/nginx-keycloack/docker-compose-nginx.yml on some directory and rename it docker-compose.yml

put the file https://raw.githubusercontent.com/meveo-org/meveo/master/install/nginx-keycloack/nginx.conf in /root/nginx/ (update the docker-compose.yml file if you want to use another directory)

put the certificate.crt and certificate.key files in /root/nginx/ssl/ (update the docker-compose.yml file if you want to use another directory)

start meveo,postgres and nginx by running 

    docker-compose up -d
in the directory containing the docker-compose.yml file

Enter the meveo container to execute some bash command by running 

    docker exec -it meveo bash
then the followinf wildfly cli command (with the correct domain name instead of "mydomain.org")


    bin/jboss-cli.sh --connect --commands="/system-property=meveo.keycloak.url:write-attribute(name=value,value=https://mydomain.org/auth)"

    bin/jboss-cli.sh --connect --commands="/system-property=meveo.keycloak.fixed-hostname:write-attribute(name=value, value=mydomain.org)"

    bin/jboss-cli.sh --connect --commands="/socket-binding-group=standard-sockets/socket-binding=proxy-https:add(port=443)"

    bin/jboss-cli.sh --connect --commands="/subsystem=undertow/server=default-server/http-listener=default:write-attribute(name=proxy-address-forwarding,value=true)"

    bin/jboss-cli.sh --connect --commands="/subsystem=undertow/server=default-server/http-listener=default:write-attribute(name=redirect-socket,value=proxy-https)"

then restart the meveo container

    docker restart meveo

Once meveo is started, you can check that by displaying the logs should and check that they display

    INFO  [org.jboss.as] (Controller Boot Thread) WFLYSRV0051: Admin console listening on...

meveo admin console should be accessible at https://mydomain.org

 
Debug
-----

We have several methods to see meveo log. 

1. From meveo GUI, click on Execution / Logs

2. See the meveo docker container's log

Run this command: 

    $ sudo docker logs -f meveo 

This command will see the full log of meveo. So if the container is running for a long time, this will see huge log. This command is appropriate when meveo doesn't output much log.

3. Enter the meveo docker container, and tail the log file

Enter into the meveo container

    $ sudo docker exec -it meveo bash

Inside the container, navigate to the log folder, and then tail the log file

    $ cd /opt/jboss/wildfly/standalone/log
    $ tail -f server.log


4. Extract the log file from the container to the host system.

Developer can extract the log file from the container. Then can see it on host system. It's because developer might would like to see on the host system.

This command will extract the log file and then will put it under /tmp folder on the host system.

    $ sudo docker cp meveo:/opt/jboss/wildfly/standalone/log/server.log /tmp/server.log


5. Create a volume mapping for log files.

Developer might want to keep the log files permenantly on the host system. In this case, we can create a volume mapping rule for that.

In docker-compose.yml file, add a volume rule under the service "meveo"

      volumes:
        ......
        - <meveo_log_path_on_host>:/opt/jboss/wildfly/standalone/log

<meveo_log_path_on_host> : this path shold be relaced by the location the developer want.

Grant the good permission to the log location on host system.

    $ sudo chmod 777 <meveo_log_path_on_host>

On windows, opendocker desktop dashboard and Share your drive

Finally, apply the change of docker-compose file.

    $ sudo docker-compose up -d
This command will recreate the meveo container. and developer can see the log file in the specified location on host system.


Documentation
-------------
You can find documentation at https://meveo.org for the backend, https://front.meveo.org for the frontend framework and https://frontend.meveo.org for a demo of all the web components.
