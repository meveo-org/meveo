#FROM jboss/wildfly:15.0.1.Final
FROM openjdk:8-jdk-slim-buster


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


### ------------------------- Jboss Wildfly-15.0.1.Final -----------------------------###
# Set the WILDFLY_VERSION env variable
ENV WILDFLY_VERSION 15.0.1.Final
ARG WILDFLY_SHA1=23d6a5889b76702fc518600fc5b2d80d6b3b7bb1
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

### ------------------------- Jboss Wildfly-15.0.1.Final - End -----------------------------###


### ------------------------- Keycloak-4.8.3.Final -----------------------------###

ENV KEYCLOAK_VERSION 4.8.3.Final

WORKDIR ${JBOSS_HOME}

USER jboss

RUN curl -L https://downloads.jboss.org/keycloak/${KEYCLOAK_VERSION}/adapters/keycloak-oidc/keycloak-wildfly-adapter-dist-${KEYCLOAK_VERSION}.tar.gz | tar zx
RUN curl -L https://downloads.jboss.org/keycloak/${KEYCLOAK_VERSION}/adapters/saml/keycloak-saml-wildfly-adapter-dist-${KEYCLOAK_VERSION}.tar.gz | tar zx
RUN curl -L https://downloads.jboss.org/keycloak/${KEYCLOAK_VERSION}/keycloak-overlay-${KEYCLOAK_VERSION}.tar.gz | tar zx

### ------------------------- Keycloak-4.8.3.Final - End -----------------------------###

# Standalone.xml modifications regarding keycloak & activemq
### Note: These actions will be done by replacing of standalone.xml file
#RUN sed -i -e 's/<extensions>/&\n        <extension module="org.keycloak.keycloak-adapter-subsystem"\/>/' ${JBOSS_HOME}/standalone/configuration/standalone.xml \
#    && sed -i -e 's/<profile>/&\n        <subsystem xmlns="urn:jboss:domain:keycloak:1.1"\/>/' ${JBOSS_HOME}/standalone/configuration/standalone.xml \
#    && sed -i -e 's/<security-domains>/&\n                <security-domain name="keycloak">\n                    <authentication>\n                        <login-module code="org.keycloak.adapters.jboss.KeycloakLoginModule" flag="required"\/>\n                    <\/authentication>\n                <\/security-domain>/' ${JBOSS_HOME}/standalone/configuration/standalone.xml \ 
#    && sed -i -e 's/<extensions>/&\n        <extension module="org.keycloak.keycloak-saml-adapter-subsystem"\/>/' ${JBOSS_HOME}/standalone/configuration/standalone.xml \
#    && sed -i -e 's/<profile>/&\n        <subsystem xmlns="urn:jboss:domain:keycloak-saml:1.1"\/>/' ${JBOSS_HOME}/standalone/configuration/standalone.xml \
#    && sed -i -e 's/<extensions>/&\n        <extension module="org.wildfly.extension.messaging-activemq"\/>/' ${JBOSS_HOME}/standalone/configuration/standalone.xml


### Create some folders for meveo volumes
RUN mkdir -p ${JBOSS_HOME}/standalone/data ${JBOSS_HOME}/meveodata /tmp/meveo/binary/storage \
    && chown -R jboss:jboss ${JBOSS_HOME}/standalone/data ${JBOSS_HOME}/meveodata /tmp/meveo \
### Backup the original standalone.xml
    && mv ${JBOSS_HOME}/standalone/configuration/standalone.xml ${JBOSS_HOME}/standalone/configuration/standalone.xml.org

### For meveo configuration ###
COPY --chown=jboss:jboss templates/standalone.xml ${JBOSS_HOME}/standalone/configuration/standalone.xml
COPY --chown=jboss:jboss templates/meveo-admin.properties ${JBOSS_HOME}/standalone/configuration/meveo-admin.properties
COPY --chown=jboss:jboss templates/profile.properties ${JBOSS_HOME}/standalone/configuration/profile.properties
COPY --chown=jboss:jboss templates/postgresql ${JBOSS_HOME}/modules/system/layers/base/org/postgresql
COPY --chown=jboss:jboss templates/keycloakdb ${JBOSS_HOME}/standalone/data/keycloakdb

### meveo.war
COPY --chown=jboss:jboss meveo.war ${JBOSS_HOME}/standalone/deployments/meveo.war

### cli commands
COPY --chown=jboss:jboss tools/cli ${JBOSS_HOME}/cli

# Ensure signals are forwarded to the JVM process correctly for graceful shutdown
ENV LAUNCH_JBOSS_IN_BACKGROUND true

# Expose the ports we're interested in
EXPOSE 8080 9990

COPY --chown=jboss:jboss docker-entrypoint.sh /
RUN chmod +x /docker-entrypoint.sh

ENTRYPOINT [ "/docker-entrypoint.sh" ]
