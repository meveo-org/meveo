package org.meveo.admin.action.admin.custom;

import java.util.List;
import java.util.Map;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.persistence.Table;

import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.service.custom.NativeCustomEntityInstanceService;
import org.meveo.util.view.CrossStorageDataModel;
import org.primefaces.model.LazyDataModel;

@Named
@ViewScoped
public class CustomEntityInstanceListBean extends CustomEntityInstanceBean {

	private static final long serialVersionUID = 2227098775326177111L;

	@Inject
	private NativeCustomEntityInstanceService nativeCustomEntityInstanceService;

	private LazyDataModel<Map<String, Object>> nativeDataModel;

	private List<Map<String, Object>> selectedValues;

	public void initialize() {
		customEntityTemplate = customEntityTemplateService.findByCode(customEntityTemplateCode);

		if (customEntityTemplate.getSqlStorageConfigurationNullSafe().isStoreAsTable()) {
			if (customTableName == null) {
				customTableName = SQLStorageConfiguration.getDbTablename(customEntityTemplate);
			}

		} else {
			Table table = CustomEntityInstance.class.getAnnotation(Table.class);
			customTableName = table.name();
		}
	}

	public LazyDataModel<Map<String, Object>> getNativeDataModel() throws NamingException {

		return getNativeDataModel(filters);
	}

	/**
	 * DataModel for primefaces lazy loading datatable component.
	 * 
	 * @param inputFilters Search criteria
	 * @return LazyDataModel implementation.
	 * @throws NamingException
	 */
	public LazyDataModel<Map<String, Object>> getNativeDataModel(Map<String, Object> inputFilters) throws NamingException {

		if (nativeDataModel == null && getCustomEntityTemplate() != null && getRepository() != null) {

			final Map<String, Object> filters = inputFilters;

			nativeDataModel = new CrossStorageDataModel() {

				private static final long serialVersionUID = 6682319740448829853L;

				@Override
				protected Map<String, Object> getSearchCriteria() {
					return filters;
				}

				@Override
				protected Repository getRepository() {
					return CustomEntityInstanceListBean.this.getRepository();
				}

				@Override
				protected CustomEntityTemplate getCustomEntityTemplate() {
					return CustomEntityInstanceListBean.this.getCustomEntityTemplate();
				}

			};
		}

		return nativeDataModel;
	}

	public void handleRepositoryChangeEvent() {
		dataModel = null;
		filters = null;
	}

	public List<Repository> listRepositories() {
		return repositoryService.listByCet(customEntityTemplate);
	}

	public List<Map<String, Object>> getSelectedValues() {
		return selectedValues;
	}

	public void setSelectedValues(List<Map<String, Object>> selectedValues) {
		this.selectedValues = selectedValues;
	}

	public void setNativeDataModel(LazyDataModel<Map<String, Object>> nativeDataModel) {
		this.nativeDataModel = nativeDataModel;
	}
}