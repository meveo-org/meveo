package org.meveo.admin.action.admin.custom;

import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.enterprise.context.ConversationScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.commons.utils.StringUtils;
import org.meveo.service.custom.CustomizedEntity;
import org.meveo.service.custom.CustomizedEntityFilter;
import org.meveo.service.custom.CustomizedEntityService;
import org.meveo.util.view.CustomizedEntityLazyDataModel;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 */
@Named
@ConversationScoped
public class CustomEntityTemplateListBean extends CustomEntityTemplateBean {

    private static final long serialVersionUID = 7731570832396817056L;

    @Inject
    private CustomizedEntityService customizedEntityService;
    
    private LazyDataModel<CustomizedEntity> customizedEntityDM = null;

    @SuppressWarnings("unused")
	private List<CustomizedEntity> selectedCustomizedEntities;
    
    @PostConstruct
    public void init() {
    	this.filters.put("customEntity", true);
    }

    public LazyDataModel<CustomizedEntity> getCustomizedEntities() {

        if (customizedEntityDM != null) {
            return customizedEntityDM;
        }

        customizedEntityDM = new CustomizedEntityLazyDataModel<CustomizedEntity>() {
            private static final long serialVersionUID = 1L;

            @Override
            public Long getRowKey(CustomizedEntity object) {
                return object.getCustomEntityId();
            }

            @Override
            public int getRowCount() {
                return super.getRowCount();
            }

            @Override
            public CustomizedEntity getRowData() {
                return super.getRowData();
            }

            @Override
            public int getRowIndex() {
                return super.getRowIndex();
            }

            @Override
            public void setRowIndex(int rowIndex) {
                super.setRowIndex(rowIndex);
            }

            @Override
            public CustomizedEntity getRowData(String rowKey) {
                return super.getRowData(rowKey);
            }

            @Override
            public List<CustomizedEntity> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> loadingFilters) {

                List<CustomizedEntity> entities = null;
                String query = (String) filters.get("entityName");
				String cecId = (String) filters.get("cecId");
                boolean isCustomEntityOnly = filters.get("customEntity") != null && (boolean) filters.get("customEntity");
                String sortBy = sortOrder != null ? sortOrder.name() : null;
                String primitiveEntity = (String) filters.get("primitiveEntity");
                primitiveEntity = primitiveEntity == null ? "0" : primitiveEntity;
                
                CustomizedEntityFilter filter = new CustomizedEntityFilter();
                filter.setEntityName(query);                
                filter.setCustomEntityTemplatesOnly(isCustomEntityOnly);
                filter.setSortBy(sortField);
                filter.setSortBy(sortBy);
                filter.setPrimitiveEntity(primitiveEntity);
                
				if (StringUtils.isBlank(cecId)) {
					filter.setIncludeNonManagedEntities(false);
					filter.setIncludeParentClassesOnly(false);
					filter.setIncludeRelationships(false);
				
				} else {
					filter.setCecId(Long.valueOf(cecId));
				}
				
				entities = customizedEntityService.getCustomizedEntities(filter);
                setRowCount(entities.size());
                
                if(first>entities.size()) {
                	this.setRowIndex(0);
                	first=0;
                }

                return entities.subList(first, (first + pageSize) > entities.size() ? entities.size() : (first + pageSize));
            }
        };

        return customizedEntityDM;
    }
}