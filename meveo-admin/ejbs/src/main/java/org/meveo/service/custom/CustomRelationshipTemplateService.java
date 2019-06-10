/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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
package org.meveo.service.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.NonUniqueResultException;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.infinispan.Cache;
import org.meveo.admin.exception.BusinessException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.ParamBean;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.service.admin.impl.PermissionService;
import org.meveo.service.base.BusinessService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.index.ElasticClient;

@Stateless
public class CustomRelationshipTemplateService extends BusinessService<CustomRelationshipTemplate> {

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;
    
    @Inject
    private CustomTableCreatorService customTableCreatorService;

    @Inject
    private PermissionService permissionService;

    @Inject
    private CustomFieldsCacheContainerProvider customFieldsCache;

    @Inject
    private ElasticClient elasticClient;

    @Resource(lookup = "java:jboss/infinispan/cache/meveo/unique-crt")
    private Cache<String, Boolean> uniqueRelations;

    private ParamBean paramBean = ParamBean.getInstance();

    @Override
    public void create(CustomRelationshipTemplate cet) throws BusinessException {
        super.create(cet);
        try {
            permissionService.createIfAbsent("modify", cet.getPermissionResourceName(), paramBean.getProperty("role.modifyAllCE", "ModifyAllCE"));
            permissionService.createIfAbsent("read", cet.getPermissionResourceName(), paramBean.getProperty("role.readAllCE", "ReadAllCE"));
            if(cet.getAvailableStorages().contains(DBStorageType.SQL)) {
            	customTableCreatorService.createCrtTable(cet);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public CustomRelationshipTemplate update(CustomRelationshipTemplate cet) throws BusinessException {
        CustomRelationshipTemplate cetUpdated = super.update(cet);
        
        permissionService.createIfAbsent("modify", cet.getPermissionResourceName(), paramBean.getProperty("role.modifyAllCE", "ModifyAllCE"));
        permissionService.createIfAbsent("read", cet.getPermissionResourceName(), paramBean.getProperty("role.readAllCE", "ReadAllCE"));
        
        // SQL Storage logic
        if(cet.getAvailableStorages().contains(DBStorageType.SQL)) {
        	boolean created = customTableCreatorService.createCrtTable(cet);
        	// Create the custom fields for the table if the table has been created
        	if(created) {
        		for(CustomFieldTemplate cft : customFieldTemplateService.findByAppliesTo(cet.getAppliesTo()).values()) {
    				customTableCreatorService.addField(SQLStorageConfiguration.getDbTablename(cet), cft);
        		}
        	}
        }else {
        	customTableCreatorService.removeTable(SQLStorageConfiguration.getDbTablename(cet));
        }

        return cetUpdated;
    }

    public void synchronizeStorages(CustomRelationshipTemplate crt) throws BusinessException {
        // Synchronize custom fields storages with CRT available storages
        for (CustomFieldTemplate cft : customFieldTemplateService.findByAppliesToNoCache(crt.getAppliesTo()).values()) {
            for (DBStorageType storage : new ArrayList<>(cft.getStorages())) {
                if (!crt.getAvailableStorages().contains(storage)) {
                    log.info("Remove storage '{}' from CFT '{}' of CRT '{}'", storage, cft.getCode(), crt.getCode());
                    cft.getStorages().remove(storage);
                    customFieldTemplateService.update(cft);
                }
            }
        }
    }


    @Override
    public void remove(Long id) throws BusinessException {
        CustomRelationshipTemplate cet = findById(id);
        Map<String, CustomFieldTemplate> fields = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());
        for (CustomFieldTemplate cft : fields.values()) {
            customFieldTemplateService.remove(cft.getId());
        }
        if(cet.getAvailableStorages().contains(DBStorageType.SQL)) {
            customTableCreatorService.removeTable(SQLStorageConfiguration.getDbTablename(cet));
        }
        super.remove(id);
    }

    /**
     * Whether the relation is unique
     *
     * @param code Code of the relationship template
     * @return {@code true} if the relationship is unique
     */
    public boolean isUnique(String code){
        return uniqueRelations.computeIfAbsent(code, key -> {
            try {
                CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
                CriteriaQuery<Boolean> query = cb.createQuery(Boolean.class);
                Root<CustomRelationshipTemplate> root = query.from(getEntityClass());
                query.select(root.get("unique"));
                query.where(cb.equal(root.get("code"), key));
                query.distinct(true);
                return getEntityManager().createQuery(query).getSingleResult();
            } catch (NoResultException e) {
                return false;
            }
        });
    }

    /**
     * Get a list of custom entity templates for cache
     * 
     * @return A list of custom entity templates
     */
    public List<CustomRelationshipTemplate> getCRTForCache() {
        return getEntityManager().createNamedQuery("CustomRelationshipTemplate.getCRTForCache", CustomRelationshipTemplate.class).getResultList();
    }

    /**
     * Find entity by code
     *
     * @param code Code to match
     * @return
     */
    @Override
    public CustomRelationshipTemplate findByCode(String code){
        return super.findByCode(code);
    }

    public List<CustomRelationshipTemplate> findByStartEndAndName(String startCode, String endCode, String name){
        return getEntityManager().createNamedQuery("CustomRelationshipTemplate.findByStartEndAndName", CustomRelationshipTemplate.class)
                .setParameter("startCode", startCode)
                .setParameter("endCode", endCode)
                .setParameter("name", name)
                .getResultList();

    }
}
