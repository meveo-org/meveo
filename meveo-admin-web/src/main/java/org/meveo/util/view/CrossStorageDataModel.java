/**
 * 
 */
package org.meveo.util.view;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.enterprise.inject.spi.CDI;

import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This data model is use to display a list of custom entities derive from cross
 * storage.
 * 
 * @see CrossStorageService
 * @author clement.bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.8.0
 * @version 6.8.0
 */
public abstract class CrossStorageDataModel extends LazyDataModel<Map<String, Object>> {

	private static final long serialVersionUID = 1L;

	private CrossStorageService persistenceService = CDI.current().select(CrossStorageService.class).get();
	
	private CustomFieldTemplateService cftService = CDI.current().select(CustomFieldTemplateService.class).get();

	private Logger log = LoggerFactory.getLogger(CrossStorageDataModel.class);

	private Integer rowCount;

	private Integer rowIndex;

	protected abstract Repository getRepository();

	protected abstract CustomEntityTemplate getCustomEntityTemplate();
	
	@Override
	public List<Map<String, Object>> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> loadingFilters) {

		PaginationConfiguration paginationConfig = new PaginationConfiguration(first, 
				pageSize, 
				getSearchCriteria(loadingFilters), 
				null, 
				getListFieldsToFetchImpl(), 
				sortField,
				sortOrder);
		
		List<String> summaryFields = cftService.findByAppliesTo(getCustomEntityTemplate().getAppliesTo())
				.values()
				.stream()
				.filter(CustomFieldTemplate::isSummary)
				.sorted((field1, field2) -> field1.getGUIFieldPosition() - field2.getGUIFieldPosition())
				.map(CustomFieldTemplate::getCode)
				.collect(Collectors.toList());
		
		paginationConfig.setFetchFields(summaryFields);

		try {
			setRowCount(persistenceService.count(getRepository(), getCustomEntityTemplate(), paginationConfig));

			if (getRowCount() > 0) {
				return persistenceService.find(getRepository(), getCustomEntityTemplate(), paginationConfig);
			}

		} catch (EntityDoesNotExistsException e) {
			log.error("Error retrieving data", e);
		}

		return new ArrayList<>();
	}

	@Override
	public void setRowIndex(int rowIndex) {
		if (rowIndex == -1 || getPageSize() == 0) {
			this.rowIndex = rowIndex;
		} else {
			this.rowIndex = rowIndex % getPageSize();
		}
	}

	@Override
	public int getRowIndex() {
		return this.rowIndex;
	}

	@Override
	public boolean isRowAvailable() {
		if (getWrappedData() == null) {
			return false;
		}

		return rowIndex >= 0 && rowIndex < (getWrappedData()).size();
	}

	@Override
	public Object getRowKey(Map<String, Object> object) {
		return Optional.ofNullable(object.get("uuid")).orElse(object.get("meveo_uuid"));
	}

	@Override
	public void setRowCount(int rowCount) {
		this.rowCount = rowCount;
	}

	@Override
	public int getRowCount() {
		return rowCount != null ? rowCount : 0;
	}

	@Override
	public Map<String, Object> getRowData() {
		return (getWrappedData()).get(rowIndex);
	}

	@Override
	public Map<String, Object> getRowData(String uuid) {

		try {
			return persistenceService.find(getRepository(), getCustomEntityTemplate(), uuid, true);
		} catch (EntityDoesNotExistsException e) {
			log.error("Error retrieving detail", e);
		}

		return null;
	}

	/**
	 * Get default sort.
	 * 
	 * @return default sort implementation
	 */
	protected String getDefaultSortImpl() {
		return "uuid";
	}

	protected SortOrder getDefaultSortOrderImpl() {
		return SortOrder.ASCENDING;
	}

	/**
	 * Override this method if you need to fetch any fields when selecting list of
	 * entities in data table. Return list of field names that has to be fetched.
	 * 
	 * @return List of fields to fetch
	 */
	protected List<String> getListFieldsToFetchImpl() {
		return null;
	}

	/**
	 * A method to mock List/Set/Collection size property, so it is easy to be used
	 * in EL expressions.
	 * 
	 * @return Size of rows
	 */
	public Integer size() {
		return rowCount;
	}

	/**
	 * Get search criteria for data searching.&lt;br/&gt; Search criteria is a map
	 * with filter criteria name as a key and value as a value. &lt;br/&gt; Criteria
	 * name consist of [&lt;condition&gt; ]&lt;field name&gt; (e.g. "like
	 * firstName") where &lt;condition&gt; is a condition to apply to field value
	 * comparison and &lt;field name&gt; is an entity attribute name.
	 *
	 * @param filters the filters
	 * @return the search criteria
	 */
	protected Map<String, Object> getSearchCriteria(Map<String, Object> filters) {
		return getSearchCriteria();
	}

	/**
	 * Get search criteria for data searching.&lt;br/&gt; Search criteria is a map
	 * with filter criteria name as a key and value as a value. &lt;br/&gt; Criteria
	 * name consist of [&lt;condition&gt; ]&lt;field name&gt; (e.g. "like
	 * firstName") where &lt;condition&gt; is a condition to apply to field value
	 * comparison and &lt;field name&gt; is an entity attribute name.
	 * 
	 * @return Map of search criteria
	 */
	protected abstract Map<String, Object> getSearchCriteria();

}
