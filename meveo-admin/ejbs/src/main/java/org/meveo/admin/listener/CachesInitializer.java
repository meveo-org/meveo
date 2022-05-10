/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.admin.listener;

import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.cache.UserMessageCacheContainerProvider;
import org.meveo.commons.utils.ParamBean;
import org.slf4j.Logger;

import javax.annotation.PostConstruct;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.inject.Inject;
import javax.naming.InitialContext;

@Startup
@Singleton
public class CachesInitializer {

    // private static final String INFINISPAN_CACHE_LOCATION = "infinispan-cache.location";

    @Inject
    protected Logger log;

//    @Resource(lookup = "java:jboss/infinispan/container/meveo")
    private EmbeddedCacheManager cacheContainer;

    private ParamBean paramBean = ParamBean.getInstance();

    @PostConstruct
    protected void init() {
    	try {
			InitialContext initialContext = new InitialContext();
			cacheContainer = (EmbeddedCacheManager) initialContext.lookup("java:jboss/infinispan/container/meveo");
		} catch (Exception e) {
			log.error("Cannot instantiate cache container", e);
		}
    	
        log.info("Initializing ontology caches");

        PersistenceConfigurationBuilder confBuilder = new ConfigurationBuilder().persistence()
                .passivation(false);

        Configuration configuration = confBuilder.build();

        if (!cacheContainer.cacheExists(CustomFieldsCacheContainerProvider.MEVEO_CFT_CACHE)) {
            cacheContainer.defineConfiguration(CustomFieldsCacheContainerProvider.MEVEO_CFT_CACHE, configuration);
        }

        if (!cacheContainer.cacheExists(CustomFieldsCacheContainerProvider.MEVEO_CET_CACHE)) {
            cacheContainer.defineConfiguration(CustomFieldsCacheContainerProvider.MEVEO_CET_CACHE, configuration);
        }

        if (!cacheContainer.cacheExists(CustomFieldsCacheContainerProvider.MEVEO_CRT_CACHE)) {
            cacheContainer.defineConfiguration(CustomFieldsCacheContainerProvider.MEVEO_CRT_CACHE, configuration);
        }
        
        if (!cacheContainer.cacheExists(UserMessageCacheContainerProvider.MEVEO_USER_MESSAGE_CACHE)) {
            Configuration persistentFileConfig =confBuilder.addSingleFileStore()
                    .location(UserMessageCacheContainerProvider.MEVEO_USER_MESSAGE_CACHE).preload(true).purgeOnStartup(false).build();
            cacheContainer.defineConfiguration(UserMessageCacheContainerProvider.MEVEO_USER_MESSAGE_CACHE, persistentFileConfig);
        }

        log.info("Finished initializing ontology caches");
    }
}
