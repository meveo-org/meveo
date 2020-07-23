#!/bin/bash -e

exit_with_error() {
    if [ "$ERROR" != "0" ]; then
        echo "${red}[$(date '+%Y-%m-%d %H:%M:%S')] ERROR : $@ ${reset}";
        exit 1;
    fi
}

warn() {
    echo "${yellow}[$(date '+%Y-%m-%d %H:%M:%S')] WARN : $@ ${reset}";
}

info() {
    echo "${green}[$(date '+%Y-%m-%d %H:%M:%S')] INFO : $@ ${reset}";
}


# Meveo database parameters
export MEVEO_DB_HOST=${MEVEO_DB_HOST:-postgres}
export MEVEO_DB_PORT=${MEVEO_DB_PORT:-5432}
export MEVEO_DB_NAME=${MEVEO_DB_NAME:-meveo}
export MEVEO_DB_USERNAME=${MEVEO_DB_USERNAME:-meveo}
export MEVEO_DB_PASSWORD=${MEVEO_DB_PASSWORD:-meveo}

# Wildfly parameters
export WILDFLY_BIND_ADDR=${WILDFLY_BIND_ADDR:-0.0.0.0}
export WILDFLY_MANAGEMENT_BIND_ADDR=${WILDFLY_MANAGEMENT_BIND_ADDR:-0.0.0.0}
export WILDFLY_PROXY_ADDRESS_FORWARDING=${WILDFLY_PROXY_ADDRESS_FORWARDING:-false}
export WILDFLY_LOG_CONSOLE_LEVEL=${WILDFLY_LOG_CONSOLE_LEVEL:-INFO}
export WILDFLY_LOG_FILE_LEVEL=${WILDFLY_LOG_FILE_LEVEL:-INFO}
export MEVEO_LOG_LEVEL=${MEVEO_LOG_LEVEL:-DEBUG}

# Debug parameters
export WILDFLY_DEBUG_ENABLE=${WILDFLY_DEBUG_ENABLE:-false}
export WILDFLY_DEBUG_PORT=${WILDFLY_DEBUG_PORT:-9999}
if [ "${WILDFLY_DEBUG_ENABLE}" = true ]; then
    export WILDFLY_LOG_CONSOLE_LEVEL=DEBUG
    export WILDFLY_LOG_FILE_LEVEL=DEBUG
fi

# Keycloak parameters
# if [ "x${KEYCLOAK_URL}" = "x" ]; then
#     if [ "x${DOMAIN_NAME}" = "x" ]; then
#         export KEYCLOAK_URL="http://localhost:8080/auth"
#     else
#         export KEYCLOAK_URL="https://${DOMAIN_NAME}/auth"
#     fi
#     export KEYCLOAK_FIXED_HOSTNAME=${DOMAIN_NAME:-localhost}
# else
#     export KEYCLOAK_URL=${KEYCLOAK_URL}
#     domain=$(echo ${KEYCLOAK_URL} | cut -d'/' -f3 | cut -d':' -f1)
#     export KEYCLOAK_FIXED_HOSTNAME=${domain}
# fi
export KEYCLOAK_URL=${KEYCLOAK_URL:-http://localhost:8080/auth}
export KEYCLOAK_REALM=${KEYCLOAK_REALM:-meveo}
export KEYCLOAK_CLIENT=${KEYCLOAK_CLIENT:-meveo-web}
export KEYCLOAK_SECRET=${KEYCLOAK_SECRET:-afe07e5a-68cb-4fb0-8b75-5b6053b07dc3}


# Reset standalone-full.xml file
if [ -f ${JBOSS_HOME}/standalone/configuration/standalone-full.xml.org ]; then
    cp -rf ${JBOSS_HOME}/standalone/configuration/standalone-full.xml.org ${JBOSS_HOME}/standalone/configuration/standalone-full.xml
else
    ERROR=1; exit_with_error "No default configuration file : ${JBOSS_HOME}/standalone/configuration/standalone-full.xml.org"
fi

# Configure standalone-full.xml
if [ -f ${JBOSS_HOME}/cli/standalone-configuration.cli ]; then
    info "Configure standalone-full.xml"
    ${JBOSS_HOME}/bin/jboss-cli.sh --file=${JBOSS_HOME}/cli/standalone-configuration.cli
fi

# Disable logging
if [ "$DISABLE_LOGGING" = true ]; then
    if [ -f ${JBOSS_HOME}/cli/disable-logging.cli ]; then
        info "Disable the logging of wildfly"
        ${JBOSS_HOME}/bin/jboss-cli.sh --file=${JBOSS_HOME}/cli/disable-logging.cli
    fi
fi

# Run entrypoint scripts if need to run extra cli
if [ -d /docker-entrypoint-initdb.d ]; then
    for f in /docker-entrypoint-initdb.d/*.sh; do
        [ -f "$f" ] && . "$f"
    done
fi


system_memory_in_mb=`free -m | awk '/:/ {print $2;exit}'`
system_cpu_cores=`egrep -c 'processor([[:space:]]+):.*' /proc/cpuinfo`

# Check whether container has enough memory
if [ ${system_memory_in_mb} -lt 1500 ]; then
    ERROR=1; exit_with_error "The container doesn't have enough memory. Needs at least 2 GB of memory. Currently the available memory is ${system_memory_in_mb} MB."
fi

if [ "${system_cpu_cores}" -lt "1" ]; then
    system_cpu_cores="1"
fi


if [ "x${JBOSS_MODULES_SYSTEM_PKGS}" = "x" ]; then
   JBOSS_MODULES_SYSTEM_PKGS="org.jboss.byteman"
fi

#
# Specify options to pass to the Java VM.
#
if [ "x${JAVA_OPTS}" = "x" ]; then
    if [ "x${WILDFLY_CUSTOM_XMS}" = "x" ]; then
        WILDFLY_CUSTOM_XMS="1024m"
    fi
    if [ "x${WILDFLY_CUSTOM_XMX}" = "x" ]; then
        WILDFLY_CUSTOM_XMX="2048m"
    fi
    JAVA_OPTS="-Xms${WILDFLY_CUSTOM_XMS} -Xmx${WILDFLY_CUSTOM_XMX}"
    JAVA_OPTS="${JAVA_OPTS} -XX:MetaspaceSize=300m -XX:MaxMetaspaceSize=500m"
    JAVA_OPTS="${JAVA_OPTS} -XX:ParallelGCThreads=${system_cpu_cores} -XshowSettings:vm"
    JAVA_OPTS="${JAVA_OPTS} -Djava.net.preferIPv4Stack=true -Djboss.modules.system.pkgs=${JBOSS_MODULES_SYSTEM_PKGS} -Djava.awt.headless=true"
else
    info "JAVA_OPTS already set in environment; overriding default settings with values: ${JAVA_OPTS}"
fi

export JAVA_OPTS=$JAVA_OPTS

# Set admin user/pass for wildfly
if [ ! -z "${WILDFLY_ADMIN_USER}" -a ! -z "${WILDFLY_ADMIN_PASSWORD}" ]; then
    if [ -f ${JBOSS_HOME}/bin/add-user.sh ]; then
        info "Add an wildfly user : ${WILDFLY_ADMIN_USER}"
        ${JBOSS_HOME}/bin/add-user.sh -u ${WILDFLY_ADMIN_USER} -p ${WILDFLY_ADMIN_PASSWORD} --silent
    fi
fi

# Set admin user/pass for keycloak
if [ ! -z "${KEYCLOAK_ADMIN_USER}" -a ! -z "${KEYCLOAK_ADMIN_PASSWORD}" ]; then
    if [ -f ${JBOSS_HOME}/bin/add-user-keycloak.sh ]; then
        info "Add a keycloak user : ${KEYCLOAK_ADMIN_USER}"
        ${JBOSS_HOME}/bin/add-user-keycloak.sh --user ${KEYCLOAK_ADMIN_USER} --password ${KEYCLOAK_ADMIN_PASSWORD}
    fi
fi

WILDFLY_OPTS="-b ${WILDFLY_BIND_ADDR} -bmanagement ${WILDFLY_MANAGEMENT_BIND_ADDR}"
if [ "${WILDFLY_DEBUG_ENABLE}" = true ]; then
    WILDFLY_OPTS="${WILDFLY_OPTS} --debug *:${WILDFLY_DEBUG_PORT}"
fi

info "Starting Wildfly"
exec ${JBOSS_HOME}/bin/standalone.sh ${WILDFLY_OPTS} -c standalone-full.xml

exit 0