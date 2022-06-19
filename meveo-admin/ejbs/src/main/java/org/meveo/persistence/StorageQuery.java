/**
 * 
 */
package org.meveo.persistence;

import java.util.Map;
import java.util.Set;

import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.storage.Repository;

/**
 * 
 * @author heros
 * @since 
 * @version
 */
public class StorageQuery {
	private Map<String, Object> filters;
	private PaginationConfiguration paginationConfiguration;
	private CustomEntityTemplate cet;
	private Repository repository;
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
	 * @return the {@link #repository}
	 */
	public Repository getRepository() {
		return repository;
	}
	/**
	 * @param repository the repository to set
	 */
	public void setRepository(Repository repository) {
		this.repository = repository;
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
