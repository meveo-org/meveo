package org.meveo.admin.action.admin.custom;

import java.util.List;
import java.util.Map;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.Table;

import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.service.base.NativePersistenceService;
import org.meveo.service.custom.NativeCustomEntityInstanceService;
import org.meveo.util.view.NativeTableBasedDataModel;
import org.primefaces.model.LazyDataModel;

@Named
@ConversationScoped
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

	public LazyDataModel<Map<String, Object>> getNativeDataModel() {

		return getNativeDataModel(filters);
	}

	/**
	 * DataModel for primefaces lazy loading datatable component.
	 * 
	 * @param inputFilters Search criteria
	 * @return LazyDataModel implementation.
	 */
	public LazyDataModel<Map<String, Object>> getNativeDataModel(Map<String, Object> inputFilters) {

		if (nativeDataModel == null && customTableName != null) {

			final Map<String, Object> filters = inputFilters;

			nativeDataModel = new NativeTableBasedDataModel() {

				private static final long serialVersionUID = 6682319740448829853L;

				@Override
				protected Map<String, Object> getSearchCriteria() {
					return filters;
				}

				@Override
				protected NativePersistenceService getPersistenceServiceImpl() {
					return nativeCustomEntityInstanceService;
				}

				@Override
				protected String getTableName() {
					return CustomEntityInstanceListBean.this.getCustomTableName();
				}

				@Override
				protected CustomEntityTemplate getCet() {
					return customEntityTemplate;
				}

				@Override
				protected String getSqlConnectionCode() {
					return CustomEntityInstanceListBean.this.getSqlConnectionCode();
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