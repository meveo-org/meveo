## Local installation

### Keycloak setup

This configuration deploys `meveo` locally in order to use or develop modules. Make sure the latest version of `docker` and `docker-compose` are installed on your system.

1) In order to use the local keycloak server, please add a hostname `kc-server` to the local hosts file.
How to do is [here](https://github.com/meveo-org/keycloak/tree/master/docker#how-to-use-with-meveo-container-on-localhost)

2) In order to use the remote keycloak server, remove the service `keycloak` in docker-compose.yml file. And then, under the service `meveo`, change the environment variable `KEYCLOAK_URL` by the remote keycloak url.


### Start Meveo

Then, just copy the `docker-compose.yml` file in a directory and run:

```sh
  docker-compose up -d
```

Then access meveo admin console at:
`http://localhost:8080/meveo`

The default credentials are:
`meveo.admin / meveo`

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
