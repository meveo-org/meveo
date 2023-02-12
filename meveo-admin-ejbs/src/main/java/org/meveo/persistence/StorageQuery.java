/**
 * 
 */
package org.meveo.persistence;

import java.util.Map;
import java.util.Set;

import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.IStorageConfiguration;

public class StorageQuery {
	
	public static StorageQuery fromCei(CustomEntityInstance cei, IStorageConfiguration storage) {
		StorageQuery query = new StorageQuery();
		query.setCet(cei.getCet());
		query.setStorageConfiguration(storage);
		var values = cei.getCfValuesAsValues(storage.getDbStorageType(), cei.getFieldTemplates().values(), true);
		for (var cft : cei.getFieldTemplates().values()) {
			if (!cft.isUnique()) {
				values.remove(cft.getCode());
			}
		}
		query.setFilters(values);
		query.setPaginationConfiguration(new PaginationConfiguration(values));
		return query;
	}
	
	private Map<String, Object> filters;
	private PaginationConfiguration paginationConfiguration;
	private CustomEntityTemplate cet;
	private IStorageConfiguration storageConfiguration;
	private Set<String> fetchFields;
	private Map<String, Set<String>> subFields;
	private boolean fetchAllFields;
	
	/**
	 * @return the {@link #fetchAllFields}
	 */
	public boolean isFetchAllFields() {
		return fetchAllFields;
	}
	
	/**
	 * @param fetchAllFields the fetchAllFields to set
	 */
	public void setFetchAllFields(boolean fetchAllFields) {
		this.fetchAllFields = fetchAllFields;
	}
	/**
	 * @return the {@link #filters}
	 */
	public Map<String, Object> getFilters() {
		return filters;
	}
	/**
	 * @param filters the filters to set
	 */
	public void setFilters(Map<String, Object> filters) {
		this.filters = filters;
	}
	/**
	 * @return the {@link #paginationConfiguration}
	 */
	public PaginationConfiguration getPaginationConfiguration() {
		return paginationConfiguration;
	}
	/**
	 * @param paginationConfiguration the paginationConfiguration to set
	 */
	public void setPaginationConfiguration(PaginationConfiguration paginationConfiguration) {
		this.paginationConfiguration = paginationConfiguration;
	}
	/**
	 * @return the {@link #cet}
	 */
	public CustomEntityTemplate getCet() {
		return cet;
	}
	/**
	 * @param cet the cet to set
	 */
	public void setCet(CustomEntityTemplate cet) {
		this.cet = cet;
	}
	/**
	 * @return the {@link #storageConfiguration}
	 */
	public IStorageConfiguration getStorageConfiguration() {
		return storageConfiguration;
	}
	/**
	 * @param repository the repository to set
	 */
	public void setStorageConfiguration(IStorageConfiguration repository) {
		this.storageConfiguration = repository;
	}
	/**
	 * @return the {@link #fetchFields}
	 */
	public Set<String> getFetchFields() {
		return fetchFields;
	}
	/**
	 * @param fetchFields the fetchFields to set
	 */
	public void setFetchFields(Set<String> fetchFields) {
		this.fetchFields = fetchFields;
	}
	/**
	 * @return the {@link #subFields}
	 */
	public Map<String, Set<String>> getSubFields() {
		return subFields;
	}
	/**
	 * @param subFields the subFields to set
	 */
	public void setSubFields(Map<String, Set<String>> subFields) {
		this.subFields = subFields;
	}
	
}
