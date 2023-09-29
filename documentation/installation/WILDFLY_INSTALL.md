# Installation on a wildfly instance

This kind of installation might be useful if you dont like docker for some reason.

It covers server configuration and use of the default database. Advanced deployment options are not covered.

## Prerequisites

The following binaries must be installed in your local environment:

| Software           | Version        | Download Link |
| --- | --- | --- |
| PostgreSQL | 9.5 | https://www.postgresql.org/download/ |
| PGAdmin | 4 | https://www.pgadmin.org/download/ |
| PostgreSQL Driver | 42.5.4 | https://jdbc.postgresql.org/download/ |
| Keycloak | 18 | https://www.keycloak.org/downloads.html  |
| Keycloak Adapter | 18 | https://www.keycloak.org/downloads.html |
| Wildfly | 18.0.1.Final | https://wildfly.org/downloads/ |


We assume you have installed curl, Java 11, Maven and Git and have those variables set `JAVA_HOME`,`M2_HOME`

WARNING: there are issues on linux with JDK 11 from version 11.10+


## PostgreSQL and Meveo database

This step is optional if you use a docker container for postgres

 * Download and install PostgreSQL
 
We need to create a `meveo` database with owner `meveo` (username: `meveo`, password:`meveo`)

Using PGAdmin:

* Download and install PGAdmin.
* Open PGAdmin, it might ask you for a master password. Enter a password and make sure to remember it.
* In the left panel, expand Servers / PostgreSQL 9.5.
* Right click Login/Group Roles and select Create / Login/Group Role.
* Under General tab set Name=meveo.
* Under Description tab set Password=meveo.
* Under Privileges tab toggle Can login and Superuser.
* Hit Save.
* Back in the left panel, right click on Databases and select Create / Database.
* Under database tab set Database=meveo, Owner=meveo.
* Hit Save.

## Keycloak

### Installation
Keycloak version 18.x using quarkus:
* Download and extract Keycloak to your PC. Let's call the folder where you extracted the files KEYCLOAK_HOME.
* Open the file KEYCLOAK_HOME/conf/keycloak.conf 
* add the line `http-port=8081` at the end
* start the server in dev mode by running `KEYCLOAK_HOME/bin/./kc.sh start-dev`

[deprecated] alternatively you can install Keycloak version 10.x using wildfly:
* Download and extract Keycloak to your PC. Let's call the folder where you extracted the files KEYCLOAK_HOME.
* Open the file KEYCLOAK_HOME/standalone/configuration/standalone.xml.
* Find this line `<socket-binding-group name="standard-sockets" default-interface="public" port-offset="${jboss.socket.binding.port-offset:0}">` and replace the port-offset:0 with port-offset:1. This means 1 will be added to all default ports. Making the oauth url available at port 8081.
* To start the server run KEYCLOAK_HOME\bin\standalone.exe.

### Realm and user creation
Create admin user and meveo realm
* Download [Meveo Realm json file](./src/main/resources/meveo-realm.json)
* Try accessing `http://localhost:8081` from your web browser.
* Create an initial admin user by entering a username and password and proceed to login. You should be able to arrive at the Keycloak admin interface.
. In the left panel, hover on Master and click Add Realm.
. In the right panel, click Select a file and choose `meveo-realm.json` file you downloaded. 
. Click create. The Meveo realm should be created and it should now be the selected realm.
. The default meveo clients, roles and users should be created as well.

To check if meveo realm is correctly configured:

 * Click the Users menu and you should see 4 users.
 * Click the Clients menu and you should see meveo-web.

## Wildfly

* Download and extract Wildfly into your PC. 
```
curl https://download.jboss.org/wildfly/18.0.1.Final/wildfly-18.0.1.Final.zip -o wildfly-18.0.1.Final.zip
sudo unzip wildfly-18.0.1.Final.zip -d /opt
export WILDFLY_HOME=/opt/wildfly-18.0.1.Final
```
 Let's call the folder where you extracted the files `WILDFLY_HOME`.


### Postgresql driver module

You can create the [postgres wildfly module manually](./POSTGRES_WILDFY_MODULE.md) but here we simply used the prepackaged module 
available in the meveo github repository

* Download the [postgres module zip file](./resources/postgres-module-42.5.4.zip)
* Unzip it in `${WILDFLY_HOME}/modules` (make sure you use same user as the one that deployed wildfly)

you should have those files created
```
${WILDFLY_HOME}/modules/org/postgresql/main/postgresql-42.5.4.jar
${WILDFLY_HOME}/modules/org/postgresql/main/module.xml
```

### Keycloak Adapter

* Download the Keycloak Adapter with the same version as the downloaded Keycloak Server.
```
cd ${WILDFLY_HOME}
sudo curl https://github.com/keycloak/keycloak/releases/download/18.0.2/keycloak-oidc-wildfly-adapter-18.0.2.zip -L -o keycloak-oidc-wildfly-adapter-18.0.2.zip
```
* Copy the downloaded file into WILDFLY_HOME and extract.
```
sudo unzip keycloak-oidc-wildfly-adapter-18.0.2.zip
sudo rm keycloak-oidc-wildfly-adapter-18.0.2.zip
```
* Run command prompt and navigate to WILDFLY_HOME\bin folder.
* Open WILDFLY_HOME\bin\adapter-install-offline.cli and change the standalone to standalone-full.
```
cd bin
sudo sed -i 's/standalone.xml/standalone-full.xml/g' adapter-install-offline.cli
```
* Execute: jboss-cli.bat --file=adapter-install-offline.cli  (or jboss-cli.sh on linux)
```
sudo ./jboss-cli.sh --file=adapter-install-offline.cli
```
* A success message should be shown.
```
{"outcome" => "success"}
{"outcome" => "success"}
{"outcome" => "success"}
{"outcome" => "success"}
```
### Standalone file

You can [create the standalone file manually](./WILDFLY_STANDALONE.md) but here we simply used the prepackaged module 
available in the meveo github repository

    
## Deploy and start Meveo 

Deploy bundled war
```
cd ${MEVEO_SRC_DIR}/
mvn clean install
sudo cp ./meveo-ear/target/meveo.ear ${WILDFLY_HOME}/standalone/deployments/
touch ${WILDFLY_HOME}/standalone/deployments/meveo.ear.dodeploy
```

Or deploy in exploded mode (for hotreload of jsf files for instance)
```
cd ${MEVEO_SRC_DIR}/
mvn clean install
unzip ./meveo-ear/target/meveo.ear -d ${WILDFLY_HOME}/standalone/deployments/meveo.ear
touch ${WILDFLY_HOME}/standalone/deployments/meveo.ear.dodeploy
```

Start wildfly
```
cd ${WILDFLY_HOME}/bin
sudo ./standalone.sh --server-config=standalone-full.xml
```
you should be able to access meveo by opening `http://localhost:8080/meveo` and loging in with `meveo.admin/meveo`

## Optional steps

### Meveo Properties 

This step is optional as a file `${WILDFLY_HOME}E\standalone\configuration\meveo-admin.properties` will be created by meveo at startup.
If you want to change the default you can edit it after startup or :
* download this file [Meveo properties file](./docker/configs/meveo-admin.properties).
* Make sure to make the necessary changes depending on your local configuration. See keys like meveo.log.file, binary.storage.path and providers.rootDir.
* Copy this file into `${WILDFLY_HOME}\standalone\configuration`.

Note that meveo properties are directly editable from meveo admin console [Configuration/Settings/System Settings](http://localhost:8080/meveo/pages/admin/properties/properties.jsf)

### Create a Wildfly Admin User

* Open a command prompt.
* Navigate to `${WILDFLY_HOME}\bin`.
* Run add-user.bat.
* Select management User
* Enter your desired user account.
* An "admin" account already exists, so you must update it instead.
* Enter any String for group.

### Login to Wildfly Server

* Start wildfly
```
cd ${WILDFLY_HOME}/bin
sudo ./standalone.sh --server-config=standalone-full.xml
```
* Open your favorite browser.
* Enter the url localhost:8080.
* Click Administration Console.
* Login using your newly created account.
