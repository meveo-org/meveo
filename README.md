Meveo
=====

Meveo is a platform that allow to develop and execute enterprise back and front applications
It is intended to be run by Wildfly 15 with full Jakarta EE stack under licence : AGPLv3.0

Installation
------------
You can build the war and deploy on wildfly 15 after configuring keycloack, postgres, infinispan, neo4j(optional), elasticsearch(optional),...

Or simply execute the docker images from docker hub.

For a simple postgres localhost deployment:

make sure you have docker installed

put the file https://raw.githubusercontent.com/meveo-org/meveo/master/docker-compose.yml on some directory

and run

'docker-compose up -d'

then meveo is running on https://localhost:8080/meveo

Debug
-----

If you encounter some bug and want to check the logs (assuming you deployed from docker compose like above) then execute 

docker logs -f meveo


Documentation
-------------
You can find documentation at https://meveo.org for the backend, https://front.meveo.org for the frontend framework and https://frontend.meveo.org for a demo of all the web components.
