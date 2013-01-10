/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.manaty.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.meveo.model.mediation.MedinaConfiguration;
import org.manaty.telecom.mediation.MedinaPersistence;

/**
 * Similar as {@link ParamBean} just loads configuration not from file
 * but from database.
 * 
 * @author Ignas
 *
 */
public class DBConfigBean {
	
	private Map<String, String> properties = new HashMap<String, String>();
	
	private static final Logger logger = Logger.getLogger(DBConfigBean.class.getName());
	
	private static DBConfigBean instance;
	
	static {
		instance = new DBConfigBean();
	}
	
	private DBConfigBean() {
		super();
		loadProperties();
	}
	
	@SuppressWarnings("unchecked")
	private void loadProperties() {
		logger.info("Loading medina configuration from database:");
		EntityManager em = MedinaPersistence.getEntityManager();
		Query q = em.createQuery("select mc from MedinaConfiguration mc");
		List<MedinaConfiguration> entries = (List<MedinaConfiguration>)q.getResultList();
		Map<String, String> loadedProperties = new HashMap<String, String>();
		for (MedinaConfiguration config : entries) {
			loadedProperties.put(config.getKey(), config.getValue());
			logger.info(config.getKey() + " = " + config.getValue());
		}
		properties = Collections.unmodifiableMap(loadedProperties); // immutable map
		logger.info(entries.size() + " properties was loaded.");
	}
	
	public static DBConfigBean getInstance() {
		return instance;
	}
	
	/**
	 * Synchronize with DB and refresh properties. Because refreshProperties is invoked from batch job, it does not require that property change
	 * would be seen immediately. Since batch job refreshes properties only after some time, Medina does not see most up to date values all the time
	 * but thats currently is ok. Because loadProperties does not change values of properties map, but loads all properties from db to new map and
	 * then reference properties variable to new map, no synchronization is used (properties map is immutable). Because of reason that no precision is 
	 * required properties map can be not final (one thread can read one version of map and other thread new version after refresh, thats perfectly fine
	 * for now).
	 */
	public void refreshProperties() {
		loadProperties();
	}
	
	public String getProperty(String key) {
		return properties.get(key);
	}
	
	public String getProperty(String key, String defaulValue) {
		String property = getProperty(key);
		if (property == null) {
			property = defaulValue;
		}
		return property;
	}

}
