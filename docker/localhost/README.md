## Local installation

This configuration deploys `meveo` locally in order to use or develop modules. Make sure the latest version of `docker` and `docker-compose` are installed on your system.

Just copy the `docker-compose.yml` file in a directory and run:

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
