/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.admin.action;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.context.Conversation;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.inject.Inject;
import javax.inject.Named;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.infinispan.Cache;
import org.jboss.seam.international.status.Messages;
import org.jboss.seam.international.status.builder.BundleKey;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ConstraintViolationException;
import org.meveo.admin.util.ImageUploadEventHandler;
import org.meveo.admin.util.ResourceBundle;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.api.ApiService;
import org.meveo.api.ApiUtils;
import org.meveo.api.ApiVersionedService;
import org.meveo.api.BaseCrudApi;
import org.meveo.api.CustomFieldTemplateApi;
import org.meveo.api.EntityCustomActionApi;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.technicalservice.endpoint.EndpointApi;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.elresolver.ELException;
import org.meveo.model.BusinessEntity;
import org.meveo.model.IEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.VersionedEntity;
import org.meveo.model.catalog.IImageUpload;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.Provider;
import org.meveo.model.crm.custom.EntityCustomAction;
import org.meveo.model.filter.Filter;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.storage.Repository;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.admin.impl.PermissionService;
import org.meveo.service.api.EntityHelperBean;
import org.meveo.service.base.MeveoExceptionMapper;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.filter.FilterService;
import org.meveo.service.storage.RepositoryService;
import org.meveo.service.technicalservice.endpoint.EndpointService;
import org.meveo.util.ApplicationProvider;
import org.meveo.util.EntityCustomizationUtils;
import org.meveo.util.view.MessagesHelper;
import org.meveo.util.view.PagePermission;
import org.meveo.util.view.ServiceBasedLazyDataModel;
import org.omnifaces.cdi.Param;
import org.primefaces.PrimeFaces;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.data.PageEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.lapis.jsfexporter.csv.CSVExportOptions;

/**
 * Base bean class. Other backing beans extends this class if they need functionality it provides.
 *
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @author Wassim Drira
 * @version 6.9.0
 */
@Named
//@ViewScoped
public abstract class BaseBean<T extends IEntity> implements Serializable {

    private static final long serialVersionUID = 1L;

    /** Logger. */
    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Inject
    protected ResourceBundle resourceBundle;

    @Inject
    protected Messages messages;

    @Inject
    @CurrentUser
    protected MeveoUser currentUser;

    @Inject
    @ApplicationProvider
    protected Provider appProvider;

    @Inject
    protected Conversation conversation;

    @Inject
    protected PermissionService permissionService;

    @Inject
    private FilterService filterService;

    @Inject
    private FilterCustomFieldSearchBean filterCustomFieldSearchBean;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private EndpointService endpointService;

    @Inject
    private CustomFieldTemplateApi customFieldTemplateApi;

    @Inject
    private EntityCustomActionApi entityCustomActionApi;

    @Inject
    private EndpointApi endpointApi;
    
    @Inject
    protected RepositoryService repositoryService;

    /** Search filters. */
    protected Map<String, Object> filters = new HashMap<String, Object>();

    /** Entity to edit/view. */
    protected T entity;

    /** Class of backing bean. */
    private Class<T> clazz;

    protected boolean hasParams = false;

    /**
     * Request parameter. Should form be displayed in create/edit or view mode
     */
    @Inject
    @Param
    private String edit;

    // private boolean editSaved;

    protected int dataTableFirstAttribute;

    @Inject
    private MeveoModuleService meveoModuleService;
    
    @Inject
    private EntityHelperBean entityHelper;
    
    private String partOfModules;

    /**
     * Request parameter. A custom back view page instead of a regular list page
     */
    @Inject
    @Param
    private String backView;

    private String backViewSave;

    private MeveoModule meveoModule;

    private MeveoModule selectedMeveoModule;

    /**
     * Object identifier to load
     */
    private Long objectId;

    /**
     * Datamodel for lazy dataloading in datatable.
     */
    protected LazyDataModel<T> dataModel;

    /**
     * Selected Entities in multiselect datatable.
     */
    private List<T> selectedEntities = new ArrayList<>();

    private Filter listFilter;

    protected boolean listFiltered = false;

    /**
     * Tracks active tabs in GUI
     */
    private int activeTab;

    private int activeMainTab = 0;

    private Map<String, Boolean> writeAccessMap;

    @Inject
    protected ParamBeanFactory paramBeanFactory;
    // protected String providerFilePath = paramBean.getCet("providers.rootDir", "./meveodata/");

    private UploadedFile uploadedFile;

    @Resource(lookup = "java:jboss/infinispan/cache/meveo/meveo-rows-page-cache")
    private Cache<String, Map<String, Integer>> cacheNumberRow;

    private BaseCrudApi<T,?> baseCrudApi;

    private boolean override;

    /**
     * Constructor
     */
    public BaseBean() {
        super();
    }

    /**
     * Constructor.
     *
     * @param clazz Class.
     */
    public BaseBean(Class<T> clazz) {
        super();
        this.clazz = clazz;
    }

    @PostConstruct
    public void init() {
    	baseCrudApi = getBaseCrudApi();
    }

    public boolean isOverride() {
		return override;
	}

	public void setOverride(boolean override) {
		this.override = override;
	}

	/**
     * Returns entity class
     *
     * @return Class
     */
    public Class<T> getClazz() {
        return clazz;
    }

    public void setClazz(Class<T> clazz) {
        this.clazz = clazz;
    }

    protected void beginConversation() {
        if (conversation.isTransient()) {
            conversation.begin();
        }
    }

    protected void endConversation() {
        if (!conversation.isTransient()) {
            conversation.end();
        }
    }

    public BaseCrudApi<T, ?> getBaseCrudApi() {
    	return null;
    }

    public void preRenderView() {
        beginConversation();
    }

    /**
     * Initiates entity from request parameter id.
     *
     * @return Entity from database.
     */
    public T initEntity() {
        log.debug("instantiating {} with id {}", this.getClass(), getObjectId());
        if (getObjectId() != null) {

            List<String> formFieldsToFetch = getFormFieldsToFetch();

            if (formFieldsToFetch == null) {
                entity = (T) getPersistenceService().findById(getObjectId());
            } else {
                entity = (T) getPersistenceService().findById(getObjectId(), formFieldsToFetch);
            }

            loadPartOfModules();

            // getPersistenceService().detach(entity);
        } else {
            try {
                entity = getInstance();

                // FIXME: If entity is Auditable, set here the creator and
                // creation time
            } catch (InstantiationException e) {
                log.error("Unexpected error!", e);
                throw new IllegalStateException("could not instantiate a class, abstract class");
            } catch (IllegalAccessException e) {
                log.error("Unexpected error!", e);
                throw new IllegalStateException("could not instantiate a class, constructor not accessible");
            }
        }

        return entity;
    }

    public T initEntity(Long id) {
        entity = null;
        setObjectId(id);
        return initEntity();
    }

    /**
     * Clear object parameters and instantiate a new entity
     *
     * @return Entity instantiated
     */
    public T newEntity() {
        log.debug("instantiating {} with id {}", this.getClass(), getObjectId());
        entity = null;
        setObjectId(null);
        return initEntity();
    }

    private boolean isPartOfModules() {
        return clazz.isAnnotationPresent(ModuleItem.class);
    }

    protected boolean isImageUpload() {
        return IImageUpload.class.isAssignableFrom(clazz);
    }

    private void loadPartOfModules() {
        if ((entity instanceof BusinessEntity) && isPartOfModules()) {
            BusinessEntity businessEntity = (BusinessEntity) entity;
            String appliesTo = null;
            if (ReflectionUtils.hasField(entity, "appliesTo")) {
                try {
                    appliesTo = (String) FieldUtils.readField(entity, "appliesTo", true);
                } catch (IllegalAccessException e) {
                    log.error("Failed to access 'appliesTo' field value", e);
                }
            }

            partOfModules = meveoModuleService.getRelatedModulesAsString(businessEntity.getCode(), clazz.getName(), appliesTo);
        }
    }

    /**
     * When opened to view or edit entity - this getter method returns it. In case entity is not loaded it will initialize it.
     *
     * @return Entity in current view state.
     */
    public T getEntity() {
        return entity != null ? entity : initEntity();
    }

    public void setEntity(T entity) {
        this.entity = entity;
    }

    @ActionMethod
    public String saveOrUpdate(boolean killConversation, String objectName, Long objectId) throws BusinessException, ELException {
        String outcome = saveOrUpdate(killConversation);

        if (killConversation) {
            endConversation();
        }

        return outcome;
    }

    @ActionMethod
    public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {

        String message = entity.isTransient() ? "save.successful" : "update.successful";
        
        try {
            entity = saveOrUpdate(entity);
            messages.info(new BundleKey("messages", message));
            if (killConversation) {
                endConversation();
            }
            
        } catch (Exception e) {
        	log.error("Can't create / update entity", e);
        	return MessagesHelper.error(messages, e);
        }

        return back();
    }

    @ActionMethod
    public String saveOrUpdateWithMessage(boolean killConversation) throws BusinessException {
        boolean result = true;
        try {
            return this.saveOrUpdate(killConversation);
        } catch (Exception e) {
            result = false;
        }

        PrimeFaces.current().ajax().addCallbackParam("result", result);
        return null;
    }

    /**
     * Save method when used in popup - no return value. Sets validation to failed if saveOrUpdate method called does not return a value.
     *
     * @throws BusinessException business exception
     */
    @ActionMethod
    public void saveOrUpdateForPopup() throws BusinessException, ELException {
        String result = saveOrUpdate(false);
        if (result == null) {
            FacesContext.getCurrentInstance().validationFailed();
        }
        return;
    }

    /**
     * Save or update entity depending on if entity is transient.
     *
     * @param entity Entity to save.
     * @throws BusinessException
     */
    protected T saveOrUpdate(T entity) throws BusinessException {
        IPersistenceService<T> persistenceService = getPersistenceService();
		if (entity.isTransient()) {
            persistenceService.create(entity);

        } else {
            entity = persistenceService.update(entity);
        }

        objectId = (Long) entity.getId();

        return entity;
    }

    /**
     * Lists all entities, sorted by description if bean is related to BusinessEntity type
     */
    public List<T> listAll() {
        if (clazz != null && BusinessEntity.class.isAssignableFrom(clazz)) {
            return getPersistenceService().list(new PaginationConfiguration("description", SortOrder.ASCENDING));
        } else {
            return getPersistenceService().list();
        }
    }

    /**
     * Returns view after save() operation. By default it goes back to list view. Override if need different logic (for example return to one view for save and another for update
     * operations)
     */
    public String getViewAfterSave() {
        return getListViewName();
    }

    /**
     * Method to get Back link. If default view name is different than override the method. Default name: entity's name + s;
     *
     * @return string for navigation
     */
    public String back() {
        if (backViewSave == null && backView != null) {
            // log.debug("backview parameter is " + backView.get());
            backViewSave = backView;
        } else if (backViewSave == null) {
            return getListViewName();
        }
        return backViewSave;
    }
    
    protected void addToModule(T entity, MeveoModule module) throws BusinessException {
        BusinessEntity businessEntity = (BusinessEntity) entity;
        MeveoModuleItem item = new MeveoModuleItem(businessEntity);
        if (!module.getModuleItems().contains(item)) {
            try {
				meveoModuleService.addModuleItem(item, module);
			} catch (BusinessException e2) {
				throw new BusinessException("Entity cannot be add or remove from the module", e2);
			}
            try {
                if (!org.meveo.commons.utils.StringUtils.isBlank(module.getModuleSource())) {
                    module.setModuleSource(JacksonUtil.toString(updateModuleItemDto(module)));
                }
                meveoModuleService.update(module);
                messages.info(businessEntity.getCode() + " added to module " + module.getCode());
            } catch (Exception e) {
                messages.error(businessEntity.getCode() + " not added to module " + module.getCode(), e);
            }
        } else {
            messages.error(new BundleKey("messages", "meveoModule.error.moduleItemExisted"), businessEntity.getCode(), module.getCode());
            return;
        }
    }

    public void addToModule() throws BusinessException  {
        MeveoModule module = meveoModuleService.findById(selectedMeveoModule.getId(), Arrays.asList("moduleItems", "patches", "releases", "moduleDependencies", "moduleFiles"));
        if (entity != null && !selectedMeveoModule.equals(entity)) {
        	try {
				addToModule(entity, module);
			} catch (BusinessException e) {
				throw new BusinessException("Entity cannot be add or remove from the module", e);
			}
        }
    }

    public void addManyToModule() throws BusinessException  {
        if (selectedEntities == null || selectedEntities.isEmpty()) {
            return;
        }

        MeveoModule module = meveoModuleService.findById(selectedMeveoModule.getId(), Arrays.asList("moduleItems", "patches", "releases", "moduleDependencies", "moduleFiles"));
        for (T entity : selectedEntities) {
            if (entity != null && !selectedMeveoModule.equals(entity)) {
            	try {
					addToModule(entity, module);
				} catch (BusinessException e2) {
					throw new BusinessException("Entity cannot be add or remove from the module", e2);
				}
            }
        }
    }

    public MeveoModuleDto updateModuleItemDto(MeveoModule meveoModule) {

        Set<MeveoModuleItem> moduleItems = meveoModule.getModuleItems();

        MeveoModuleDto moduleDto = JacksonUtil.fromString(meveoModule.getModuleSource(), MeveoModuleDto.class);

        if (!moduleDto.getModuleItems().isEmpty()) {
            moduleDto.getModuleItems().clear();
        }

        if (moduleItems != null) {
            for (MeveoModuleItem item : moduleItems) {

                try {
                    BaseEntityDto itemDto = null;

                    if (item.getItemClass().equals(CustomFieldTemplate.class.getName())) {
                        // we will only add a cft if it's not a field of a cet
                        if (!org.meveo.commons.utils.StringUtils.isBlank(item.getAppliesTo())) {
                            String cetCode = EntityCustomizationUtils.getEntityCode(item.getAppliesTo());
                            if (customEntityTemplateService.findByCode(cetCode) == null) {
                                itemDto = customFieldTemplateApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());
                            }

                        } else {
                            itemDto = customFieldTemplateApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());
                        }

                    } else if (item.getItemClass().equals(EntityCustomAction.class.getName())) {
                        itemDto = entityCustomActionApi.findIgnoreNotFound(item.getItemCode(), item.getAppliesTo());

                    } else {
                        try {
                            Class clazz = Class.forName(item.getItemClass());
                            if (clazz.getSimpleName().startsWith("Endpoint")) {
                                Endpoint endpoint = endpointService.findByCode(item.getItemCode(), Arrays.asList("service", "pathParameters", "parametersMapping"));
                                itemDto = endpointApi.toDto(endpoint);
                            } else if (clazz.isAnnotationPresent(VersionedEntity.class)) {
                                ApiVersionedService apiService = ApiUtils.getApiVersionedService(item.getItemClass(), true);
                                itemDto = apiService.findIgnoreNotFound(
                                        item.getItemCode(),
                                        item.getValidity() != null ? item.getValidity().getFrom() : null,
                                        item.getValidity() != null ? item.getValidity().getTo() : null
                                );
                            } else {
                                ApiService apiService = ApiUtils.getApiService(clazz, true);
                                itemDto = apiService.findIgnoreNotFound(item.getItemCode());
                            }
                        } catch (org.meveo.exceptions.EntityDoesNotExistsException e) {
                            log.error(clazz.getSimpleName() + " with code=" + item.getItemCode() + " does not exists.");
                        }
                    }
                    if (itemDto != null) {
                        moduleDto.addModuleItem(itemDto);
                    } else {
                        log.warn("Failed to find a module item or not added in case of CFT that is a field of CET {}", item);
                    }

                } catch (ClassNotFoundException e) {
                    log.error("Failed to find a class", e);

                } catch (MeveoApiException e) {
                    log.error("Failed to transform module item to DTO. Module item {}", item, e);
                }
            }
        }

        return moduleDto;
    }

    /**
     * Go back and end conversation. BeforeRedirect flag is set to true, so conversation is first ended and then redirect is proceeded, that means that after redirect new
     * conversation will have to be created (temp or long running) so that view will have all most up to date info because it will load everything from db when starting new
     * conversation.
     *
     * @return string for navigation
     */
    public String backAndEndConversation() {
        String outcome = back();
        endConversation();
        return outcome;
    }

    /**
     * Generating action name to get to entity creation page. Override this method if its view name does not fit.
     */
    public String getNewViewName() {
        return getEditViewName();
    }

    /**
     * Get navigation view link name for a current entity class
     */
    public String getEditViewName() {
        return BaseBean.getEditViewName(clazz);
    }

    /**
     * Convert entity class to a detail view name
     *
     * @param clazz Entity class
     * @return Navigation view link name
     */
    @SuppressWarnings("rawtypes")
    public static String getEditViewName(Class clazz) {
        String className = ReflectionUtils.getCleanClassName(clazz.getSimpleName());
        StringBuilder sb = new StringBuilder(className);
        sb.append("Detail");
        char[] dst = new char[1];
        sb.getChars(0, 1, dst, 0);
        sb.replace(0, 1, new String(dst).toLowerCase());
        return sb.toString();
    }

    /**
     * Generating back link.
     */
    protected String getListViewName() {
        String className = clazz.getSimpleName();
        StringBuilder sb = new StringBuilder(className);
        char[] dst = new char[1];
        sb.getChars(0, 1, dst, 0);
        sb.replace(0, 1, new String(dst).toLowerCase());
        sb.append("s");
        return sb.toString();
    }

    public String getIdParameterName() {
        String className = clazz.getSimpleName();
        StringBuilder sb = new StringBuilder(className);
        sb.append("Id");
        char[] dst = new char[1];
        sb.getChars(0, 1, dst, 0);
        sb.replace(0, 1, new String(dst).toLowerCase());
        return sb.toString();
    }

    /**
     * Delete Entity using it's ID. Add error message to status message if unsuccessful.
     *
     * @param id Entity id to delete
     * @throws BusinessException business exception
     */
    @ActionMethod
    public void delete(Long id) throws BusinessException {

        deleteInternal(id, null, true);
    }

    /**
     * Delete Entity using it's ID. Add error message to status messages if unsuccessful.
     *
     * @param id Entity id to delete
     * @param code Entity's code - just for display in error messages
     * @param setOkMessages Shall success messages be set for display
     * @throws BusinessException business exception
     */
    private boolean deleteInternal(Long id, String code, boolean setOkMessages) throws BusinessException {
        try {
            log.info("Deleting entity {} with id = {}", clazz.getName(), id);
            getPersistenceService().remove(id);

            if (setOkMessages) {
                messages.info(new BundleKey("messages", "delete.successful"));
            }

            return true;

        } catch (Exception e) {
        	BusinessException be = MeveoExceptionMapper.translatePersistenceException(e, clazz.getName(), String.valueOf(id));
        	if (be!= null) {
        		if (be instanceof ConstraintViolationException ) {
                    String referencedBy = null;
                    
                    try { 
                    	referencedBy = entityHelper.findReferencedByEntities(clazz, id);
                    	log.info("Delete was unsuccessful because entity is used by other entities {}", referencedBy);
                    } catch (Exception ex) {
                    	log.error("Can't find related entities", ex);
                    }
                    
                    if (referencedBy != null) {
                        messages.error(new BundleKey("messages", "error.delete.entityUsedWDetails"), code == null ? "" : code, referencedBy);
                    } else {
                        messages.error(new BundleKey("messages", "error.delete.entityUsed"));
                    }
                    
                    FacesContext.getCurrentInstance().validationFailed();
                    return false;
                }
                
                throw be;
            }

            throw e;
        }
    }

    @ActionMethod
    public void delete() throws BusinessException {
        delete((Long) getEntity().getId());
    }

    /**
     * Delete checked entities. Add error message to status messages if unsuccessful.
     *
     * @throws Exception general exception
     */
    @ActionMethod
    @TransactionAttribute(TransactionAttributeType.REQUIRED)
    public void deleteMany() throws Exception {

        if (selectedEntities == null || selectedEntities.isEmpty()) {
            messages.info(new BundleKey("messages", "delete.entitities.noSelection"));
            return;
        }

        boolean allOk = true;
        for (IEntity entity : selectedEntities) {
            allOk = deleteInternal((Long) entity.getId(), entity instanceof BusinessEntity ? ((BusinessEntity) entity).getCode() : "", false) && allOk;
        }

        if (allOk) {
            messages.info(new BundleKey("messages", "delete.entitities.successful"));
        }
    }

    /**
     * Delete current entity from detail page and redirect to a previous page. Used mostly for deletion in detail pages.
     *
     * @return back() page if deleted success, if not, return a callback result to UI for validate
     * @throws BusinessException
     */
    @ActionMethod
    public String deleteWithBack() throws BusinessException {

        if (this.deleteInternal((Long) getEntity().getId(), null, true)) {
            return back();
        }
        return null;
    }

    /**
     * Gets search filters map.
     *
     * @return Filters map.
     */
    public Map<String, Object> getFilters() {
        if (filters == null) {
            filters = new HashMap<>();
        }
        return filters;
    }

    /**
     * Clean search fields in datatable.
     */
    public void clean() {
        dataModel = null;
        filters = new HashMap<>();
        listFilter = null;
    }

    /**
     * Reset values to the last state.
     */
    public void resetFormEntity() {
        entity = null;
        entity = getEntity();
    }

    /**
     * Get new instance for backing bean class.
     *
     * @return New instance.
     *
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    public T getInstance() throws InstantiationException, IllegalAccessException {

        T newInstance = clazz.newInstance();

        return newInstance;
    }

    /**
     * Method that returns concrete PersistenceService. That service is then used for operations on concrete entities (eg. save, delete etc).
     *
     * @return Persistence service
     */
    protected abstract IPersistenceService<T> getPersistenceService();

    /**
     * Override this method if you need to fetch any fields when selecting list of entities in data table. Return list of field names that has to be fetched.
     */
    protected List<String> getListFieldsToFetch() {
        return null;
    }

    /**
     * Override this method if you need to fetch any fields when selecting one entity to show it a form. Return list of field names that has to be fetched.
     */
    protected List<String> getFormFieldsToFetch() {
        return null;
    }

    /**
     * Override this method when pop up with additional entity information is needed.
     */
    protected String getPopupInfo() {
        return "No popup information. Override BaseBean.getPopupInfo() method.";
    }

    /**
     * Disable current entity. Add error message to status messages if unsuccessful.
     *
     */
    @ActionMethod
    public void disable() {
        try {
            log.info("Disabling entity {} with id = {}", clazz.getName(), entity.getId());
            entity = getPersistenceService().disable((Long) entity.getId());
            messages.info(new BundleKey("messages", "disabled.successful"));

        } catch (Exception t) {
            log.info("unexpected exception when disabling!", t);
            messages.error(new BundleKey("messages", "error.unexpected"));
        }
    }

    /**
     * Disable Entity using it's ID. Add error message to status messages if unsuccessful.
     *
     * @param id Entity id to disable
     */
    @ActionMethod
    public void disable(Long id) {
        try {
            log.info("Disabling entity {} with id = {}", clazz.getName(), id);
            getPersistenceService().disable(id);
            messages.info(new BundleKey("messages", "disabled.successful"));

        } catch (Throwable t) {
            log.info("unexpected exception when disabling!", t);
            messages.error(new BundleKey("messages", "error.unexpected"));
        }
    }

    /**
     * Enable current entity. Add error message to status messages if unsuccessful.
     *
     */
    @ActionMethod
    public void enable() {
        try {
            log.info("Enabling entity {} with id = {}", clazz.getName(), entity.getId());
            entity = getPersistenceService().enable((Long) entity.getId());
            messages.info(new BundleKey("messages", "enabled.successful"));

        } catch (Exception t) {
            log.info("unexpected exception when enabling!", t);
            messages.error(new BundleKey("messages", "error.unexpected"));
        }
    }

    /**
     * Enable Entity using it's ID. Add error message to status messages if unsuccessful.
     *
     * @param id Entity id to enable
     */
    @ActionMethod
    public void enable(Long id) {
        try {
            log.info("Enabling entity {} with id = {}", clazz.getName(), id);
            getPersistenceService().enable(id);
            messages.info(new BundleKey("messages", "enabled.successful"));

        } catch (Throwable t) {
            log.info("unexpected exception when enabling!", t);
            messages.error(new BundleKey("messages", "error.unexpected"));
        }
    }

    /**
     * DataModel for primefaces lazy loading datatable component.
     *
     * @return LazyDataModel implementation.
     */
    public LazyDataModel<T> getLazyDataModel() {
        return getLazyDataModel(filters, listFiltered);
    }

    public LazyDataModel<T> getLazyDataModel(Map<String, Object> inputFilters, boolean forceReload) {
        if (dataModel == null || forceReload) {

            final Map<String, Object> filters = inputFilters;

            dataModel = new ServiceBasedLazyDataModel<T>() {

                private static final long serialVersionUID = 1736191234466041033L;

                @Override
                protected IPersistenceService<T> getPersistenceServiceImpl() {

                    return getPersistenceService();
                }

                @Override
                protected Map<String, Object> getSearchCriteria() {

                    // Omit empty or null values
                    Map<String, Object> cleanFilters = new HashMap<String, Object>();

                    for (Entry<String, Object> filterEntry : filters.entrySet()) {
                        if (filterEntry.getValue() == null) {
                            continue;
                        }
                        if (filterEntry.getValue() instanceof String) {
                            if (StringUtils.isBlank((String) filterEntry.getValue())) {
                                continue;
                            }
                        }
                        cleanFilters.put(filterEntry.getKey(), filterEntry.getValue());
                    }

                    return BaseBean.this.supplementSearchCriteria(cleanFilters);
                }

                @Override
                protected String getDefaultSortImpl() {
                    return getDefaultSort();
                }

                @Override
                protected SortOrder getDefaultSortOrderImpl() {
                    return getDefaultSortOrder();
                }

                @Override
                protected List<String> getListFieldsToFetchImpl() {
                    return getListFieldsToFetch();
                }

            };
        }

        listFiltered = false;

        return dataModel;
    }

    /**
     * Allows to overwrite, or add additional search criteria for filtering a list. Search criteria is a map with filter criteria name as a key and value as a value. Criteria name
     * consist of [&lt;condition&gt;]&lt;field name&gt; (e.g. "like firstName") where &lt;condition&gt; is a condition to apply to field value comparison and &lt;name&gt; is an
     * entity attribute name.
     *
     * @param searchCriteria Search criteria - should be same as filters attribute
     * @return HashMap with filter criteria name as a key and value as a value
     */
    protected Map<String, Object> supplementSearchCriteria(Map<String, Object> searchCriteria) {
        return searchCriteria;
    }

    public void search() {
        filterCustomFieldSearchBean.buildFilterParameters(filters);
    }

    public List<T> getSelectedEntities() {
        return selectedEntities;
    }

    public void setSelectedEntities(List<T> selectedEntities) {
        this.selectedEntities = selectedEntities;
    }

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    /**
     * true in edit mode
     *
     * @return
     */
    public boolean isEdit() {
        if (edit == null || org.meveo.commons.utils.StringUtils.isBlank(edit)) {
            return true;
        }

        return Boolean.valueOf(edit);
    }
    
    public void setEdit(String value) {
		this.edit = value;
    }
    
    public void setEdit(boolean value) {
    	this.edit = String.valueOf(value);
    }

    protected void clearObjectId() {
        objectId = null;
    }

    public String getProviderLanguageCode() {
        if (appProvider.getLanguage() != null) {
            return appProvider.getLanguage().getCode();
        }
        return "";
    }

    protected String getDefaultSort() {
        if (listFilter != null && listFilter.getOrderCondition() != null) {
            StringBuffer sb = new StringBuffer();
            for (String field : listFilter.getOrderCondition().getFieldNames()) {
                if (field.indexOf(".") == -1) {
                    sb.append(listFilter.getPrimarySelector().getAlias() + "." + field + ",");
                } else {
                    sb.append(field + ",");
                }
            }
            sb.deleteCharAt(sb.length() - 1);

            return StringUtils.join(listFilter.getOrderCondition().getFieldNames(), ",");
        }

        return "id";
    }

    protected SortOrder getDefaultSortOrder() {
        if (listFilter != null && listFilter.getOrderCondition() != null) {
            if (listFilter.getOrderCondition().isAscending()) {
                return SortOrder.ASCENDING;
            }
        }

        return SortOrder.DESCENDING;
    }

    public String getBackView() {
        return backView;
    }

    public String getBackViewSave() {
        return backViewSave;
    }

    public void setBackViewSave(String backViewSave) {
        this.backViewSave = backViewSave;
    }

    public int getDataTableFirstAttribute() {
        return dataTableFirstAttribute;
    }

    public void setDataTableFirstAttribute(int dataTableFirstAttribute) {
        this.dataTableFirstAttribute = dataTableFirstAttribute;
    }

    /**
     * Change page
     * @param event
     */
    public void onPageChange(PageEvent event) {
        this.setDataTableFirstAttribute(((DataTable) event.getSource()).getFirst());
    }

    /**
     * Get currently active locale
     *
     * @return Currently active locale
     */
    public Locale getCurrentLocale() {
        return FacesContext.getCurrentInstance().getViewRoot().getLocale();
    }

    public CSVExportOptions csvOptions() {
        ParamBean param = paramBeanFactory.getInstance();
        String characterEncoding = param.getProperty("csv.characterEncoding", "iso-8859-1");
        CSVExportOptions csvOption = new CSVExportOptions();
        csvOption.setSeparatorCharacter(';');
        csvOption.setCharacterEncoding(characterEncoding);
        return csvOption;
    }

    // dummy codes for avoiding to get custom field templates
    public List<CustomFieldTemplate> getCustomFieldTemplates() {
        return null;
    }

    public Filter getListFilter() {
        return listFilter;
    }

    public void setListFilter(Filter listFilter) {
        this.listFilter = listFilter;
    }

    public List<Filter> getListFilters() {
        if (clazz != null) {
            return filterService.findByPrimaryTargetClass(clazz.getName());
        } else {
            return null;
        }
    }

    public void runListFilter() {
        if (listFilter != null) {
            dataModel = null;
            filters = new HashMap<>();
            filters.put("$FILTER", listFilter);
            listFiltered = true;
        } else {
            filters.remove("$FILTER");
        }
    }

    public boolean isListFiltered() {
        return listFiltered;
    }

    public int getActiveTab() {
        return activeTab;
    }

    public void setActiveTab(int activeTab) {
        this.activeTab = activeTab;
    }

    /**
     * @param activeMainTab Main tab to select
     */
    public void setActiveMainTab(int activeMainTab) {
        this.activeMainTab = activeMainTab;
    }

    /**
     * @return the activeMainTab
     */
    public int getActiveMainTab() {
        return activeMainTab;
    }

    /**
     * Get custom actions applicable to the entity - right now implemented in customFieldEntityBean only. Here provided for GUI compatibility issue only
     *
     * @return A list of entity action scripts
     */
    public List<EntityCustomAction> getCustomActions() {
        return null;
    }

    /**
     * Delete item from a collection of values
     *
     * @param values Collection of values
     * @param itemIndex An index of an item to remove
     */
    @SuppressWarnings("rawtypes")
    public void deleteItemFromCollection(Collection values, int itemIndex) {

        int index = 0;
        Iterator iterator = values.iterator();
        while (iterator.hasNext()) {
            iterator.next();
            if (itemIndex == index) {
                iterator.remove();
                return;
            }
            index++;
        }
    }

    /**
     * Change value in a collection. Collection to update an item index are passed as attributes
     *
     * @param event Value change event
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void updateItemInCollection(ValueChangeEvent event) {

        Collection values = (Collection) event.getComponent().getAttributes().get("values");

        values.remove(event.getOldValue());
        values.add(event.getNewValue());

        // Unpredictable results when changing several values at a time, as Set does not guarantee same value order - could be used only in Ajax and only with refresh
        // int itemIndex = (int) event.getComponent().getAttributes().get("itemIndex");
        // log.error("AKK changing value from {} to {} in index {} values {}", event.getOldValue(), event.getNewValue(), itemIndex, values.toArray());
        // ArrayList newValues = new ArrayList();
        // newValues.addAll(values);
        //
        // newValues.remove(itemIndex);
        // newValues.add(itemIndex, event.getNewValue());
        // values.clear();
        // values.addAll(newValues);
        // log.error("AKK end changing value from {} to {} in index {} values {}", event.getOldValue(), event.getNewValue(), itemIndex, values.toArray());
    }

    /**
     * Add a new blank item to collection. Instantiate a new item based on parametized collection type.
     *
     * @param values A collection of values
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void addItemToCollection(Collection values, Class itemClass) {

        try {
            values.add(itemClass.newInstance());
        } catch (InstantiationException | IllegalAccessException e) {
            log.error("Failed to instantiate a new item of {} class", itemClass.getName());
        }
    }

    public List<T> listActive() {
        Map<String, Object> filters = getFilters();
        filters.put("disabled", false);
        PaginationConfiguration config = new PaginationConfiguration(filters);

        return getPersistenceService().list(config);
    }

    /**
     * crm/customers
     *
     *
     */
    public boolean canUserUpdateEntity() {
        if (this.writeAccessMap == null) {
            writeAccessMap = Collections.synchronizedMap(new HashMap<String, Boolean>());
        }
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest request = (HttpServletRequest) context.getRequest();
        String requestURI = request.getRequestURI();

        if (writeAccessMap.get(requestURI) == null) {
            boolean hasWriteAccess = false;
            try {
                hasWriteAccess = PagePermission.getInstance().hasWriteAccess(request, currentUser);
            } catch (BusinessException e) {
                log.error("Error encountered checking for write access to {}", requestURI, e);
                hasWriteAccess = false;
            }
            writeAccessMap.put(requestURI, hasWriteAccess);
        }
        return writeAccessMap.get(requestURI);
    }

    public String getPartOfModules() {
        return partOfModules;
    }

    public void setPartOfModules(String partOfModules) {
        this.partOfModules = partOfModules;
    }

    public String getDescriptionOrCode() {
        if (entity instanceof BusinessEntity) {
            BusinessEntity be = (BusinessEntity) entity;
            if (org.meveo.commons.utils.StringUtils.isBlank(be.getDescription())) {
                return be.getCode();
            } else {
                return be.getDescription();
            }
        }

        return null;
    }

    public void hfHandleFileUpload(FileUploadEvent event) throws BusinessException {
        uploadedFile = event.getFile();

        try {
            ImageUploadEventHandler<T> uploadHandler = new ImageUploadEventHandler<T>(currentUser.getProviderCode());
            String filename = uploadHandler.handleImageUpload(entity, uploadedFile);
            if (filename != null) {
                ((IImageUpload) entity).setImagePath(filename);
                messages.info(new BundleKey("messages", "message.upload.succesful"));
            }
        } catch (Exception e) {
            messages.error(new BundleKey("messages", "message.upload.fail"), e.getMessage());
        }
    }

    public UploadedFile getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(UploadedFile uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    public MeveoModule getMeveoModule() {
        return meveoModule;
    }

    public void setMeveoModule(MeveoModule meveoModule) {
        this.meveoModule = meveoModule;
    }

    public MeveoModule getSelectedMeveoModule() {
        return selectedMeveoModule;
    }

    public void setSelectedMeveoModule(MeveoModule selectedMeveoModule) {
        this.selectedMeveoModule = selectedMeveoModule;
    }

    /**
     * Get rows per page from meveo-rows-page-cache cache
     *
     */
    public int getCacheNumRows() {
        String username = currentUser.getUserName();

        if(clazz != null) {
	        String clazzName = clazz.getName();
	        Map<String, Integer> numberRow = cacheNumberRow.get(username);
	        if (numberRow != null && numberRow.get(clazzName) != null) {
	            return numberRow.get(clazzName);
	        }
        }
        
        return 10;
    }

    /**
     * Set rows per page for given user and entity class
     */
    public void setCacheNumRows(int rows) {
        String username = currentUser.getUserName();

        String clazzName = clazz.getName();
        Map<String, Integer> rowsByClassForUser = cacheNumberRow.get(username);

        if(rowsByClassForUser == null) {
        	rowsByClassForUser = new HashMap<>();
        }

        rowsByClassForUser.put(clazzName, rows);

        cacheNumberRow.put(username, rowsByClassForUser);
    }

    public boolean isHasParams() {
        return hasParams;
    }
    
	public List<Repository> listRepositories() {
		List<Repository> result = repositoryService.list();
		return result;
	}
}