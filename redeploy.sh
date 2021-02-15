#!/bin/bash -e

undeploy() {
    if /opt/jboss/wildfly/bin/jboss-cli.sh --connect --commands="/deployment=meveo.war:read-attribute(name=status)" ; then
        echo "undeploy meveo.war"
        /opt/jboss/wildfly/bin/jboss-cli.sh --connect --commands="undeploy meveo.war"
        if /opt/jboss/wildfly/bin/jboss-cli.sh --connect --commands="/deployment=meveo.war:read-attribute(name=status)" ; then
            echo "ERROR: undeployment failed for meveo.war"
            exit 1
        else
            echo "undeployment succeeded"
        fi
    else
        echo "meveo is not deployed"
    fi

    if [ -d "/opt/jboss/wildfly/standalone/deployments/meveo.war" ]; then 
        rm -Rf /opt/jboss/wildfly/standalone/deployments/meveo.war;
    else 
        rm -f /opt/jboss/wildfly/standalone/deployments/meveo.war;
    fi
}

main() {
    undeploy
    "./deploy.sh"
}

main;