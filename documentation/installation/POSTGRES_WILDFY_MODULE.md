# Postgres module manual creation

Note that you can simply download and unzip the [prepackaged module](./WILDFLY_INSTALL.md#postgresql-driver-module)

* Inside `WILDFLY_HOME/modules` folder create the folder hierarchy `org/postgresql/main`.
```
cd ${WILDFLY_HOME}/modules
mkdir -p org/postgresql/main
```
* Navigate to this folder.
* download the PostgreSQL driver (postgresql-42.5.4.jar) here.
```
cd ${WILDFLY_HOME}/modules/org/postgresql/main
curl https://jdbc.postgresql.org/download/postgresql-42.5.4.jar -o postgresql-42.5.4.jar
```

* Create a new file module.xml with the content below.

```
<?xml version='1.0' encoding='UTF-8'?>
<module xmlns="urn:jboss:module:1.1" name="org.postgresql">
    <resources>
        <resource-root path="postgresql-42.5.4.jar"/>
    </resources>

    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
    </dependencies>
</module>
```
by running the following commands
```
sudo touch module.xml
sudo chmod a+w module.xml
sudo echo "<?xml version='1.0' encoding='UTF-8'?>
<module xmlns="urn:jboss:module:1.1" name="org.postgresql">
    <resources>
        <resource-root path="postgresql-42.5.4.jar"/>
    </resources>

    <dependencies>
        <module name="javax.api"/>
        <module name="javax.transaction.api"/>
    </dependencies>
</module>" >>  module.xml
```
