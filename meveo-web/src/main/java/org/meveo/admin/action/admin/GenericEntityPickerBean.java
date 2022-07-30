package org.meveo.admin.action.admin;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import java.util.stream.Collectors;
import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.action.BaseBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.module.MeveoModuleApi;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.IEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.base.BaseEntityService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.storage.RepositoryService;
import org.primefaces.model.LazyDataModel;

@ViewScoped
@Named
public class GenericEntityPickerBean extends BaseBean<IEntity> {

    private static final long serialVersionUID = 115130709397837651L;

    private Class<? extends IEntity> selectedEntityClass;

    @Inject
    private BaseEntityService baseEntityService;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private transient CrossStorageService crossStorageService;

    @Inject
    private CustomFieldInstanceService customFieldInstanceService;

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;
    
    @Inject
    private RepositoryService repositoryService;
    
    @Inject
    private MeveoModuleApi moduleApi;

    private List<CustomEntityInstance> customEntityInstances = new ArrayList<>();

    private List<CustomEntityTemplate> customEntityTemplates = new ArrayList<>();

    private CustomEntityTemplate customEntityTemplate;
    

    private static final ConcurrentHashMap<Class<? extends Annotation>, Collection<Class<?>>> classesByAnnotation = new ConcurrentHashMap<>();

    /**
     * Get a list of classes that contain the given annotation
     *
     * @param annotation Annotation classname
     * @return A list of classes
     */
    public List<Class<?>> getEntityClasses(String annotation) {
        if(annotation.equals(ModuleItem.class.getName())) {
            List<Class<?>> classes = moduleApi.getModuleItemClasses();
            return classes.stream().sorted(Comparator.comparing(Class::getSimpleName)).collect(Collectors.toUnmodifiableList());
        }
    	
        try {

            final Class<? extends Annotation> annotationClass = (Class<? extends Annotation>) Class.forName(annotation);
            classesByAnnotation.computeIfAbsent(annotationClass, a -> ReflectionUtils.getClassesAnnotatedWith(a, ""));

            List<Class<?>> classes = new ArrayList<>(classesByAnnotation.get(annotationClass));
            classes.sort(Comparator.comparing(Class::getName));
            return classes;

        } catch (ClassNotFoundException e) {
            return new ArrayList<>();
        }
    }

    public Class<? extends IEntity> getSelectedEntityClass() {
        return selectedEntityClass;
    }

    public void setSelectedEntityClass(Class<? extends IEntity> selectedEntityClass) {
        this.selectedEntityClass = selectedEntityClass;
        setClazz((Class<IEntity>) selectedEntityClass);
        baseEntityService.setEntityClass((Class<IEntity>) selectedEntityClass);
    }

    @Override
    protected IPersistenceService<IEntity> getPersistenceService() {
        return baseEntityService;
    }

    @Override
    public LazyDataModel<IEntity> getLazyDataModel() {
        if (selectedEntityClass == null) {
            return null;
        } else {
            return super.getLazyDataModel();
        }
    }

    public List<CustomEntityInstance> getCustomEntityInstances() throws BusinessException {
        if (customEntityTemplate != null) {
            customEntityInstances.clear();
            customEntityInstances.addAll(getCeiListFromCet(customEntityTemplate));
            return customEntityInstances;
        }
        if (CollectionUtils.isEmpty(customEntityInstances)) {
            List<CustomEntityTemplate> customEntityTemplates = customEntityTemplateService.list();
            if (CollectionUtils.isNotEmpty(customEntityTemplates)) {
                for (CustomEntityTemplate cet : customEntityTemplates) {
                    customEntityInstances.addAll(getCeiListFromCet(cet));
                }
            }
        }
        return customEntityInstances;
    }

    private List<CustomEntityInstance> getCeiListFromCet(CustomEntityTemplate customEntityTemplate) throws BusinessException {
    	List<CustomEntityInstance> ceiList = new ArrayList<>();
    	List<Map<String, Object>> values;
    	try {
    		values = crossStorageService.find(
				repositoryService.findDefaultRepository(), // XXX: Maybe we will need to parameterize this or search in all repositories ?
				customEntityTemplate, 
				new PaginationConfiguration()
			);
    	} catch (EntityDoesNotExistsException e) {
    		throw new RuntimeException(e);
    	}

    	if (CollectionUtils.isNotEmpty(values)) {
    		for (Map<String, Object> customEntity : values) {
    			CustomEntityInstance customEntityInstance = new CustomEntityInstance();
    			customEntityInstance.setUuid((String) customEntity.get("uuid"));
    			customEntityInstance.setCode((String) customEntity.get("uuid"));
    			String fieldName = customFieldTemplateService.getFieldName(customEntityTemplate);
    			if (fieldName != null) {
    				customEntityInstance.setDescription(fieldName + ": " + customEntity.get(fieldName));
    			}
    			customEntityInstance.setCet(customEntityTemplate);
    			customEntityInstance.setCetCode(customEntityTemplate.getCode());
    			customFieldInstanceService.setCfValues(customEntityInstance, customEntityTemplate.getCode(), customEntity);
    			ceiList.add(customEntityInstance);
    		}
    	}
    	return ceiList;
    }

    public List<CustomEntityTemplate> getCustomEntityTemplates() {
        customEntityTemplates = customEntityTemplateService.list();
        return customEntityTemplates;
    }

    public void setCustomEntityTemplates(List<CustomEntityTemplate> customEntityTemplates) {
        this.customEntityTemplates = customEntityTemplates;
    }

    public CustomEntityTemplate getCustomEntityTemplate() {
        return customEntityTemplate;
    }

    public void setCustomEntityTemplate(CustomEntityTemplate customEntityTemplate) {
        this.customEntityTemplate = customEntityTemplate;
    }

    @Override
    public void clean() {
        customEntityTemplate = null;
        customEntityInstances.clear();
        super.clean();
    }

    @Override
    protected String getDefaultSort() {
        return "code";
    }
}