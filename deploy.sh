#!/bin/bash -e
MEVEO_DEPLOY_DIR="/opt/jboss/wildfly/standalone/deployments" 

echo "Build meveo project"
mvn clean package -DskipTests

echo "Deploy meveo.ear in exploded mode"
unzip meveo-ear/src/target/meveo.ear -d ${MEVEO_DEPLOY_DIR}/meveo.ear
touch ${MEVEO_DEPLOY_DIR}/meveo.ear.dodeploy
