#!/bin/bash -e

MVN_VERSION="3.6.3"
MVN_REPO_DIR="/opt/jboss/wildfly/meveodata/default/.m2/repository"
MVN_CONF="/opt/jboss/wildfly/meveodata/default/.m2/settings.xml"
MVN_EXEC="mvn"

MEVEO_SOURCE_DIR="."    # /opt/jboss/wildfly/meveodata/default/git/meveo-source
MEVEO_DEPLOY_DIR="/opt/jboss/wildfly/standalone/deployments" #"../../../../standalone/deployments"   #

# Create a custom conf file for maven
install_maven_conf() {
    mkdir -p ${MVN_REPO_DIR}
    cat > ${MVN_CONF} << EOF
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <localRepository>[MVN_REPO_DIR]</localRepository>
</settings>
EOF
    sed -i "s|\[MVN\_REPO\_DIR\]|${MVN_REPO_DIR}|g" ${MVN_CONF}
}

# Install Maven
install_maven() {
    install_maven_conf

    # make sure maven exists
    if command -v ${MVN_EXEC} ; then
        ${MVN_EXEC} -version
        echo "maven is already installed."
        return
    fi

    echo "Not found maven. Installing maven version ${MVN_VERSION} ..."

    curl -s https://apache.uib.no/maven/maven-3/${MVN_VERSION}/binaries/apache-maven-${MVN_VERSION}-bin.tar.gz | tar -xzf - -C $HOME
    mv $HOME/apache-maven-${MVN_VERSION} $HOME/maven
    export PATH=$HOME/maven/bin:$PATH

    ${MVN_EXEC} -version
    echo "maven v${MVN_VERSION} installed"
}

meveo_build_and_deploy() {
    echo "Build meveo project"
    #cd ${MEVEO_SOURCE_DIR}
    # ${MVN_EXEC} clean install -gs ${MVN_CONF}
    ${MVN_EXEC} clean package -gs ${MVN_CONF} -DskipTests

    echo "Deploy meveo.war"
    cp -f meveo-admin/web/target/meveo.war ${MEVEO_DEPLOY_DIR}/

    # Check the deployment result
    if /opt/jboss/wildfly/bin/jboss-cli.sh --connect --commands="/deployment=meveo.war:read-attribute(name=status)" ; then
        echo "deployment succeeded"
    else
        echo "ERROR: deployment failed for meveo.war"
        exit 1
    fi
}

main() {
    install_maven
    meveo_build_and_deploy
}

main;
