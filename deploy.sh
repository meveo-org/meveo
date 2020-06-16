#!/bin/bash -e

MVN_VERSION="3.6.3"
MVN_REPO_DIR="/opt/jboss/wildfly/meveodata/default/.m2/repository"
MVN_CONF="/opt/jboss/wildfly/meveodata/default/.m2/settings.xml"
MVN_EXEC="$HOME/bin/mvn"

MEVEO_SOURCE_DIR="/opt/jboss/wildfly/meveodata/default/git/meveo-source"
MEVEO_DEPLOY_DIR="/opt/jboss/wildfly/standalone/deployments"

# Create a custom conf file for maven
install_maven_conf() {
    cat > ${MVN_CONF} << EOF
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
  <localRepository>[MVN_REPO_DIR]</localRepository>
</settings>
EOF
    sed -i "s/\[MVN\_REPO\_DIR\]/${MVN_REPO_DIR}/g" ${MVN_CONF}
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

    echo "Not found maven. Installing maven ${MVN_VERSION} ..."

    curl -s https://apache.uib.no/maven/maven-3/${MVN_VERSION}/binaries/apache-maven-${MVN_VERSION}-bin.tar.gz | tar -xzf - -C $HOME
    mv $HOME/apache-maven-${MVN_VERSION} $HOME/maven
    export PATH=$HOME/apache-maven-${MVN_VERSION}/bin:$PATH

    ${MVN_EXEC} -version
    echo "maven v${MVN_VERSION} installed"
}

meveo_build_and_deploy() {
    echo "Build meveo project in the folder : ${MEVEO_SOURCE_DIR}"
    cd ${MEVEO_SOURCE_DIR}
    ${MVN_EXEC} clean package -gs ${MVN_CONF} -Dscm.url="scm:git:ssh://git@github.com:meveo-org/meveo.git" -DskipTests

    if [ -f "${MEVEO_DEPLOY_DIR}" ]; then
        echo "Undeploy old meveo.war"
        rm -f ${MEVEO_DEPLOY_DIR}/meveo.war
        sleep 3
    fi
    echo "Deploy meveo.war"
    cp meveo-admin/web/target/meveo.war ${MEVEO_DEPLOY_DIR}/

    # Check the deployment result
    local deploy_time=0
    local deploy_time_limit=600
    while [ ! -e ${MEVEO_DEPLOY_DIR}/meveo.war.deployed ]; do
        TIME=$(( ${deploy_time} + 1 ));
        if [ ${deploy_time} -gt ${deploy_time_limit} ]; then
            echo "ERROR: deployment for meveo.war is timed out (> ${deploy_time_limit} seconds)"
            exit 1
        fi
        sleep 1
    done
}

main() {
    install_maven
    meveo_build_and_deploy
}

main;
