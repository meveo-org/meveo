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

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.Query;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;

import org.apache.commons.httpclient.util.HttpURLConnection;
import org.jboss.resteasy.client.jaxrs.BasicAuthentication;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyWebTarget;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.ApiService;
import org.meveo.api.dto.ActionStatus;
import org.meveo.api.dto.ActionStatusEnum;
import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.response.module.MeveoModuleDtosResponse;
import org.meveo.commons.utils.EjbUtils;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.commons.utils.StringUtils;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.export.RemoteAuthenticationException;
import org.meveo.model.BusinessEntity;
import org.meveo.model.ModuleItem;
import org.meveo.model.communication.MeveoInstance;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.service.base.PersistenceService;
import org.meveo.service.communication.impl.MeveoInstanceService;
import org.meveo.service.script.module.ModuleScriptInterface;
import org.meveo.service.script.module.ModuleScriptService;
import org.meveo.util.EntityCustomizationUtils;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.ws.rs.core.Response;
import java.util.*;


/**
 * EJB for managing MeveoModule entities
 * @author Clément Bareth
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @lastModifiedVersion 6.3.0
 */
@Stateless
public class MeveoModuleService extends GenericModuleService<MeveoModule> {

    @Inject
    private ModuleScriptService moduleScriptService;

    @Inject
    private MeveoInstanceService meveoInstanceService;

    /**
     * import module from remote meveo instance.
     * 
     * @param meveoInstance meveo instance
     * @return list of meveo module
     * @throws BusinessException business exception.
     */
    public List<MeveoModuleDto> downloadModulesFromMeveoInstance(MeveoInstance meveoInstance) throws BusinessException {
        List<MeveoModuleDto> result;
        try {
            String url = "api/rest/module/list";
            String baseurl = meveoInstance.getUrl().endsWith("/") ? meveoInstance.getUrl() : meveoInstance.getUrl() + "/";
            String username = meveoInstance.getAuthUsername() != null ? meveoInstance.getAuthUsername() : "";
            String password = meveoInstance.getAuthPassword() != null ? meveoInstance.getAuthPassword() : "";
            ResteasyClient client = meveoInstanceService.getRestEasyClient();
            ResteasyWebTarget target = client.target(baseurl + url);
            BasicAuthentication basicAuthentication = new BasicAuthentication(username, password);
            target.register(basicAuthentication);

            Response response = target.request().get();
            if (response.getStatus() != HttpURLConnection.HTTP_OK) {
                if (response.getStatus() == HttpURLConnection.HTTP_UNAUTHORIZED || response.getStatus() == HttpURLConnection.HTTP_FORBIDDEN) {
                    throw new RemoteAuthenticationException("Http status " + response.getStatus() + ", info " + response.getStatusInfo().getReasonPhrase());
                } else {
                    throw new BusinessException("Http status " + response.getStatus() + ", info " + response.getStatusInfo().getReasonPhrase());
                }
            }

            MeveoModuleDtosResponse resultDto = response.readEntity(MeveoModuleDtosResponse.class);
            if (resultDto == null) {
                throw new BusinessException("No response body from meveo instance " + meveoInstance.getCode());
            }

            if (ActionStatusEnum.SUCCESS != resultDto.getActionStatus().getStatus()) {
                throw new BusinessException("Code " + resultDto.getActionStatus().getErrorCode() + ", info " + resultDto.getActionStatus().getMessage());
            }

            result = resultDto.getModules();
            if (result != null) {
                result.sort(Comparator.comparing(BusinessEntityDto::getCode));
            }

            return result;

        } catch (Exception e) {
            log.error("Failed to communicate {}. Reason {}", meveoInstance.getCode(), (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
            throw new BusinessException("Fail to communicate " + meveoInstance.getCode() + ". Error " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    /**
     * Publish meveo module with DTO items to remote meveo instance.
     * 
     * @param module meveo module
     * @param meveoInstance meveo instance.
     * @throws BusinessException business exception.
     */
    @SuppressWarnings("unchecked")
    public void publishModule2MeveoInstance(MeveoModule module, MeveoInstance meveoInstance) throws BusinessException {
        log.debug("export module {} to {}", module, meveoInstance);
        final String url = "api/rest/module/createOrUpdate";

        try {
            ApiService<MeveoModule, MeveoModuleDto> moduleApi = (ApiService<MeveoModule, MeveoModuleDto>) EjbUtils.getServiceInterface("MeveoModuleApi");
            if(moduleApi == null){
                throw new IllegalArgumentException("Cannot find api MeveoModuleApi bean");
            }

            MeveoModuleDto moduleDto = moduleApi.find(module.getCode());

            Response response = meveoInstanceService.publishDto2MeveoInstance(url, meveoInstance, moduleDto);
            ActionStatus actionStatus = response.readEntity(ActionStatus.class);
            if (actionStatus == null) {
                throw new BusinessException("Cannot read response status");
            }

            if (ActionStatusEnum.SUCCESS != actionStatus.getStatus()) {
                throw new BusinessException("Code " + actionStatus.getErrorCode() + ", info " + actionStatus.getMessage());
            }
        } catch (Exception e) {
            log.error("Error when export module {} to {}. Reason {}", module.getCode(), meveoInstance.getCode(), (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()), e);
            throw new BusinessException("Fail to communicate " + meveoInstance.getCode() + ". Error " + (e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    public List<MeveoModuleItem> findByCodeAndItemType(String code, String className) {
        QueryBuilder qb = new QueryBuilder(MeveoModuleItem.class, "m");
        qb.addCriterion("itemCode", "=", code, true);
        qb.addCriterion("itemClass", "=", className, true);

        try {
            return (List<MeveoModuleItem>) qb.getQuery(getEntityManager()).getResultList();
        } catch (NoResultException e) {
            return null;
        }
    }

    public MeveoModule uninstall(MeveoModule module) throws BusinessException {
        return uninstall(module, false, false);
    }

    public MeveoModule uninstall(MeveoModule module, boolean remove) throws BusinessException {
        return uninstall(module, false, remove);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private MeveoModule uninstall(MeveoModule module, boolean childModule, boolean remove) throws BusinessException {

        if (!module.isInstalled()) {
            throw new BusinessException("Module is not installed");
        }

        ModuleScriptInterface moduleScript = null;
        if (module.getScript() != null) {
            moduleScript = moduleScriptService.preUninstallModule(module.getScript().getCode(), module);
        }

        for (MeveoModuleItem item : module.getModuleItems()) {
            
            // check if moduleItem is linked to other active module
            if (isChildOfOtherActiveModule(item.getItemCode(), item.getItemClass())) {
                continue;
            }
            
            loadModuleItem(item);
            BusinessEntity itemEntity = item.getItemEntity();
            if (itemEntity == null) {
                continue;
            }

            try {
                if (itemEntity instanceof MeveoModule) {
                    uninstall((MeveoModule) itemEntity, true, remove);
                } else {

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

                    if(remove) {
                        persistenceServiceForItem.remove(itemEntity);
                    } else {
                        persistenceServiceForItem.disable(itemEntity);
                    }

                }
            } catch (Exception e) {
                log.error("Failed to uninstall/disable module item. Module item {}", item, e);
            }
        }

        if (moduleScript != null) {
            moduleScriptService.postUninstallModule(moduleScript, module);
        }

        // Remove if it is a child module
        if (childModule) {
            remove(module);
            return null;

        // Otherwise mark it uninstalled and clear module items
        } else {
            module.setInstalled(false);
            module.getModuleItems().clear();
            return update(module);
        }
    }

    /**
     * Check whether the given item is a child of an other active module
     *
     * @param moduleItemCode Module item code
     * @param itemClass      Class of the module item
     * @return <code>true</code> if the given item exists in an other module
     */
    public boolean isChildOfOtherActiveModule(String moduleItemCode, String itemClass) {
        QueryBuilder qb = new QueryBuilder(MeveoModuleItem.class, "i", null, Collections.singletonList("meveoModule"));
        qb.addCriterion("i.itemCode", "=", moduleItemCode, true);
        qb.addCriterion("i.itemClass", "=", itemClass, false);
        qb.addBooleanCriterion("meveoModule.disabled", false);
        return qb.count(getEntityManager()) > 1;
    }

    @SuppressWarnings("unchecked")
    public String getRelatedModulesAsString(String itemCode, String itemClazz, String appliesTo) {
        QueryBuilder qb = new QueryBuilder(MeveoModule.class, "m", Collections.singletonList("moduleItems as i"));
        qb.addCriterion("i.itemCode", "=", itemCode, true);
        qb.addCriterion("i.itemClass", "=", itemClazz, false);
        qb.addCriterion("i.appliesTo", "=", appliesTo, false);
        List<MeveoModule> modules = qb.getQuery(getEntityManager()).getResultList();

        if (modules != null) {
            StringBuilder sb = new StringBuilder();
            int i = 0;
            for (MeveoModule module : modules) {
                if (i != 0) {
                    sb.append(";");
                }
                sb.append(module.getCode());
                i++;
            }
            return sb.toString();
        }
        return null;
    }

    /**
     * Find entity by code - match the beginning of code.
     *
     * @param code Beginning of code
     * @return A list of entities which code starts with a given value
     */
    @SuppressWarnings("unchecked")
    public List<MeveoModule> findLikeWithCode(String code) {
        try {
            QueryBuilder qb = new QueryBuilder(getEntityClass(), "be");
            if (!StringUtils.isBlank(code)) {
                qb.like("be.code", code, QueryBuilder.QueryLikeStyleEnum.MATCH_ANYWHERE, false);
            }

            return (List<MeveoModule>) qb.getQuery(getEntityManager()).getResultList();

        } catch (NoResultException | NonUniqueResultException ne) {
            return null;
        }
    }

    public List<MeveoModule> list(MeveoModuleFilters filters) {
        return list(filters, "*", MeveoModule.class);
    }

    public List<String> listCodesOnly(MeveoModuleFilters filters) {
        return list(filters, "code", String.class);
    }

    private <T> List<T> list(MeveoModuleFilters filters, String projection, Class<T> returnedClass) {
    	if (projection == "*") {
			projection = "";

		} else {
			projection = "." + projection;
		}

    	StringBuilder queryBuilder = new StringBuilder("SELECT DISTINCT m" + projection + " FROM " + entityClass.getName() + " m");
        if(filters.getItemType() !=null && filters.getItemClass() != null){
            queryBuilder.append(" INNER JOIN m.moduleItems i");
        }

        Map<String, Object> parameters = new HashMap<>();

        queryBuilder.append(" WHERE 1 = 1 \n");

        if(filters.getActive() != null){
            if(filters.getActive()) {
                queryBuilder.append(" AND m.disabled = 0 ");
            } else {
                queryBuilder.append(" AND m.disabled = 1 ");
            }
        }

        if(filters.getDownloaded() != null){
            if(!filters.getDownloaded()) {
                queryBuilder.append(" AND m.moduleSource = '' OR m.moduleSource = null ");
            } else {
                queryBuilder.append(" AND m.moduleSource != null AND m.moduleSource != '' ");
            }
        }

        if(filters.getItemCode() != null && filters.getItemClass() != null){
            queryBuilder.append(" AND i.itemCode = :itemCode AND i.itemClass = :itemClass");
            parameters.put("itemCode", filters.getItemCode());
            parameters.put("itemClass", filters.getItemClass());
        }

        final TypedQuery<T> query = getEntityManager().createQuery(queryBuilder.toString(), returnedClass);
        parameters.forEach(query::setParameter);

        return query.getResultList();
    }


	/**
	 * Observer when an entity that extends a BusinessEntity is deleted which is
	 * annotated by MeveoModuleItem.
	 *
	 * @param be BusinessEntity
	 * @throws BusinessException
	 */
	public void onMeveoModuleItemDelete(@Observes @Removed BusinessEntity be) throws BusinessException {
		if (be.getClass().isAnnotationPresent(ModuleItem.class)) {
			QueryBuilder qb = new QueryBuilder(MeveoModuleItem.class, "i");
			qb = qb.addCriterion("itemCode", "=", be.getCode(), true);
			qb = qb.addCriterion("itemClass", "=", be.getClass().getName(), true);

			try {
				Long count = qb.count(getEntityManager());

				// need to do the check when uninstalling
				if (count > 0) {
					Query query = getEntityManager().createNamedQuery("MeveoModuleItem.delete");
					query = query.setParameter("itemCode", be.getCode());
					query = query.setParameter("itemClass", be.getClass().getName());
					query.executeUpdate();
				}
			} catch (NoResultException e) {

			}
		}
	}

	@SuppressWarnings("unchecked")
	public void onCftCreated(@Observes @Created CustomFieldTemplate cft) throws BusinessException {
		String cetCode = EntityCustomizationUtils.getEntityCode(cft.getAppliesTo());
		Query q = getEntityManager().createNamedQuery("MeveoModuleItem.synchronizeCftCreate");
		q = q.setParameter("itemCode", cetCode);
		q = q.setParameter("itemClass", CustomEntityTemplate.class.getName());

		try {
			List<MeveoModule> modules = q.getResultList();
			if (modules != null && !modules.isEmpty()) {
				for (MeveoModule module : modules) {
					MeveoModuleItem mi = new MeveoModuleItem();
					mi.setMeveoModule(module);
					mi.setAppliesTo(cft.getAppliesTo());
					mi.setItemClass(CustomFieldTemplate.class.getName());
					mi.setItemCode(cft.getCode());
					meveoModuleItemService.create(mi);
				}
			}

		} catch (NoResultException e) {

		}
	}

	public void onCftDeleted(@Observes @Removed CustomFieldTemplate cft) {
		Query q = getEntityManager().createNamedQuery("MeveoModuleItem.synchronizeCftDelete");
		q = q.setParameter("itemCode", cft.getCode());
		q = q.setParameter("itemClass", CustomEntityTemplate.class.getName());
		q.executeUpdate();
	}
}