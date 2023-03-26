# Installation and configuration of eclipse

This section describe how to install an configure eclipse IDE to be able to work efficiently on meveo core code.

## Prerequisites

The following binaries must be installed in your local environment:

| Software           | Version        | Download Link |
| --- | --- | --- |
| Eclipse | JEE-latest | https://www.eclipse.org/downloads/packages/ |

## Installation Guide

This guide assumes you already [installed meveo](./WILDFLY_INSTALL.md).

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
* Right click on the server panel, select New / Server and select Wildfly 18.
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
* Select the directory where you want to checkout the project, click Finish. we not this directory `${MEVEO_SRC_DIR}`
* Once the cloning is done, meveo project should appear in your Git Repositories.
* Right click on the meveo repository and select Import Maven Projects.
* Make sure that all projects are selected. You can select a working set (use for grouping projects) and then hit finish.
* Meveo projects should be visible in your Package or Project explorer, whichever is open.
* Now you can start coding.

#### Adding meveo-ejb as a Source Folder

You should have already cloned the meveo project before proceeding with these steps.

* Build the meveo parent project, disregard the errors for now.
  * `mvn clean package -DskipTests` 
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

#### Init postgres database

* Select file pom.xml in project meveo-model .
* Select run as maven, and run with the following options :
    * **Goals** : liquibase:dropAll liquibase:update
    * **Profiles** : rebuild
    * **Attributes** : 
        * db.url=jdbc:postgresql://localhost:5432/meveo
        * db.username=meveo
        * db.password=meveo
        * db.schema=public
        * db.driver=org.postgresql.Driver

in command line :
```
cd ${MEVEO_SRC_DIR}/meveo/model
mvn liquibase:dropAll liquibase:update -Prebuild -D"db.url=jdbc:postgresql://localhost:5432/meveo" -D"db.username=meveo" -D"db.password=meveo" -D"db.schema=public" -D"db.driver=org.postgresql.Driver"
```
    
#### Deploying Meveo to Wildfly

* Open the server tab once again.
* Select Add and Remove.
* Select meveo-admin-web, click Finish.
* Debug or start the server.
* Open the console log from Window / Show View / Console.
* If no error is shown, you should be able to access meveo from the URL http://localhost:8080/meveo.
* Login using the account meveo.admin / meveo.

you should be able to access meveo by opening `http://localhost:8080/meveo` and loging in with `meveo.admin/meveo`
