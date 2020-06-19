package org.meveo.admin.action.admin.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.faces.view.ViewScoped;
import javax.inject.Named;
import javax.naming.NamingException;
import javax.persistence.Table;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.storage.Repository;
import org.meveo.util.view.CrossStorageDataModel;
import org.omnifaces.util.Faces;
import org.primefaces.model.LazyDataModel;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@Named
@ViewScoped
public class CustomEntityInstanceListBean extends CustomEntityInstanceBean {

	private static final long serialVersionUID = 2227098775326177111L;

	private List<CustomFieldTemplate> customFieldTemplateList = new ArrayList<>();
	private List<CustomFieldTemplate> summaryFields;
	private List<CustomFieldTemplate> filterFields;

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

		// Get fields and sort them by GUI order
		Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(customEntityTemplate.getAppliesTo());
		if (cfts != null) {
			GroupedCustomField groupedCFTAndActions = new GroupedCustomField(cfts.values(), "Custom fields", true);
			List<GroupedCustomField> groupedCustomFields = groupedCFTAndActions.getChildren();
			if (groupedCustomFields != null) {
				int i = 0;
				for (GroupedCustomField groupedCustomField : groupedCustomFields.get(i).getChildren()) {
					List<CustomFieldTemplate> list = new ArrayList<>();
					if (groupedCustomField != null) {
						CustomFieldTemplate cft = (CustomFieldTemplate) groupedCustomField.getData();
						list.add(cft);
					}
					i++;
					customFieldTemplateList.addAll(list);
				}
			}

			summaryFields = customFieldTemplateList.stream().filter(CustomFieldTemplate::isSummary).collect(Collectors.toList());
			filterFields = customFieldTemplateList.stream().filter(CustomFieldTemplate::isFilter).collect(Collectors.toList());
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

	@Override
	public void clean() {

		super.clean();
		nativeDataModel = null;
	}

	public void handleRepositoryChangeEvent() {
		dataModel = null;
		filters = null;
		Faces.addResponseCookie("repository", getRepository().getCode(), Integer.MAX_VALUE);
		repositoryProvider.setRepository(getRepository());
	}

	public void executeCustomAction(List<Map<String, Object>> selectedValues, EntityCustomAction action) {
		try {
			for (Map<String, Object> entity : selectedValues) {
				CustomEntityInstance customEntityInstance = new CustomEntityInstance();
				if (!customEntityTemplate.isStoreAsTable()) {
					customEntityInstance = customEntityInstanceService.findByUuid(customEntityTemplate.getCode(), entity.get("uuid").toString());
				} else {
					customEntityInstance.setCet(customEntityTemplate);
					customEntityInstance.setCetCode(customEntityTemplate.getCode());
					customEntityInstance.setUuid((String) entity.get("uuid"));
					customEntityInstance.setCode((String) entity.get("code"));
					customEntityInstance.setDescription((String) entity.get("description"));
					customFieldInstanceService.setCfValues(customEntityInstance, customEntityTemplate.getCode(), entity);
				}
				customFieldDataEntryBean.executeCustomAction(customEntityInstance, action, null);
			}
			setSelectedValues(null);
		} catch (BusinessException e) {}
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

	public List<CustomFieldTemplate> getSummaryFields() {
		return summaryFields;
	}

	public void setSummaryFields(List<CustomFieldTemplate> summaryFields) {
		this.summaryFields = summaryFields;
	}

	public List<CustomFieldTemplate> getFilterFields() {
		return filterFields;
	}

	public void setFilterFields(List<CustomFieldTemplate> filterFields) {
		this.filterFields = filterFields;
	}

	public List<CustomFieldTemplate> getCustomFieldTemplateList() {
		return customFieldTemplateList;
	}

	public void setCustomFieldTemplateList(List<CustomFieldTemplate> customFieldTemplateList) {
		this.customFieldTemplateList = customFieldTemplateList;
	}
}