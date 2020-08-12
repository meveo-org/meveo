/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.commons.utils;

import org.apache.mina.util.ConcurrentHashSet;
import org.meveo.api.ApiService;
import org.meveo.model.IEntity;
import org.meveo.service.base.PersistenceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.enterprise.inject.spi.CDI;
import javax.enterprise.util.TypeLiteral;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Util class for remote ejb lookups.
 * 
 * @author Ignas Lelys
 *
 */
public class EjbUtils {

    private static final Logger logger = LoggerFactory.getLogger(EjbUtils.class);

    private static final String LOCALHOST = "127.0.0.1";
    private static final Set<PersistenceService<?>> persistenceServices;
    private static final Set<ApiService<?,?>> apiServices;


    static {
        persistenceServices = new ConcurrentHashSet<>();
        TypeLiteral<PersistenceService<?>> type = new TypeLiteral<PersistenceService<?>>() {};
        persistenceServices.addAll(CDI.current().select(type).stream().collect(Collectors.toList()));

        apiServices = new ConcurrentHashSet<>();
        TypeLiteral<ApiService<?,?>> apiType = new TypeLiteral<ApiService<?,?>>() {};
        apiServices.addAll(CDI.current().select(apiType).stream().collect(Collectors.toList()));
    }

    /**
     * Non instantiable class.
     */
    private EjbUtils() {

    }

    /**
     * Obtain interface of an object in JNDI.
     * 
     * @param nameEJB Full JNDI path to an object
     * @return Object instance
     * @throws NamingException naming exception.
     * 
     */
    public static Object getInterface(String nameEJB) throws NamingException {
    	// Special cases handling
    	if(nameEJB.equals("ScriptNotificationApi")) {
    		nameEJB = "NotificationApi";
    	}
    	
        InitialContext ctx = null;
        if (System.getenv("JBOSS_HOST") != null) {
            logger.info(String.format("JBOSS_HOST=", System.getenv("JBOSS_HOST")));
            ctx = getInitialContext(System.getenv("JBOSS_HOST"));
        } else {
            ctx = getInitialContext(LOCALHOST);
        }
        return ctx.lookup(nameEJB);
    }

    /**
     * Obtain remote interface of an object in JNDI.
     * 
     * @param nameEJB Full JNDI path to an object
     * @param serverName Server address where to look for an object
     * @return Object instance
     * @throws NamingException naming exception.
     * 
     */
    public static Object getRemoteInterface(String nameEJB, String serverName) throws NamingException {
        InitialContext ctx = (InitialContext) getInitialContext(serverName);
        return ctx.lookup(nameEJB);
    }

    /**
     * @param serverName server name
     * @return initial context
     * @throws NamingException naming exception.
     */
    private static InitialContext getInitialContext(String serverName) throws NamingException {
        Properties properties = new Properties();
        properties.put(Context.INITIAL_CONTEXT_FACTORY, "org.jnp.interfaces.NamingContextFactory");
        properties.put(Context.URL_PKG_PREFIXES, "org.jboss.naming:org.jnp.interfaces");
        properties.put(Context.PROVIDER_URL, serverName);
        return new InitialContext(properties);
    }
    
    /**
     * Return a service by a service interface name.
     * 
     * @param serviceInterfaceName 	A simple name of a service class (NOT a full classname). E.g. WorkflowService
     * @param failSilently			Whether to log an error if implementation is not found
     * @return Service instance
     */
    public static Object getServiceInterface(String serviceInterfaceName, boolean failSilently) {
        try {
            InitialContext ic = new InitialContext();
            return ic.lookup("java:global/" + ParamBean.getInstance().getProperty("meveo.moduleName", "meveo") + "/" + serviceInterfaceName);
        } catch (Exception e) {
        	if(!failSilently) {
	            Logger log = LoggerFactory.getLogger(EjbUtils.class);
	            log.error("Failed to obtain service interface for {} {}", serviceInterfaceName, e.getMessage());
        	}
        }
        return null;
    }

    /**
     * Return a service by a service interface name.
     * 
     * @param serviceInterfaceName A simple name of a service class (NOT a full classname). E.g. WorkflowService
     * @return Service instance
     */
    public static Object getServiceInterface(String serviceInterfaceName) {
        return getServiceInterface(serviceInterfaceName, false);
    }

    /**
     * Return a persistence service for a given entity class.
     * 
     * @param entityClass Entity class
     * @return Persistence service
     */
    @SuppressWarnings("rawtypes")
    public static Object getServiceInterface(Class entityClass) {
        return getServiceInterface(entityClass.getSimpleName() + "Service");
    }

    public static String getCurrentClusterNode() {
        return System.getProperty("jboss.node.name");
    }

    public static boolean isRunningInClusterMode() {
        String nodeName = System.getProperty("jboss.node.name");
        return  nodeName!= null && nodeName.startsWith("meveo");
    }

    public static Optional<PersistenceService<?>> getPersistenceService(IEntity entityToSave) {
        // Try to find the associated persistence service
        return getPersistenceService(entityToSave.getClass());
    }

    public static Optional<PersistenceService<?>> getPersistenceService(Class clazz) {
        // Try to find the associated persistence service
        return persistenceServices
                .stream()
                .filter(service -> service.getEntityClass().equals(clazz))
                .findFirst();
    }
}