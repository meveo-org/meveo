/*
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

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.ParamBeanFactory;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.PrimitiveTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.service.admin.impl.PermissionService;
import org.meveo.service.base.BusinessService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.index.ElasticClient;

/**
 * @author Wassim Drira
 * @lastModifiedVersion 5.0
 */
@Stateless
public class CustomEntityTemplateService extends BusinessService<CustomEntityTemplate> {

    private static final String PRIMITIVE_CFT_VALUE = "value";

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private PermissionService permissionService;

    @Inject
    private CustomFieldsCacheContainerProvider customFieldsCache;

    @Inject
    private ElasticClient elasticClient;

    private static boolean useCETCache = true;

    @Inject
    private ParamBeanFactory paramBeanFactory;

    @PostConstruct
    private void init() {
        useCETCache = Boolean.parseBoolean(ParamBeanFactory.getAppScopeInstance().getProperty("cache.cacheCET", "true"));
    }

    @Override
    public void create(CustomEntityTemplate cet) throws BusinessException {

        ParamBean paramBean = paramBeanFactory.getInstance();
        super.create(cet);
        customFieldsCache.addUpdateCustomEntityTemplate(cet);

        elasticClient.createCETMapping(cet);

        try {
            permissionService.createIfAbsent(cet.getModifyPermission(), paramBean.getProperty("role.modifyAllCE", "ModifyAllCE"));
            permissionService.createIfAbsent(cet.getReadPermission(), paramBean.getProperty("role.readAllCE", "ReadAllCE"));

            /* If cet is a primitive type, create custom field of corresponding type */
            if (cet.isPrimitiveEntity()) {
                createPrimitiveCft(cet);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void createPrimitiveCft(CustomEntityTemplate cet) throws BusinessException {
        // Define CFT
        final CustomFieldTemplate customFieldTemplate = new CustomFieldTemplate();
        turnIntoPrimitive(cet, customFieldTemplate);
        // Create CFT
        customFieldTemplateService.create(customFieldTemplate);
    }

    public static void turnIntoPrimitive(CustomEntityTemplate cet, CustomFieldTemplate customFieldTemplate) {
        customFieldTemplate.setActive(true);                        // Always active
        customFieldTemplate.setAllowEdit(false);                    // CFT can't be updated
        customFieldTemplate.setAppliesTo(cet.getAppliesTo());
        if (cet.getPrimitiveType() == null) {
            throw new IllegalArgumentException("Primitive type class must be provided");
        }
        customFieldTemplate.setFieldType(cet.getPrimitiveType().getCftType());
        customFieldTemplate.setUnique(true);                        // Must be unique
        customFieldTemplate.setCode(PRIMITIVE_CFT_VALUE);            // Code is 'value'
        customFieldTemplate.setDescription(PRIMITIVE_CFT_VALUE);    // Label is 'value'
        customFieldTemplate.setFilter(true);                        // Can be used as filter
        customFieldTemplate.setValueRequired(true);                    // Always required
        customFieldTemplate.setStorageType(CustomFieldStorageTypeEnum.SINGLE);
    }

    @Override
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    @Asynchronous
    protected void afterUpdate(CustomEntityTemplate cet) throws BusinessException {
        /* Primitive entity and type management */
        if (cet.isPrimitiveEntity() && cet.getPrimitiveType() != null) {
            final Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());
            CustomFieldTemplate valueCft = cfts.get(PRIMITIVE_CFT_VALUE);
            if (valueCft == null) {
                createPrimitiveCft(cet);
            } else if (valueCft.getFieldType() != cet.getPrimitiveType().getCftType()) {
                flush();
                valueCft.setFieldType(cet.getPrimitiveType().getCftType());
                customFieldTemplateService.update(valueCft);
            }
        } else {
            cet.setPrimitiveType(null);
        }
    }

    @Override
    public CustomEntityTemplate update(CustomEntityTemplate cet) throws BusinessException {
        ParamBean paramBean = paramBeanFactory.getInstance();


        /* Update */

        CustomEntityTemplate cetUpdated = super.update(cet);

        customFieldsCache.addUpdateCustomEntityTemplate(cet);

        try {
            permissionService.createIfAbsent(cet.getModifyPermission(), paramBean.getProperty("role.modifyAllCE", "ModifyAllCE"));
            permissionService.createIfAbsent(cet.getReadPermission(), paramBean.getProperty("role.readAllCE", "ReadAllCE"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        return cetUpdated;
    }

    @Override
    public void remove(Long id) throws BusinessException {

        CustomEntityTemplate cet = findById(id);

        Map<String, CustomFieldTemplate> fields = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());

        for (CustomFieldTemplate cft : fields.values()) {
            customFieldTemplateService.remove(cft.getId());
        }
        super.remove(id);

        customFieldsCache.removeCustomEntityTemplate(cet);
    }

    /**
     * List custom entity templates, optionally filtering by an active status. Custom entity templates will be looked up in cache or retrieved from DB.
     *
     * @param active Custom entity template's status. Or any if null
     * @return A list of custom entity templates
     */
    @Override
    public List<CustomEntityTemplate> list(Boolean active) {

        if (useCETCache && (active == null || active)) {

            List<CustomEntityTemplate> cets = new ArrayList<CustomEntityTemplate>();
            cets.addAll(customFieldsCache.getCustomEntityTemplates());

            // Populate cache if record is not found in cache
            if (cets.isEmpty()) {
                cets = super.list(active);
                if (cets != null) {
                    cets.forEach((cet) -> customFieldsCache.addUpdateCustomEntityTemplate(cet));
                }
            }

            return cets;

        } else {
            return super.list(active);
        }
    }

    public List<CustomEntityTemplate> listNoCache() {
        return super.list((Boolean) null);
    }

    @Override
    public List<CustomEntityTemplate> list(PaginationConfiguration config) {

        if (useCETCache && (config.getFilters() == null || config.getFilters().isEmpty()
                || (config.getFilters().size() == 1 && config.getFilters().get("disabled") != null && !(boolean) config.getFilters().get("disabled")))) {
            List<CustomEntityTemplate> cets = new ArrayList<CustomEntityTemplate>();
            cets.addAll(customFieldsCache.getCustomEntityTemplates());

            // Populate cache if record is not found in cache
            if (cets.isEmpty()) {
                cets = super.list(config);
                if (cets != null) {
                    cets.forEach((cet) -> customFieldsCache.addUpdateCustomEntityTemplate(cet));
                }
            }

            return cets;

        } else {
            return super.list(config);
        }
    }

    public List<CustomEntityTemplate> getCETsWithSubTemplates() {
        String query = new StringBuffer()
                .append("SELECT DISTINCT cet from CustomEntityTemplate cet ")
                .append("LEFT JOIN FETCH cet.subTemplates")
                .toString();

        return getEntityManager().createQuery(query, CustomEntityTemplate.class).getResultList();
    }


    /**
     * Get a list of custom entity templates for cache
     *
     * @return A list of custom entity templates
     */
    public List<CustomEntityTemplate> getCETForCache() {
        return getEntityManager().createNamedQuery("CustomEntityTemplate.getCETForCache", CustomEntityTemplate.class).getResultList();
    }

    /**
     * A generic method that returns a filtered list of ICustomFieldEntity given an entity class and code.
     *
     * @param entityClass - class of an entity. eg. org.meveo.catalog.OfferTemplate
     * @param entityCode  - code of entity
     * @return customer field entity
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public ICustomFieldEntity findByClassAndCode(Class entityClass, String entityCode) {
        ICustomFieldEntity result = null;
        QueryBuilder queryBuilder = new QueryBuilder(entityClass, "a", null);
        queryBuilder.addCriterion("code", "=", entityCode, true);
        List<ICustomFieldEntity> entities = (List<ICustomFieldEntity>) queryBuilder.getQuery(getEntityManager()).getResultList();
        if (entities != null && !entities.isEmpty()) {
            result = entities.get(0);
        }

        return result;
    }

    /**
     * Get a list of custom entity templates for Configuration
     *
     * @return A list of custom entity templates
     */
    public List<CustomEntityTemplate> getCETForConfiguration() {
        return getEntityManager().createNamedQuery("CustomEntityTemplate.getCETForConfiguration", CustomEntityTemplate.class).getResultList();
    }

    public PrimitiveTypeEnum getPrimitiveType(String code) {
        return getEntityManager().createNamedQuery("CustomEntityTemplate.PrimitiveType", PrimitiveTypeEnum.class)
                .setParameter("code", code)
                .getSingleResult();
    }
}