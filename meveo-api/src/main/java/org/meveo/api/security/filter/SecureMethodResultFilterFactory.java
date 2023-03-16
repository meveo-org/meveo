package org.meveo.api.security.filter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import javax.ejb.Startup;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This factory encapsulates the creation and retrieval of
 * {@link SecureMethodResultFilter} instances.
 * 
 * @author Tony Alejandro
 *
 */
@Startup
@Singleton
public class SecureMethodResultFilterFactory implements Serializable {

	private static final long serialVersionUID = 2249067511854832348L;

	@Any
	@Inject
	private Instance<SecureMethodResultFilter> filters;

	private static Logger log = LoggerFactory.getLogger(SecureMethodResultFilterFactory.class);

	private Map<Class<? extends SecureMethodResultFilter>, SecureMethodResultFilter> filterMap = new HashMap<>();

	public SecureMethodResultFilter getFilter(Class<? extends SecureMethodResultFilter> filterClass) {
		initialize();
		SecureMethodResultFilter filter = filterMap.get(filterClass);
		if (filter == null) {
			log.warn("No SecuredBusinessEntityFilter instance of type {} found.", filterClass.getName());
		}
		return filter;
	}

	private void initialize() {
		if (filterMap.isEmpty() && filters!=null) {
			log.debug("Initializing filter map.");
			for (SecureMethodResultFilter filter : filters) {
				filterMap.put(filter.getClass(), filter);
			}
			log.debug("Filter map Initialization done. Found {} filters.", filterMap.size());
		}
	}
}
