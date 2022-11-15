#!/bin/bash -e

undeploy() {
    if /opt/jboss/wildfly/bin/jboss-cli.sh --connect --commands="/deployment=meveo.ear:read-attribute(name=status)" ; then
        echo "undeploy meveo.ear"
        /opt/jboss/wildfly/bin/jboss-cli.sh --connect --commands="undeploy meveo.ear"
        if /opt/jboss/wildfly/bin/jboss-cli.sh --connect --commands="/deployment=meveo.ear:read-attribute(name=status)" ; then
            echo "ERROR: undeployment failed for meveo.ear"
            exit 1
        else
            echo "undeployment succeeded"
        fi
    else
        echo "meveo is not deployed"
    fi

    if [ -d "/opt/jboss/wildfly/standalone/deployments/meveo.ear" ]; then 
        rm -Rf /opt/jboss/wildfly/standalone/deployments/meveo.ear;
    else 
        rm -f /opt/jboss/wildfly/standalone/deployments/meveo.ear;
    fi
}

main() {
    undeploy
    "./deploy.sh"
}

main;