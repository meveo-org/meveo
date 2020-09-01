/**
 * 
 */
package org.meveo.api.persistence;

import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class CrossStorageRequest<T> {
	
	private Repository repository;
	private CrossStorageService api;
	private Class<T> clazz;
	private PaginationConfiguration configuration;
	private CustomEntityTemplate cet;
	
	public CrossStorageRequest(Repository repo, CrossStorageService api, Class<T> clazz, CustomEntityTemplate cet) {
		repository = repo;
		this.api = api;
		this.clazz = clazz;
		this.cet = cet;
		configuration = new PaginationConfiguration();
		configuration.setFilters(new HashMap<>());
	}
	
	public CrossStorageRequest<T> by(String field, Object value) {
		configuration.getFilters().put(field, value);
		return this;
	}
	
	public List<T> getResults() {
		try {
			return api.find(repository, cet, configuration)
					.stream()
					.map(value -> JacksonUtil.convert(value, clazz))
					.collect(Collectors.toList());
		} catch (EntityDoesNotExistsException e) {
			return List.of();
		}
	}
	
	public T getResult() {
		try {
			var values = api.find(repository, cet, configuration);
			if(values == null || values.isEmpty()) {
				return null;
			}
			
			return JacksonUtil.convert(values.get(0), clazz);
		} catch (EntityDoesNotExistsException e) {
			return null;
		}
	}
	
	
	
	

}
