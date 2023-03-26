
# Configuration of standalone-full.xml

Note that you can simply download the [pre-configured standalone-full.xml](./WILDFLY_INSTALL.md#)

* Open the file WILDFLY_HOME\standalone\configuration\standalone-full.xml
* At the end of `<extensions>` tag add the following properties.

Note that depending on the keycloak version you have the value of meveo.keycloak.url varies:
* with keycloak running on wildfly `<property name="meveo.keycloak.url" value="http://localhost:8081"/>`
* with keycloak running on quarkus `<property name="meveo.keycloak.url" value="http://localhost:8081/auth"/>`


```
<system-properties>
    <property name="jboss.as.management.blocking.timeout" value="900"/>
    <property name="meveo.instance.name" value="demo"/>
    <property name="java.net.preferIPv4Stack" value="true"/>
    <property name="meveo.keycloak.url" value="http://localhost:8081"/>
    <property name="meveo.keycloak.secret" value="afe07e5a-68cb-4fb0-8b75-5b6053b07dc3"/>
    <property name="meveo.keycloak.realm" value="meveo"/>
    <property name="meveo.keycloak.client" value="meveo-web"/>
    <property name="meveo.admin.server.name" value="localhost"/>
    <property name="meveo.admin.port.number" value="5432"/>
    <property name="meveo.admin.database.name" value="meveo"/>
    <property name="meveo.admin.database.driver" value="postgresql"/>
    <property name="meveo.admin.database.username" value="meveo"/>
    <property name="meveo.admin.database.password" value="meveo"/>
    <property name="jboss.tx.node.id" value="meveo-default"/>
    <property name="meveo.keycloak.fixed-hostname" value="localhost"/>
    <property name="resteasy.preferJacksonOverJsonB" value="true"/>
</system-properties>
```

*Note that we are using the username and password of the meveo database that must be created on postgres

##### Add a Datasource

* Open the file `${WILDFLY_HOME}\standalone\configuration\standalone-full.xml`.
* Search for "subsystem xmlns="urn:jboss:domain:datasources".
* Add the following datasource configuration.

```
<datasource jta="true" jndi-name="java:jboss/datasources/MeveoAdminDatasource" pool-name="meveo" enabled="true" use-java-context="true" spy="false" use-ccm="false" tracking="false" statistics-enabled="false">
    <connection-url>jdbc:postgresql://${meveo.admin.server.name}:${meveo.admin.port.number}/${meveo.admin.database.name}</connection-url>
    <driver>${meveo.admin.database.driver}</driver>
    <new-connection-sql>select 1</new-connection-sql>
    <transaction-isolation>TRANSACTION_READ_COMMITTED</transaction-isolation>
    <pool>
        <min-pool-size>1</min-pool-size>
        <max-pool-size>20</max-pool-size>
        <prefill>false</prefill>
        <use-strict-min>false</use-strict-min>
        <flush-strategy>FailingConnectionOnly</flush-strategy>
    </pool>
    <security>
        <user-name>${meveo.admin.database.username}</user-name>
        <password>${meveo.admin.database.password}</password>
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
</datasource>
```

If you need a XA datasource you can use instead :

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
    <local-cache name="meveo-cft-cache"/>
    <local-cache name="meveo-cet-cache"/>
    <local-cache name="meveo-notification-cache"/>
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
    <local-cache name="meveo-user-message" />
</cache-container>
```
