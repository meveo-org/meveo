/**
 * 
 */
package org.meveo.api.persistence;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.model.CustomEntity;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.primefaces.model.SortOrder;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 * @param <T> Type of the returned object
 */
public class CrossStorageRequest<T> {
	
	private Repository repository;
	private CrossStorageService api;
	private Class<T> clazz;
	private PaginationConfiguration configuration;
	private CustomEntityTemplate cet;
	private Set<String> relationsToFetch;
	
	public CrossStorageRequest(Repository repo, CrossStorageService api, Class<T> clazz, CustomEntityTemplate cet) {
		repository = repo;
		this.api = api;
		this.clazz = clazz;
		this.cet = cet;
		configuration = new PaginationConfiguration();
		configuration.setFilters(new HashMap<>());
		this.relationsToFetch = new HashSet<>();
	}
	
	public CrossStorageRequest<T> by(String field, Object value) {
		if (value instanceof CustomEntity) {
			configuration.getFilters().put(field, ((CustomEntity) value).getUuid());
		} else {
			configuration.getFilters().put(field, value);
		}
		return this;
	}
	
	public CrossStorageRequest<T> limit(int limit) {
		configuration.setNumberOfRows(limit);
		return this;
	}

	public CrossStorageRequest<T> offset(int offset) {
		configuration.setFirstRow(offset);
		return this;
	}
	
	public CrossStorageRequest<T> fetch(String field) {
		this.relationsToFetch.add(field);
		this.select(field + ".uuid");
		return this;
	}
	
	public CrossStorageRequest<T> select(String field) {
		if(this.configuration.getFetchFields() == null) {
			this.configuration.setFetchFields(new ArrayList<>());
		}
		this.configuration.getFetchFields().add(field);
		return this;
	}
	
	public CrossStorageRequest<T> orderBy(String orderField,boolean ascending){
		this.configuration.setSortField(orderField);
		this.configuration.setOrdering(ascending ? SortOrder.ASCENDING:SortOrder.DESCENDING);
		return this;
	}

	/**
	 * Filters the results on the given field, applying a LIKE query
	 * 
	 * @param filterField Field to apply filtering
	 * @param wildcard Wildcard to search
	 * @return the request builder
	 */
	public CrossStorageRequest<T> like(String filterField, String wildcard) {
		configuration.getFilters().put(filterField, "*" + wildcard + "*");
		return this;
	}
	
	@SuppressWarnings("unchecked")
	public List<T> getResults() {
		try {
			var results = api.find(repository, cet, configuration);
			
			if(!relationsToFetch.isEmpty()) {
				results.forEach(this::fetch);
			}
			
			return results.stream()
					.map(v -> {
						if(clazz.equals(CustomEntityInstance.class)) {
							return (T) CEIUtils.fromMap(v, cet);
						} else {
							return CEIUtils.deserialize(v, clazz);
						}
					})
					.collect(Collectors.toList());
		} catch (EntityDoesNotExistsException e) {
			return List.of();
		}
	}
	
	@SuppressWarnings("unchecked")
	public T getResult() {
		try {
			var values = api.find(repository, cet, configuration);
			if(values == null || values.isEmpty()) {
				return null;
			}
			
			if(!relationsToFetch.isEmpty()) {
				fetch(values.get(0));
			}
			
			if(clazz.equals(CustomEntityInstance.class)) {
				return (T) CEIUtils.fromMap(values.get(0), cet);
			} else {
				return CEIUtils.deserialize(values.get(0), clazz);
			}
			
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
			api.fetchEntityReferences(repository, cet, valuesToFetch, new HashMap<>());
			valuesToFetch.forEach(values::put);
		} catch (EntityDoesNotExistsException e) {
			throw new RuntimeException(e);
		}
	}
	

}
