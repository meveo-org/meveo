# Docker installation

Using the scripts and configuration files provided here it is easy to setup instance

* [either locally](./localhost)

* [or on a linux-server](./linux-server)

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

| Variable Name | Default Value | Description |
| --- | --- | --- |
| KEYCLOAK_REALM | meveo | realm in the external keycloak |
| KEYCLOAK_CLIENT | meveo-web | client in the external keycloak  |
| KEYCLOAK_SECRET | - | secret in the external keycloak |
| MEVEO_DB_HOST | postgres | This value should be same as the service name of postgres inside docker-compose.yml file. |
| MEVEO_DB_PORT | 5432 | This is port for postgres container. |
| MEVEO_DB_NAME | meveo | This is meveo database name in postgres. This value should be same as the value of POSTGRES_DB in postgres service inside docker-compose.yml file |
| MEVEO_DB_USERNAME | meveo | This is username for meveo database. This value should be same as the value of POSTGRES_USER in postgres service inside docker-compose.yml file |
| MEVEO_DB_PASSWORD | meveo | This is password for meveo database. This value should be same as the value of POSTGRES_PASSWORD in postgres service inside docker-compose.yml file |
| WILDFLY_CUSTOM_XMS | 1024m | for java parameter -Xms |
| WILDFLY_CUSTOM_XMX | 2048m | for java parameter -Xmx |
| WILDFLY_LOG_CONSOLE_LEVEL | INFO | Wildfly console log level |
| WILDFLY_LOG_FILE_LEVEL | INFO | Wildfly server log level (means server.log file) |
| WILDFLY_LOG_MEVEO_LEVEL | INFO | Meveo source log level |
| WILDFLY_DEBUG_ENABLE | "false" | Add a debug option to Wildfly startup command ("true" or "false"). debug_enable could not be used with `JAVA_OPTS` together. If this value is `true`, `JAVA_OPTS` should be commented. |
| WILDFLY_DEBUG_PORT | 9999 | Wildfly debug port |
| JAVA_OPTS | Many parameters.<br/>Need to look at code | This variable can override default settings for wildfly java application. If this value is defined, WILDFLY_CUSTOM_XMS and WILDFLY_CUSTOM_XMX are ignored. |
| JAVA_EXTRA_OPTS | - | The extra options to add to the default jvm options. |

