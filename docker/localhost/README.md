## Local installation

### Start Meveo

Just copy the [docker-compose.yml](https://raw.githubusercontent.com/meveo-org/meveo/develop/docker/localhost/docker-compose.yml) file in a directory. 

#### Windows or Mac OS host system
And then run:

```sh
  docker-compose up -d
```

#### Linux host system
In the copied docker-compose.yml file, please uncomment `KEYCLOAK_URL: http://localhost:8081/auth` for Linux system, and comment `KEYCLOAK_URL` variable for Windows & Mac OS.

And then run:

```sh
  docker-compose up -d
```

After start Meveo, then access meveo admin console at: `http://localhost:8080/meveo`

The default credentials are: `meveo.admin / meveo`

### How to access the local keycloak server

Local keycloak server is addressed at `http://localhost:8081/auth`

The default keycloak credentials are: `admin / admin`

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
