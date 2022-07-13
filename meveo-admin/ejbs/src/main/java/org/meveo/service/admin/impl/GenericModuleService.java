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
package org.meveo.service.admin.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;

import org.hibernate.LockOptions;
import org.hibernate.NaturalIdLoadAccess;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.ImageUploadEventHandler;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.commons.utils.EjbUtils;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.BaseEntity;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModuleItemOrder;
import org.meveo.model.ObservableEntity;
import org.meveo.model.catalog.IImageUpload;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleDependency;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.api.EntityToDtoConverter;
import org.meveo.service.base.BusinessService;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.script.module.ModuleScriptInterface;
import org.meveo.service.script.module.ModuleScriptService;
import org.meveo.service.storage.RepositoryService;

import com.github.javaparser.utils.Log;

@Stateless
public class GenericModuleService<T extends MeveoModule> extends BusinessService<T> {

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @EJB
    private CustomFieldInstanceService customFieldInstanceService;

    @Inject
    protected EntityToDtoConverter entityToDtoConverter;

    @Inject
    private ModuleScriptService moduleScriptService;

    @Inject
    private CrossStorageService crossStorageService;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;
    
    @Inject
    private RepositoryService repositoryService;
    
    @SuppressWarnings("rawtypes")
    public void loadModuleItem(MeveoModuleItem item) throws BusinessException {
    	
        BusinessEntity entity = null;
        if (item.getItemClass().startsWith("org.meveo.model.technicalservice.endpoint.Endpoint")) {
            item.setItemClass("org.meveo.model.technicalservice.endpoint.Endpoint");
        }
        if (CustomFieldTemplate.class.getName().equals(item.getItemClass()) && item.getAppliesTo() != null) {
            entity = customFieldTemplateService.findByCodeAndAppliesToNoCache(item.getItemCode(), item.getAppliesTo());
            if(entity != null && entity.getCode() == null) {
            	entity = null;
            }

        } else if (CustomEntityInstance.class.getName().equals(item.getItemClass()) && item.getAppliesTo() != null) {
            CustomEntityTemplate customEntityTemplate = customEntityTemplateService.findByCode(item.getAppliesTo());
            if (customEntityTemplate == null) {
            	return;
            }
            
            Map<String, Object> ceiTable;
            
        	try {
        		ceiTable = crossStorageService.find(
    				repositoryService.findDefaultRepository(), // XXX: Maybe we will need to parameterize this or search in all repositories ?
    				customEntityTemplate,
    				item.getItemCode(),
    				false	// XXX: Maybe it should also be a parameter
    			);
        	} catch (EntityDoesNotExistsException e) {
        		ceiTable = null;
        	}
            
            if (ceiTable != null) {
                CustomEntityInstance customEntityInstance = new CustomEntityInstance();
                customEntityInstance.setUuid((String) ceiTable.get("uuid"));
                customEntityInstance.setCode((String) ceiTable.get("uuid"));
                String fieldName = customFieldTemplateService.getFieldName(customEntityTemplate);
                if (fieldName != null) {
                    Object description = null;
                    Map<String, CustomFieldTemplate> customFieldTemplates = customFieldTemplateService.findByAppliesTo(customEntityTemplate.getAppliesTo());
                    for (CustomFieldTemplate customFieldTemplate : customFieldTemplates.values()) {
                        if (customFieldTemplate != null && customFieldTemplate.getCode().toLowerCase().equals(fieldName)) {
                            description = ceiTable.get(customFieldTemplate.getCode());
                        }
                    }
                    customEntityInstance.setDescription(fieldName + ": " + description);
                }
                customEntityInstance.setCetCode(item.getAppliesTo());
                customEntityInstance.setCet(customEntityTemplate);
                customFieldInstanceService.setCfValues(customEntityInstance, item.getAppliesTo(), ceiTable);
                entity = customEntityInstance;
            }
        } else {

            String sql = "select mi from " + item.getItemClass() + " mi where mi.code=:code ";

            Class itemClazz;
            try {
                itemClazz = Class.forName(item.getItemClass());
            } catch (ClassNotFoundException e1) {
                log.error("Failed to find a module item {}. Module item class {} unknown", item, item.getItemClass());
                return;
            }
            boolean addFromParam = false;
            boolean addToParam = false;
            if (ReflectionUtils.isClassHasField(itemClazz, "validity")) {
                if (item.getValidity() != null && item.getValidity().getFrom() != null) {
                    sql = sql + " and mi.validity.from = :from";
                    addFromParam = true;
                } else {
                    sql = sql + " and mi.validity.from IS NULL";
                }
                if (item.getValidity() != null && item.getValidity().getTo() != null) {
                    sql = sql + " and mi.validity.to = :to";
                    addToParam = true;
                } else {
                    sql = sql + " and mi.validity.to IS NULL";
                }
            }
            
            final TypedQuery<BusinessEntity> query;
            try {
            	query = getEntityManager().createQuery(sql, BusinessEntity.class);
            } catch(Exception e) {
            	log.error("Can't build the following query : {}", sql, e);
            	return;
            }
            
            query.setParameter("code", item.getItemCode());
            if (addFromParam) {
                query.setParameter("from", item.getValidity().getFrom());
            }
            if (addToParam) {
                query.setParameter("to", item.getValidity().getTo());
            }
            try {
                entity = query.getSingleResult();
            } catch (NoResultException e) {
            	return;
            } catch (NonUniqueResultException e) {
                return;
            } catch (Exception e) {
                log.error("Failed to find a module item {}", item, e);
                return;
            }
        }
        
        // getEntityManager().detach(entity);
        item.setItemEntity(entity);
    }
    
	/**
	 * @param item
	 * @param clazz
	 * @return
	 */
    public BusinessEntity getItemEntity(MeveoModuleItem item, Class<?> clazz) {
		NaturalIdLoadAccess<?> query = getEntityManager().
				unwrap(org.hibernate.Session.class)
				.byNaturalId(clazz)
				.with(LockOptions.READ)
				.using("code", item.getItemCode());
		
		if(item.getAppliesTo() != null) {
			query = query.using("appliesTo", item.getAppliesTo());
		}
		
		Object loadedItem = query.load();
		return (BusinessEntity) loadedItem;
	}
	
	public BusinessEntity getItemEntity(MeveoModuleItem item) {
		try {
			Class<?> clazz = Class.forName(item.getItemClass());
			return (BusinessEntity) getItemEntity(item, clazz);
		} catch (ClassNotFoundException e) {
			throw new IllegalArgumentException(e);
		}
	}

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public T disable(T module) throws BusinessException {

        // if module is local module (was not downloaded) just disable as any other entity without iterating module items
        if (!module.isDownloaded()) {
            return super.disable(module);
        }

        if (!module.isInstalled()) {
            // throw new BusinessException("Module is not installed");
            return module;
        }

        ModuleScriptInterface moduleScript = null;
        if (module.getScript() != null) {
            moduleScript = moduleScriptService.preDisableModule(module.getScript().getCode(), module);
        }

        for (MeveoModuleItem item : module.getModuleItems()) {
            loadModuleItem(item);
            BusinessEntity itemEntity = item.getItemEntity();
            if (itemEntity == null) {
                continue;
            }

            try {
                // Find API service class first trying with item's classname and then with its super class (a simplified version instead of trying various class
                // superclasses)
                Class clazz = Class.forName(item.getItemClass());
                PersistenceService persistenceServiceForItem = (PersistenceService) EjbUtils.getServiceInterface(clazz);
                if (persistenceServiceForItem == null) {
                    persistenceServiceForItem = (PersistenceService) EjbUtils.getServiceInterface(clazz.getSuperclass());
                }
                if (persistenceServiceForItem == null) {
                    log.error("Failed to find implementation of persistence service for class {}", item.getItemClass());
                    continue;
                }

                persistenceServiceForItem.disable(itemEntity);

            } catch (Exception e) {
                log.error("Failed to disable module item. Module item {}", item, e);
            }
        }

        if (moduleScript != null) {
            moduleScriptService.postDisableModule(moduleScript, module);
        }

        return super.disable(module);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public T enable(T module) throws BusinessException {

        // if module is local module (was not downloaded) just disable as any other entity without iterating module items
        if (!module.isDownloaded()) {
            return super.enable(module);
        }

        if (!module.isInstalled()) {
            // throw new BusinessException("Module is not installed");
            return module;
        }

        ModuleScriptInterface moduleScript = null;
        if (module.getScript() != null) {
            moduleScript = moduleScriptService.preEnableModule(module.getScript().getCode(), module);
        }

        for (MeveoModuleItem item : module.getModuleItems()) {
            loadModuleItem(item);
            BusinessEntity itemEntity = item.getItemEntity();
            if (itemEntity == null) {
                continue;
            }

            try {
                // Find API service class first trying with item's classname and then with its super class (a simplified version instead of trying various class
                // superclasses)
                Class clazz = Class.forName(item.getItemClass());
                PersistenceService persistenceServiceForItem = (PersistenceService) EjbUtils.getServiceInterface(clazz.getSimpleName() + "Service");
                if (persistenceServiceForItem == null) {
                    persistenceServiceForItem = (PersistenceService) EjbUtils.getServiceInterface(clazz.getSuperclass().getSimpleName() + "Service");
                }
                if (persistenceServiceForItem == null) {
                    log.error("Failed to find implementation of persistence service for class {}", item.getItemClass());
                    continue;
                }

                persistenceServiceForItem.enable(itemEntity);

            } catch (Exception e) {
                log.error("Failed to enable module item. Module item {}", item, e);
            }
        }

        if (moduleScript != null) {
            moduleScriptService.postEnableModule(moduleScript, module);
        }

        return super.enable(module);
    }
    
	public List<MeveoModuleItem> getSortedModuleItemsForUninstall(Set<MeveoModuleItem> moduleItems) {

		Comparator<MeveoModuleItem> comparator = new Comparator<MeveoModuleItem>() {

			@SuppressWarnings("rawtypes")
			@Override
			public int compare(MeveoModuleItem o1, MeveoModuleItem o2) {

				ModuleItemOrder sortOrder1 = null;
				ModuleItemOrder sortOrder2 = null;

				if (o1.getClass().equals(MeveoModuleItem.class)) {
					try {
						Class<?> itemClass = Class.forName(o1.getItemClass());
						sortOrder1 = itemClass.getAnnotation(ModuleItemOrder.class);
					} catch (ClassNotFoundException e) {
					}

				} else {
					sortOrder1 = o1.getClass().getAnnotation(ModuleItemOrder.class);
				}

				if (o2.getClass().equals(MeveoModuleItem.class)) {
					try {
						Class<?> itemClass = Class.forName(o2.getItemClass());
						sortOrder2 = itemClass.getAnnotation(ModuleItemOrder.class);
					} catch (ClassNotFoundException e) {
					}

				} else {
					sortOrder2 = o2.getClass().getAnnotation(ModuleItemOrder.class);
				}
				
				if (Objects.isNull(sortOrder1) || Objects.isNull(sortOrder2)) {
					return 0;
				}
				
				try {
					if (o1.getItemClass().equals(o2.getItemClass())) {
						loadModuleItem(o1);
						if (o1.getItemEntity() instanceof Comparable) {
							loadModuleItem(o2);
							return ((Comparable) o1.getItemEntity()).compareTo(o2.getItemEntity());
						}
					}
				} catch (BusinessException e) {
					log.error("Failed to compare objects", e);
				}

				return sortOrder1.value() - sortOrder2.value();
			}
		};

		List<MeveoModuleItem> sortedList = new ArrayList<>(moduleItems);
		sortedList.sort(comparator);
		Collections.reverse(sortedList);
		
		return sortedList;
	}
	
	public boolean isDependencyOfOtherModule(T module) throws BusinessException {
		
		String query = "SELECT m from \n"
				+ "MeveoModuleDependency m \n"
				+ "WHERE m.code=:moduleCode \n"
				+ "AND m.currentVersion=:currentVersion \n"
				+ "AND m.meveoModule.installed = true \n";
		// check if this module is a parent
		TypedQuery<MeveoModuleDependency> moduleDependencyResult = getEntityManager().createQuery(query, MeveoModuleDependency.class);
		moduleDependencyResult.setParameter("moduleCode", module.getCode()) //
				.setParameter("currentVersion", module.getCurrentVersion());
		try {
			return moduleDependencyResult.getSingleResult() != null;
			
		} catch (NoResultException e) {
			return false;
		}
	}

    @SuppressWarnings("unchecked")
    @Override
    public void remove(T module) throws BusinessException {

		if(isDependencyOfOtherModule(module)) {
			throw new BusinessException("Unable to delete a referenced module.");
		}
    	
        // If module was downloaded, remove all submodules as well
        if (module.isDownloaded() && module.getModuleItems() != null) {

        	//List<MeveoModuleItem> moduleItems = getSortedModuleItems(module.getModuleItems());
        	
            for (MeveoModuleItem item : module.getModuleItems()) {
                try {
                    if (MeveoModule.class.isAssignableFrom(Class.forName(item.getItemClass()))) {
                        loadModuleItem(item);
                        T itemModule = (T) item.getItemEntity();
                        remove(itemModule);
                    }
                } catch (Exception e) {
                    log.error("Failed to delete a submodule", e);
                }
            }
        }

        if (module != null) {
        	super.remove(module);

            if (module instanceof BaseEntity && (module.getClass().isAnnotationPresent(ObservableEntity.class) || module.getClass().isAnnotationPresent(ModuleItem.class))) {
                entityRemovedEventProducer.fire((BaseEntity) module);
            }

            // Remove custom field values from cache if applicable
            if (module instanceof ICustomFieldEntity) {
                customFieldInstanceService.removeCFValues((ICustomFieldEntity) module);
            }

            if (module instanceof IImageUpload) {
                try {
                    ImageUploadEventHandler<MeveoModule> imageUploadEventHandler = new ImageUploadEventHandler<>(currentUser.getProviderCode());
                    imageUploadEventHandler.deleteImage(module);
                } catch (IOException e) {
                    log.error("Failed deleting image file");
                }
            }
        }

    }
    
    @SuppressWarnings("unchecked")
    public List<T> listInstalled() {
        QueryBuilder qb = new QueryBuilder(entityClass, "b", null);
        qb.startOrClause();
        qb.addCriterion("installed", "=", true, true);
        qb.addSql("moduleSource is null");
        qb.endOrClause();

        try {
            return (List<T>) qb.getQuery(getEntityManager()).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

}