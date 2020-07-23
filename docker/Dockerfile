#FROM jboss/wildfly:18.0.1.Final
FROM openjdk:11.0.7-jdk-slim-buster


### ------------------------- base ------------------------- ###
# Install packages necessary
# procps : for 'free' command
# iputils-ping : for 'ping' command
RUN apt-get update \
    && apt-get install -y --no-install-recommends apt-transport-https ca-certificates curl jsvc unzip procps iputils-ping \
    && apt autoremove -y && rm -rf /var/lib/apt/lists/*

# Set the timezone
ENV TZ Europe/Paris
RUN echo "Europe/Paris" > /etc/timezone \
    && rm /etc/localtime && ln -s /usr/share/zoneinfo/Europe/Paris /etc/localtime \
    && dpkg-reconfigure -f noninteractive tzdata

# Create a user and group used to launch processes
# The user ID 1000 is the default for the first "regular" user on Fedora/RHEL,
# so there is a high chance that this ID will be equal to the current user
# making it easier to use volumes (no permission issues)
RUN groupadd -r jboss -g 2002 \
    && useradd -u 2002 -r -g jboss -m -d /opt/jboss -s /sbin/nologin -c "JBoss user" jboss \
    && chmod 755 /opt/jboss

# Set the working directory to jboss' user home directory
WORKDIR /opt/jboss

### ------------------------- base-end ------------------------- ###


### ------------------------- Jboss Wildfly ----------------------------- ###
# Ref: https://github.com/jboss-dockerfiles/wildfly/blob/18.0.1.Final/Dockerfile

# Set the WILDFLY_VERSION env variable
ENV WILDFLY_VERSION 18.0.1.Final
ARG WILDFLY_SHA1=ef0372589a0f08c36b15360fe7291721a7e3f7d9
ENV JBOSS_HOME /opt/jboss/wildfly

# Add the WildFly distribution to /opt, and make wildfly the owner of the extracted tar content
# Make sure the distribution is available from a well-known place
RUN cd $HOME \
    && curl -O https://download.jboss.org/wildfly/${WILDFLY_VERSION}/wildfly-${WILDFLY_VERSION}.tar.gz \
    && sha1sum wildfly-${WILDFLY_VERSION}.tar.gz | grep ${WILDFLY_SHA1} \
    && tar xf wildfly-${WILDFLY_VERSION}.tar.gz \
    && mv $HOME/wildfly-${WILDFLY_VERSION} ${JBOSS_HOME} \
    && rm wildfly-${WILDFLY_VERSION}.tar.gz \
    && chown -R jboss:0 ${JBOSS_HOME} \
    && chmod -R g+rw ${JBOSS_HOME}

### ------------------------- Jboss Wildfly - End ----------------------------- ###


# Change to the jboss user
USER jboss


### ------------------------- Keycloak ----------------------------- ###
# Ref: https://github.com/keycloak/keycloak-containers/blob/10.0.2/adapter-wildfly/Dockerfile

ARG KEYCLOAK_VERSION=10.0.2

WORKDIR ${JBOSS_HOME}

RUN curl -L https://downloads.jboss.org/keycloak/${KEYCLOAK_VERSION}/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-${KEYCLOAK_VERSION}.tar.gz | tar zx
RUN curl -L https://downloads.jboss.org/keycloak/${KEYCLOAK_VERSION}/adapters/saml/keycloak-saml-wildfly-adapter-dist-${KEYCLOAK_VERSION}.tar.gz | tar zx

### ------------------------- Keycloak - End ----------------------------- ###



### ------------------------- Liquibase ----------------------------- ###
# Ref: https://github.com/liquibase/docker/blob/main/Dockerfile

# Create a directory to install liquibase
RUN mkdir -p /opt/jboss/liquibase
WORKDIR /opt/jboss/liquibase

# Latest Liquibase Release Version
ARG LIQUIBASE_VERSION=3.10.0

# Download, install, clean up
RUN set -x \
  && curl -L https://github.com/liquibase/liquibase/releases/download/v${LIQUIBASE_VERSION}/liquibase-${LIQUIBASE_VERSION}.tar.gz | tar -xzf -


# RUN curl -o /liquibase/lib/<db platform>.jar <url to maven jar driver>
RUN curl -o /opt/jboss/liquibase/lib/postgresql.jar https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.12/postgresql-42.2.12.jar



### ------------------------- Postgresql module ----------------------------- ###

WORKDIR ${JBOSS_HOME}

ARG POSTGRESQL_VERSION=42.2.5

COPY --chown=jboss:jboss configs/postgresql/module.xml ${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main/module.xml

RUN curl -O https://jdbc.postgresql.org/download/postgresql-${POSTGRESQL_VERSION}.jar \
    && mv postgresql-${POSTGRESQL_VERSION}.jar ${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main/ \
    && sed -i "s|\[POSTGRESQL\_VERSION\]|${POSTGRESQL_VERSION}|g" ${JBOSS_HOME}/modules/system/layers/base/org/postgresql/main/module.xml



### ------------------------- Configurations ----------------------------- ###


### Create some folders for meveo volumes
RUN mkdir -p ${JBOSS_HOME}/meveodata /tmp/meveo/binary/storage /tmp/meveo/infinispan \
    && chown -R jboss:jboss ${JBOSS_HOME}/meveodata /tmp/meveo \
### Backup the original standalone-full.xml
    && cp ${JBOSS_HOME}/standalone/configuration/standalone-full.xml ${JBOSS_HOME}/standalone/configuration/standalone-full.xml.org

### cli commands
COPY --chown=jboss:jboss configs/cli ${JBOSS_HOME}/cli

### meveo configuration
COPY --chown=jboss:jboss configs/meveo-admin.properties ${JBOSS_HOME}/standalone/configuration/meveo-admin.properties

### Changelog files for Liquibase
COPY --chown=jboss:jboss configs/db_resources /opt/jboss/liquibase/db_resources

### meveo.war
COPY --chown=jboss:jboss meveo.war ${JBOSS_HOME}/standalone/deployments/meveo.war

# Ensure signals are forwarded to the JVM process correctly for graceful shutdown
ENV LAUNCH_JBOSS_IN_BACKGROUND true

# Expose the ports we're interested in
EXPOSE 8080 9990

COPY --chown=jboss:jboss docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT [ "/docker-entrypoint.sh" ]
