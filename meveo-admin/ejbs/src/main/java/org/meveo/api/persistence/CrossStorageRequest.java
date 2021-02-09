/**
 * 
 */
package org.meveo.api.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.CEIUtils;
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
	private List<String> relationsToFetch;
	
	public CrossStorageRequest(Repository repo, CrossStorageService api, Class<T> clazz, CustomEntityTemplate cet) {
		repository = repo;
		this.api = api;
		this.clazz = clazz;
		this.cet = cet;
		configuration = new PaginationConfiguration();
		configuration.setFilters(new HashMap<>());
		this.relationsToFetch = new ArrayList<>();
	}
	
	public CrossStorageRequest<T> by(String field, Object value) {
		configuration.getFilters().put(field, value);
		return this;
	}
	
	public CrossStorageRequest<T> fetch(String field) {
		this.relationsToFetch.add(field);
		return this;
	}
	
	public List<T> getResults() {
		try {
			var results = api.find(repository, cet, configuration);
			
			if(!relationsToFetch.isEmpty()) {
				results.forEach(this::fetch);
			}
			
			return results.stream()
					.map(v -> CEIUtils.deserialize(v, clazz))
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
			
			if(!relationsToFetch.isEmpty()) {
				fetch(values.get(0));
			}
			
			return CEIUtils.deserialize(values.get(0), clazz);
			
		} catch (EntityDoesNotExistsException e) {
			return null;
		}
	}
	
	public Optional<T> getResultOpt() {
		return Optional.ofNullable(getResult());
	}
	
	private void fetch(Map<String, Object> values) {
		Map<String, Object> valuesToFetch = new HashMap<>(values);
		values.forEach((k,v) -> {
			if(!k.equals("uuid") && !relationsToFetch.contains(k)) {
				valuesToFetch.remove(k);
			}
		});
		
		try {
			api.fetchEntityReferences(repository, cet, valuesToFetch);
			valuesToFetch.forEach(values::put);
		} catch (EntityDoesNotExistsException e) {
			throw new RuntimeException(e);
		}
	}

	

}
