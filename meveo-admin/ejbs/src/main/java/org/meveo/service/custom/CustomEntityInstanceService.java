package org.meveo.service.custom;

import org.apache.commons.lang.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.Repository;
import org.meveo.persistence.CrossStorageService;
import org.meveo.service.base.BusinessService;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * CustomEntityInstance persistence service implementation.
 * 
 */
@Stateless
public class CustomEntityInstanceService extends BusinessService<CustomEntityInstance> {

    @Inject
    private CustomFieldsCacheContainerProvider cetCache;
    
    @Inject
    private CrossStorageService crossStorageService;

    @Override
    public void create(CustomEntityInstance entity) throws BusinessException {
        filterValues(entity);
        super.create(entity);
    }

    @Override
    public CustomEntityInstance update(CustomEntityInstance entity) throws BusinessException {
        filterValues(entity);
        return super.update(entity);
    }

    /**
     * Remove all records related to the given CET
     *
     * @param cetCode CET code the records are related to
     */
    public void removeByCet(String cetCode) {
        String query = "DELETE FROM " + CustomEntityInstance.class.getName() + " \n"
                + "WHERE cetCode = :cetCode";

        getEntityManager().createQuery(query).setParameter("cetCode", cetCode).executeUpdate();
    }

    public CustomEntityInstance findByCodeByCet(String cetCode, String code) {
        QueryBuilder qb = new QueryBuilder(getEntityClass(), "cei", null);
        qb.addCriterion("cei.cetCode", "=", cetCode, true);
        qb.addCriterion("cei.code", "=", code, true);

        try {
            return (CustomEntityInstance) qb.getQuery(getEntityManager()).getSingleResult();
        } catch (NoResultException e) {
            log.warn("No CustomEntityInstance by code {} and cetCode {} found", code, cetCode);
            return null;
        }
    }

    public List<CustomEntityInstance> findChildEntities(String cetCode, String parentEntityUuid) {

        QueryBuilder qb = new QueryBuilder(getEntityClass(), "cei", null);
        qb.addCriterion("cei.cetCode", "=", cetCode, true);
        qb.addCriterion("cei.parentEntityUuid", "=", parentEntityUuid, true);

        return qb.getQuery(getEntityManager()).getResultList();
    }

    public List<CustomEntityInstance> findByCode(String cetCode, String code) {

        QueryBuilder qb = new QueryBuilder(getEntityClass(), "cei", null);
        qb.addCriterion("cei.cetCode", "=", cetCode, true);
        if (StringUtils.isNotEmpty(code)) {
            qb.like("cei.code", code, QueryBuilder.QueryLikeStyleEnum.MATCH_ANYWHERE, false);
        }

        return qb.getQuery(getEntityManager()).getResultList();
    }

    /**
     * Retrieves a CEI using it's UUID
     *
     * @param cetCode Code of the related CET
     * @param uuid  UUID of the CEI
     * @return the CEI or null if not found
     */
    public CustomEntityInstance findByUuid(String cetCode, String uuid) {
        QueryBuilder qb = new QueryBuilder(getEntityClass(), "cei", null);
        qb.addCriterion("cei.cetCode", "=", cetCode, true);
        qb.addCriterion("cei.uuid", "=", uuid, true);

        try {
            return qb.getTypedQuery(getEntityManager(), CustomEntityInstance.class)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public List<CustomEntityInstance> list(String cetCode, Map<String, Object> values){
        QueryBuilder qb = new QueryBuilder(getEntityClass(), "cei", null);
        qb.addCriterion("cei.cetCode", "=", cetCode, true);

        final List<CustomEntityInstance> resultList = qb.getTypedQuery(getEntityManager(), CustomEntityInstance.class).getResultList();

        if(values != null && !values.isEmpty()){
            return resultList.stream()
                    .filter(customEntityInstance -> filterOnValues(values, customEntityInstance))
                    .collect(Collectors.toList());
        }

        return resultList;
    }

    private boolean filterOnValues(Map<String, Object> values, CustomEntityInstance customEntityInstance) {
        final Map<String, Object> cfValuesAsValues = customEntityInstance.getCfValuesAsValues();
        for(Map.Entry<String, Object> value : values.entrySet()){
            if(!cfValuesAsValues.get(value.getKey()).equals(value.getValue())){
                return false;
            }
        }
        return true;
    }

    private void filterValues(CustomEntityInstance cei){
        CustomEntityTemplate cet = cetCache.getCustomEntityTemplate(cei.getCetCode());
        CustomFieldValues cfValues = cei.getCfValues();
        if (cfValues != null && cfValues.getValuesByCode() != null) {
            for (String valueCode : new HashSet<>(cfValues.getValuesByCode().keySet())) {
                CustomFieldTemplate cft = cetCache.getCustomFieldTemplate(valueCode, cet.getAppliesTo());
                if (cft != null && (cft.getStorages() == null || !cft.getStorages().contains(DBStorageType.SQL))) {
                    cfValues.removeValue(valueCode);
                }
            }
        }
    }

	public void createOrUpdateInCrossStorage(Repository repository, CustomEntityInstance entity) throws BusinessApiException, EntityDoesNotExistsException, BusinessException, IOException {
		crossStorageService.createOrUpdate(repository, entity);
	}
	
	public void removeInCrossStorage(Repository repository, CustomEntityTemplate cet, String uuid) throws BusinessException {
		crossStorageService.remove(repository, cet, uuid);
	}
}