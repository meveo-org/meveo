# Installation on a wildfly instance

This kind of installation might be useful if you dont like docker for some reason.

It covers server configuration and use of the default database. Advanced deployment options are not covered.

## Prerequisites

The following binaries must be installed in your local environment:

| Software           | Version        | Download Link |
| --- | --- | --- |
| Java | 11 | https://developers.redhat.com/products/openjdk/download |
| Git | latest | https://git-scm.com/downloads |
| Maven | latest | https://maven.apache.org/download.cgi |
| PostgreSQL | 9.5 | https://www.postgresql.org/download/ |
| PGAdmin | 4 | https://www.pgadmin.org/download/ |
| PostgreSQL Driver | 42.2.5 | https://jdbc.postgresql.org/download.html |
| Keycloak | >10 | https://www.keycloak.org/downloads.html |
| Keycloak Adapter | >10 | https://www.keycloak.org/downloads.html |
| Wildfly | 18.0.1.Final | https://wildfly.org/downloads/ |
| Eclipse | JEE-latest | https://www.eclipse.org/downloads/packages/ |

## Installation Guide

This guide is intended for Windows users. Minor changes to the instructions must be done to become applicable to other operating systems.

### Java

* Install or extract the OpenJDK in your PC.
* Set the environment variable JAVA_HOME, make sure it points to where you installed the OpenJDK. In Windows explorer, right click on This PC / Properties / Advance system settings / Environment variables / New. [Refer to this guide](https://czetsuya-tech.blogspot.com/2020/04/how-to-add-java-home-in-windows-environment-variables.html).
* Add %JAVA_HOME%\bin in the Windows Path environment variable. https://czetsuya-tech.blogspot.com/2020/04/how-to-add-java-binary-folder-to-windows-execution-path.html[Refer to this guide].
* Open a Windows command prompt and type "java -version", hit enter. The Java version installed on your system should be shown.

### Git

 Download and install Git.

### Maven

* Download and extract the maven archive into your PC.
* Add the M2_HOME with value pointing to where you extract maven to your Windows environment variable. See what we did in Java above. 
Add the %M2_HOME%\bin folder to your Windows environment path just as we did with Java above.
* Open a Windows command prompt and type "mvn -version", hit enter. Just like Java it should give us the version of maven installed.

### PostgreSQL

 Download and install PostgreSQL. Take note of the admin password as we will use it later.

### PGAdmin

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

### Wildfly and Keycloak Installation


#### Option 1 - Installing as Standalone Server.

#### Keycloak

* Download and extract Keycloak to your PC. Let's call the folder where you extracted the files KEYCLOAK_HOME.
* Open the file KEYCLOAK_HOME/standalone/configuration/standalone.xml.
* Find this line `<socket-binding-group name="standard-sockets" default-interface="public" port-offset="${jboss.socket.binding.port-offset:0}">` and replace the port-offset:0 with port-offset:1. This means 1 will be added to all default ports. Making the oauth url available at port 8081.
* To start the server run KEYCLOAK_HOME\bin\standalone.exe.
* Try accessing `http://localhost:8081` from your web browser.
* Create an initial admin user by entering a username and password and proceed to login. You should be able to arrive at the Keycloak admin interface.
. In the left panel, hover on Master and click Add Realm.
. In the right panel, click Select a file and choose `meveo-realm.json` file. [Meveo Realm](./src/main/resources/meveo-realm.json)
. Click create. The Meveo realm should be created and it should now be the selected realm.
. The default meveo clients, roles and users should be created as well.

To check if meveo realm is correctly configured:

 * Click the Users menu and you should see 9 users.
 * Click the Clients menu and you should see meveo-web.

In the eclipse section, we will discuss how we can integrate Keycloak so we can start it from there.

##### Wildfly

* Download the PostgreSQL driver.
* Download and extract Wildfly into your PC. Let's call the folder where you extracted the files `WILDFLY_HOME`.
* Inside `WILDFLY_HOME/modules` folder create this folder hierarchy `org/postgresql/main`.
* Navigate to this folder.
* Copy and paste the PostgreSQL driver (postgresql-42.2.5.jar) here.
* Create a new file module.xml with the content below.

```
<?xml version='1.0' encoding='UTF-8'?>
<module xmlns="urn:jboss:module:1.1" name="org.postgresql">
    <resources>
        <resource-root path="postgresql-42.2.5.jar"/>
    </resources>

    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
    </dependencies>
</module>
```

###### Add the Keycloak Adapter to Wildfly

* Download the Keycloak Adapter with the same version as the downloaded Keycloak Server.
* Copy the downloaded file into WILDFLY_HOME and extract.
* Run command prompt and navigate to WILDFLY_HOME\bin folder.
* Open WILDFLY_HOME\bin\adapter-install-offline.cli and change the standalone to standalone-full.
* Execute: jboss-cli.bat --file=adapter-install-offline.cli.
* A success message should be shown.

##### Add System Properties

At the end of extensions tag add the following properties.

```
<system-properties>
    <property name="jboss.as.management.blocking.timeout" value="900"/>
    <property name="meveo.instance.name" value="demo"/>
    <property name="java.net.preferIPv4Stack" value="true"/>
    <property name="meveo.keycloak.url" value="http://localhost:8081/auth"/>
    <property name="meveo.keycloak.secret" value="afe07e5a-68cb-4fb0-8b75-5b6053b07dc3"/>
    <property name="meveo.keycloak.realm" value="meveo"/>
    <property name="meveo.keycloak.client" value="meveo-web"/>
    <property name="meveo.admin.server.name" value="localhost"/>
    <property name="meveo.admin.port.number" value="5432"/>
    <property name="meveo.admin.database.name" value="meveo"/>
    <property name="meveo.admin.database.driver" value="postgresql"/>
    <property name="meveo.admin.database.username" value="postgres"/>
    <property name="meveo.admin.database.password" value="<your-postgresql-admin-password>"/>
    <property name="jboss.tx.node.id" value="meveo-default"/>
    <property name="meveo.keycloak.fixed-hostname" value="localhost"/>
    <property name="resteasy.preferJacksonOverJsonB" value="true"/>
</system-properties>
```

*Note that we are using the postgres admin account here.

##### Add a Datasource

* Open the file WILDFLY_HOME\standalone\configuration\standalone.xml.
* Search for "subsystem xmlns="urn:jboss:domain:datasources".
* Add the following datasource configuration.

```
<xa-datasource jndi-name="java:jboss/datasources/MeveoAdminDatasource" pool-name="meveo" enabled="true" use-java-context="true" use-ccm="false">
    <xa-datasource-property name="ServerName">
        ${meveo.admin.server.name}
    </xa-datasource-property>
    <xa-datasource-property name="PortNumber">
        5432
    </xa-datasource-property>
    <xa-datasource-property name="DatabaseName">
        meveo
    </xa-datasource-property>
    <driver>postgresql</driver>
    <xa-pool>
        <min-pool-size>10</min-pool-size>
        <max-pool-size>200</max-pool-size>
        <prefill>false</prefill>
        <use-strict-min>false</use-strict-min>
        <flush-strategy>FailingConnectionOnly</flush-strategy>
    </xa-pool>
    <security>
        <user-name>${meveo.admin.database.username:meveo}</user-name>
        <password>${meveo.admin.database.password:meveo}</password>
    </security>
    <validation>
        <valid-connection-checker class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLValidConnectionChecker"/>
        <validate-on-match>true</validate-on-match>
        <background-validation>false</background-validation>
        <use-fast-fail>true</use-fast-fail>
        <exception-sorter class-name="org.jboss.jca.adapters.jdbc.extensions.postgres.PostgreSQLExceptionSorter"/>
    </validation>
    <timeout>
        <blocking-timeout-millis>60000</blocking-timeout-millis>
        <idle-timeout-minutes>15</idle-timeout-minutes>
    </timeout>
    <statement>
        <share-prepared-statements>true</share-prepared-statements>
    </statement>
</xa-datasource>
```

Add the PostgreSQL driver as well in the drivers section:

```
<driver name="postgresql" module="org.postgresql">
    <driver-class>org.postgresql.Driver</driver-class>
    <xa-datasource-class>org.postgresql.xa.PGXADataSource</xa-datasource-class>
</driver>
```

##### Add Infinispan Cache

Search for `subsystem xmlns="urn:jboss:domain:infinispan"` and add the cache container below.

```
<cache-container name="meveo">
    <local-cache name="meveo-multiTenant-cache"/>
    <local-cache name="meveo-price-plan"/>
    <local-cache name="meveo-usage-charge-template-cache"/>
    <local-cache name="meveo-charge-instance-cache"/>
    <local-cache name="meveo-counter-cache"/>
    <local-cache name="meveo-edr-cache">
        <object-memory size="100000"/>
    </local-cache>
    <local-cache name="meveo-access-cache"/>
    <local-cache name="meveo-cft-cache"/>
    <local-cache name="meveo-cet-cache"/>
    <local-cache name="meveo-notification-cache"/>
    <local-cache name="meveo-balance"/>
    <local-cache name="meveo-reservedBalance"/>
    <local-cache name="meveo-usageChargeInstanceWallet"/>
    <local-cache name="meveo-running-jobs"/>
    <local-cache name="unique-crt">
        <expiration interval="300000" lifespan="300000"/>
    </local-cache>
    <local-cache name="endpoints-results">
        <expiration interval="604800000" lifespan="604800000"/>
    </local-cache>
    <local-cache name="meveo-es-index-cache"/>
    <local-cache name="meveo-crt-cache"/>
    <local-cache name="meveo-rows-page-cache"/>
</cache-container>
```

##### Download Meveo Properties

* Download this file [Meveo properties file](./docker/configs/meveo-admin.properties).
* Make sure to make the necessary changes depending on your local configuration. See keys like meveo.log.file, binary.storage.path and providers.rootDir.
* Copy this file into `WILDFLY_HOME\standalone\configuration`.

##### Create a Wildfly Admin User

* Open a command prompt.
* Navigate to WILDFLY_HOME\bin.
* Run add-user.bat.
* * Select management User
* Enter your desired user account.
* An "admin" account already exists, so you must update it instead.
* Enter any String for group.

##### Login to Wildfly Server

* Open your favorite browser.
* Enter the url localhost:8080.
* Click Administration Console.
* Login using your newly created account.

A completely configured file is available for reference link:standalone.xml[here].

### Eclipse

* Download the eclipse installer.
* Run the installer and select the Eclipse for JavaEE developers.

#### Install JBoss Tools Plugin

* Run Eclipse IDE.
* Open menu Help / Eclipse Marketplace.
* In the Find text field enter "jboss".
* Find JBoss Tools and click Install.
* Don't install all the components, select or filter maven, egit and jboss as.

#### Add both the Keycloak and Wildfly Server for Debugging

* Open the Server tab by clicking Window / Show View / Other and filter for server.
* Select servers, a new tab should open.
* Right click on the server panel, select New / Server and select Wildfly 15.
* Click Next, select Create a new runtime.
* Click Next, and browse the directory where you installed Wildfly - WILDFLY_HOME.
* In the Configuration file field select standalone-full.xml. This is the one we modified earlier.
* Click Next and then Finish.

*Do the same with Keycloak.

Now you can start running either the Keycloak and Wildfly server in debug mode by right clicking on it and selecting Debug. You can click the Debug icon too.

#### Cloning the Meveo Project

This is done inside Eclipse IDE. Since we have installed egit component from JBoss Tools plugin, we can use it to checkout from Github.

* Open Window / Show View / Other and filter for git.
* Select both Git Repositories and Git Staging. Two new panels should open.
* Open Git Repositories.
* Right click and then select Clone a Git Repository or click the green icon in the top right corner with the same label.
* In the URI enter git@github.com:meveo-org/meveo.git, click Next.
* A selection of branch that you wanted to checkout should appear. By default all branches are selected. Click Next.
* Select the directory where you want to checkout the project, click Finish.
* Once the cloning is done, meveo project should appear in your Git Repositories.
* Right click on the meveo repository and select Import Maven Projects.
* Make sure that all projects are selected. You can select a working set (use for grouping projects) and then hit finish.
* Meveo projects should be visible in your Package or Project explorer, whichever is open.
* Now you can start coding.

#### Adding meveo-ejb as a Source Folder

You should have already cloned the meveo project before proceeding with these steps.

* Build the meveo parent project, disregard the errors for now.
* Expand the meveo-admin-ejbs project.
* Expand the target/generated-sources folder.
* Right click on the java folder, choose Build Path and select Use as Source Folder.
* The source target/generated-sources/java folder, should be added in the root of meveo-admin-ejbs project.

#### Install the EGIT Plugin using JBoss Tools

* In Eclipse select Help / Eclipse Marketplace.
* In the Search / Find field, enter "JBoss Tools".
* Click JBoss Tools.
* Check the maven related features like JBoss Maven Integration.

#### Install the EGIT Plugin

This is another way to install the EGIT plugin in case for some reason that you don't want to use the JBoss Tools plugin.

* In Eclipse select File / New / Others.
* In the Wizards panel, filter for maven.
* Select Check out Maven Projects from SCM.
* In the next panel, in the lower right corner, click m2e Marketplace.
* In the m2e Catalog panel, enter "m2e-egit" in the Find field and hit enter.
* Checked the m2e-egit feature and click ok.

#### Deploying Meveo to Wildfly

* Open the server tab once again.
* Select Add and Remove.
* Select meveo-admin-web, click Finish.
* Debug or start the server.
* Open the console log from Window / Show View / Console.
* If no error is shown, you should be able to access meveo from the URL http://localhost:8080/meveo.
* Login using the account meveo.admin / meveo.
