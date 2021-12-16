## Local installation

### Start Meveo

Just copy the [docker-compose.yml](https://raw.githubusercontent.com/meveo-org/meveo/master/docker/localhost/docker-compose.yml) file in a directory. 

And then run:

```sh
  docker-compose up -d
```

After start Meveo, then access meveo admin console at: `http://localhost:8080/meveo`

The default credentials are: `meveo.admin / meveo`

### How to access the local keycloak server

Local keycloak server is addressed at `http://localhost:8081/auth`

The default keycloak credentials are: `admin / admin`

### Start Neo4j for Meveo

In docker-compose.yml file, please uncomment all lines regarding neo4j service.

```yaml
.....
volumes:
  neo4j_data:
    driver: local
.....
services:
  neo4j:
      image: manaty/neo4j-meveo:3.5.13
      container_name: neo4j
      build:
        context: ./
        dockerfile: ../Dockerfile.neo4j
      networks:
        - meveo
      restart: unless-stopped
      volumes:
        - neo4j_data:/data
      environment:
        NEO4J_AUTH: neo4j/neo4j123
      ports:
        - 7474:7474
        - 7687:7687
```

Then run:
```sh
  docker-compose up -d neo4j
```

After neo4j container, please open neo4j browser at `http://localhost:7474/`

The default neo4j credentials are: `neo4j / neo4j123`


### Externalize all meveo files

To store all meveo files (git files, maven .m2, webapps ...) outside the docker image, first stop the containers and remove the volumes

```sh
  docker-compose down -v
```

Edit the `docker-compose.yml` file and replace the line

```sh
  - meveo_data:/opt/jboss/wildfly/meveodata
```

with

```sh
  - e:/tmp/meveo:/opt/jboss/wildfly/meveodata
```
> Where `e:/tmp/meveo` is the path to a directory on your system that will contain the contents of `meveodata`

Then start the containers

```sh
  docker-compose up -d
```
