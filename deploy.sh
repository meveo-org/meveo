#!/bin/bash -e
MEVEO_DEPLOY_DIR="/opt/jboss/wildfly/standalone/deployments" 

echo "Build meveo project"
mvn clean package -DskipTests

echo "Deploy meveo.war in exploded mode"
unzip meveo-admin/web/target/meveo.war -d ${MEVEO_DEPLOY_DIR}/meveo.war
touch ${MEVEO_DEPLOY_DIR}/meveo.war.dodeploy
