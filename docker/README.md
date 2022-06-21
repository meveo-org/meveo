# Docker installation

Using the scripts and configuration files provided here it is easy to setup instance

* [either locally](./localhost/README.md)

* [or on a linux-server](./linux-server/README.md)

# Environment variables for Meveo stack
Meveo stack runs two docker containers: postgres container and meveo container.</br>
These containers has below environment variables to customize their running.

## Environment Variables for postgres image

### Mandatory variables

The following environment variables should be defined in postgres service inside docker-compose.yml

| Variable Name | Default Value | Description |
| --- | --- | --- |
| POSTGRES_USER | No default value | This is username for meveo database. It should be "meveo" |
| POSTGRES_PASSWORD | No default value | This is password for meveo database. It should be defined with secure value. |

### Optional Variables

The following environment variables can be redefined.

| Variable Name | Default Value | Description |
| --- | --- | --- |
| POSTGRES_DB | meveo | The database name can be defined as the different name. |




## Environment Variables for meveo image

### Mandatory variables

The following environment variables should be defined in meveo service inside docker-compose.yml

| Variable Name | Default Value | Description |
| --- | --- | --- |
| KEYCLOAK_URL | - | keycloak url should be defined because meveo image doesn't integrate a keycloak. |
| WILDFLY_PROXY_ADDRESS_FORWARDING | false | This is a flag to use the reverse proxy. It should be "true" if meveo is running behind the reverse proxy |


### Optional Variables

The following environment variables can be redefined for meveo service

|Variable Name            |Default Value                            |Description                                                                                                                                                                           |
|:------------------------|:----------------------------------------|:-------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
|KEYCLOAK_REALM           |meveo                                    |realm in the external keycloak                                                                                                                                                        |
|KEYCLOAK_CLIENT          |meveo-web                                |client in the external keycloak                                                                                                                                                       |
|KEYCLOAK_SECRET          |-                                        |secret in the external keycloak                                                                                                                                                       |
|MEVEO_DB_HOST            |postgres                                 |This value should be same as the service name of postgres inside docker-compose.yml file.                                                                                             |
|MEVEO_DB_PORT            |5432                                     |This is port for postgres container.                                                                                                                                                  |
|MEVEO_DB_NAME            |meveo                                    |This is meveo database name in postgres. This value should be same as the value of POSTGRES_DB in postgres service inside docker-compose.yml file                                     |
|MEVEO_DB_USERNAME        |meveo                                    |This is username for meveo database. This value should be same as the value of POSTGRES_USER in postgres service inside docker-compose.yml file                                       |
|MEVEO_DB_MIN_POOL_SIZE   |10                                       |This is the minimum pool size of the MeveoAdmin db                                                                                                                                    |
|MEVEO_DB_MAX_POOL_SIZE   |200                                      |This is the maxiumum pool size of the MeveoAdmin db                                                                                                                                   |
|MEVEO_DB_PASSWORD        |meveo                                    |This is password for meveo database. This value should be same as the value of POSTGRES_PASSWORD in postgres service inside docker-compose.yml file                                   |
|MEVEO_ADMIN_BASE_URL     |http://localhost:8080/                   |Base url for meveo admin.                                                                                                                                                             |
|MEVEO_ADMIN_WEB_CONTEXT  |meveo                                    |Web Context for meveo admin.                                                                                                                                                          |
|WILDFLY_CUSTOM_XMS       |1024m                                    |for java parameter -Xms                                                                                                                                                               |
|WILDFLY_CUSTOM_XMX       |2048m                                    |for java parameter -Xmx                                                                                                                                                               |
|WILDFLY_CUSTOM_XMMS      |300m                                     |for java parameter -XX:MetaspaceSize                                                                                                                                                  |
|WILDFLY_CUSTOM_XMMX      |500m                                     |for java parameter -XX:MaxMetaspaceSize=                                                                                                                                              |
|WILDFLY_LOG_CONSOLE_LEVEL|OFF                                      |Wildfly console log level                                                                                                                                                             |
|WILDFLY_LOG_FILE_LEVEL   |INFO                                     |Wildfly server log level (means server.log file)                                                                                                                                      |
|WILDFLY_LOG_MEVEO_LEVEL  |INFO                                     |Meveo source log level                                                                                                                                                                |
|WILDFLY_DEBUG_ENABLE     |"false"                                  |Add a debug option to Wildfly startup command ("true" or "false"). debug_enable could not be used with `JAVA_OPTS` together. If this value is `true`, `JAVA_OPTS` should be commented.|
|WILDFLY_DEBUG_PORT       |9999                                     |Wildfly debug port                                                                                                                                                                    |
|JAVA_OPTS                |Many parameters.<br/>Need to look at code|This variable can override default settings for wildfly java application. If this value is defined, WILDFLY_CUSTOM_XMS and WILDFLY_CUSTOM_XMX are ignored.                            |
|JAVA_EXTRA_OPTS          |-                                        |The extra options to add to the default jvm options.                                                                                                                                  |### APM for Meveo wildfly

### APM for Meveo wildfly

Meveo image installed [glowroot](https://glowroot.org).

`glowroot` is a Java APM(Application Performance Management), which is the monitoring and management of performance and availability of java applications.

`glowroot` helps you get to the root cause of application performance issues.

By default, it is not launched. To launch it, set the env variable `GLOWROOT_ENABLE` to `true`.

Like this:

```yaml
    environment:
      GLOWROOT_ENABLE: "true"
```

It is supposed to listen to the port 4000 inside the container. To access `glowroot` UI, please configure the reverse proxy or the port mapping of the container.
