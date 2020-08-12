package org.meveo.service.custom;

import java.time.Instant;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.EntityReferenceWrapper;
import org.meveo.model.crm.custom.CustomFieldStorageTypeEnum;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.crm.custom.CustomFieldValue;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.Repository;
import org.meveo.model.wf.WFTransition;
import org.meveo.model.wf.Workflow;
import org.meveo.service.base.BusinessService;
import org.meveo.service.crm.impl.CustomFieldInstanceService;

import com.ibm.icu.math.BigDecimal;
import org.meveo.service.crm.impl.CustomFieldTemplateService;

/**
 * CustomEntityInstance persistence service implementation.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@Stateless
public class CustomEntityInstanceService extends BusinessService<CustomEntityInstance> {

	@Inject
	private CustomFieldsCacheContainerProvider cetCache;
	
	@Inject
	private CustomFieldInstanceService customFieldInstanceService;

	@Inject
	private CustomFieldTemplateService customFieldTemplateService;

	@Inject
	private CustomTableService customTableService;

	@Override
	public void create(CustomEntityInstance entity) throws BusinessException {
		filterValues(entity);
		super.create(entity);
	}

	@Override
	public CustomEntityInstance update(CustomEntityInstance entity) throws BusinessException {

		if (entity.getCfValues() != null && entity.getCfValues().getValues() != null) {
			log.debug("Updating cei.cfValues={}", entity.getCfValues().getValues());
		}
		filterValues(entity);
		entity = super.update(entity);

		return entity;
	}
	
	public CustomEntityInstance fromMap(CustomEntityTemplate cet, Map<String, Object> values) {
		CustomEntityInstance cei = new CustomEntityInstance();
		cei.setCode((String) values.get("code"));
		cei.setCet(cet);
		cei.setDescription((String) values.get("description"));
		cei.setUuid((String) values.get("uuid"));
		cei.setCetCode(cet.getCode());
		try {
			customFieldInstanceService.setCfValues(cei, cet.getCode(), values);
		} catch (BusinessException e) {
			log.error("Error setting cf values", e);
		}
		
		return cei;
	}

	/**
	 * Remove all records related to the given CET
	 *
	 * @param cetCode CET code the records are related to
	 */
	public void removeByCet(String cetCode) {
		String query = "DELETE FROM " + CustomEntityInstance.class.getName() + " \n" + "WHERE cetCode = :cetCode";

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

	@SuppressWarnings("unchecked")
	public List<CustomEntityInstance> findChildEntities(String cetCode, String parentEntityUuid) {

		QueryBuilder qb = new QueryBuilder(getEntityClass(), "cei", null);
		qb.addCriterion("cei.cetCode", "=", cetCode, true);
		qb.addCriterion("cei.parentEntityUuid", "=", parentEntityUuid, true);

		return qb.getQuery(getEntityManager()).getResultList();
	}

	@SuppressWarnings("unchecked")
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
	 * @param uuid    UUID of the CEI
	 * @return the CEI or null if not found
	 */
	public CustomEntityInstance findByUuid(String cetCode, String uuid) {
		QueryBuilder qb = new QueryBuilder(getEntityClass(), "cei", null);
		qb.addCriterion("cei.cetCode", "=", cetCode, true);
		qb.addCriterion("cei.uuid", "=", uuid, true);

		try {
			return qb.getTypedQuery(getEntityManager(), CustomEntityInstance.class).getSingleResult();
		} catch (NoResultException e) {
			return null;
		}
	}

	public List<CustomEntityInstance> list(String cetCode, Map<String, Object> values) {
		QueryBuilder qb = new QueryBuilder(getEntityClass(), "cei", null);
		qb.addCriterion("cei.cetCode", "=", cetCode, true);

		final List<CustomEntityInstance> resultList = qb.getTypedQuery(getEntityManager(), CustomEntityInstance.class).getResultList();

		if (values != null && !values.isEmpty()) {
			return resultList.stream().filter(customEntityInstance -> filterOnValues(values, customEntityInstance)).collect(Collectors.toList());
		}

		return resultList;
	}

	public List<CustomEntityInstance> list(String cetCode, Map<String, Object> values, PaginationConfiguration paginationConfiguration) {
		return list(cetCode, false, values, paginationConfiguration);
	}
	
	public List<CustomEntityInstance> list(String cetCode, boolean isStoreAsTable, Map<String, Object> values, PaginationConfiguration paginationConfiguration) {

		QueryBuilder qb = new QueryBuilder(getEntityClass(), "cei", null);
		qb.addCriterion("cei.cetCode", "=", cetCode, true);
		qb.addPaginationConfiguration(paginationConfiguration);

		final List<CustomEntityInstance> resultList = qb.getTypedQuery(getEntityManager(), CustomEntityInstance.class).getResultList();

		if (values != null && !values.isEmpty()) {
			return resultList.stream().filter(customEntityInstance -> filterOnValues(values, customEntityInstance, isStoreAsTable)).collect(Collectors.toList());
		}

		return resultList;
	}

	public long count(String cetCode, PaginationConfiguration paginationConfiguration) {

		QueryBuilder qb = new QueryBuilder(getEntityClass(), "cei", null);
		qb.addCriterion("cei.cetCode", "=", cetCode, true);

		qb.addPaginationConfiguration(paginationConfiguration);

		return qb.count(getEntityManager());
	}

	private boolean filterOnValues(Map<String, Object> filterValues, CustomEntityInstance customEntityInstance) {
		return filterOnValues(filterValues, customEntityInstance, false);
	}
	
	private boolean filterOnValues(Map<String, Object> filterValues, CustomEntityInstance customEntityInstance, boolean isStoreAsTable) {
		final Map<String, Object> cfValuesAsValues = customEntityInstance.getCfValuesAsValues();
		for (Map.Entry<String, Object> filterValue : filterValues.entrySet()) {

			String[] fieldInfo = filterValue.getKey().split(" ");
			String condition = fieldInfo.length == 1 ? null : fieldInfo[0];
			String fieldName = fieldInfo.length == 1 ? fieldInfo[0] : fieldInfo[1];

			if (filterValue.getValue() == null) {
				continue;
			}

			if (filterValue.getValue() instanceof Date) {
				filterValue.setValue(((Date) filterValue.getValue()).getTime());
			}
			
			String strPattern = filterValue.getValue().toString().replace("*", ".*");
			Pattern pattern = Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE);

			if (cfValuesAsValues.get(fieldName) == null) {
				if (isStoreAsTable) {
					return false;
				}

				if (fieldName.equals("code")) {
					Matcher matcher = pattern.matcher(customEntityInstance.getCode());
					return matcher.matches();
				}

				List<String> keys = new ArrayList<>(cfValuesAsValues.keySet());
				for (String key : keys) {
					if (key.equalsIgnoreCase(fieldName)) {
						cfValuesAsValues.put(fieldName, cfValuesAsValues.get(key));
					}
				}
			}

			Object referenceValue = cfValuesAsValues.get(fieldName);

			if (referenceValue instanceof Instant) {
				referenceValue = Date.from((Instant) referenceValue).getTime();
			} else if (referenceValue instanceof Date) {
				referenceValue = ((Date) referenceValue).getTime();
			}
			if ("fromRange".equals(condition)) {
				if (new BigDecimal(referenceValue.toString()).compareTo(new BigDecimal(filterValue.getValue().toString())) < 0) {
					return false;
				}

			} else if ("toRange".equals(condition)) {
				if (new BigDecimal(referenceValue.toString()).compareTo(new BigDecimal(filterValue.getValue().toString())) > 0) {
					return false;
				}

			} else {
				if (strPattern instanceof String) {
					Matcher matcher = pattern.matcher(referenceValue.toString());
					if (!matcher.matches()) {
						return false;
					}

				} else {
					if (!referenceValue.equals(pattern)) {
						return false;
					}
				}
			}
		}

		return true;
	}

	private void filterValues(CustomEntityInstance cei) {
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

	public boolean checkBeforeUpdate(Repository repository, CustomEntityInstance entity) throws EntityDoesNotExistsException {
		if (entity.getCet().isStoreAsTable()) {
			Map<String, Object> values = customTableService.findById(repository.getSqlConfigurationCode(), entity.getCet(), entity.getUuid());
			if (values != null) {
				for (String key: values.keySet()) {
					CustomFieldTemplate customFieldTemplate = customFieldTemplateService.findByCode(key);
					if (customFieldTemplate != null && customFieldTemplate.getFieldType().equals(CustomFieldTypeEnum.ENTITY) && customFieldTemplate.getStorageType().equals(CustomFieldStorageTypeEnum.LIST) && customFieldTemplate.getEntityClazz().equals("org.meveo.model.wf.Workflow")) {
						List<String> valueWF = (List<String>) values.get(key);
						if (CollectionUtils.isNotEmpty(valueWF)) {
							return transitionsFromPreviousState(entity);
						} else {
							return true;
						}
					}
				}
			}
		} else {
			CustomEntityInstance customEntityInstance = findByUuid(entity.getCetCode(), entity.getUuid());
			if (customEntityInstance.getCfValues() != null) {
				for (String key : customEntityInstance.getCfValues().getValuesByCode().keySet()) {
					CustomFieldTemplate customFieldTemplate = customFieldTemplateService.findByCode(key);
					if (customFieldTemplate != null && customFieldTemplate.getFieldType().equals(CustomFieldTypeEnum.ENTITY) && customFieldTemplate.getStorageType().equals(CustomFieldStorageTypeEnum.LIST) && customFieldTemplate.getEntityClazz().equals("org.meveo.model.wf.Workflow")) {
						List<CustomFieldValue> customFieldValues = customEntityInstance.getCfValues().getValuesByCode().get(key);
						if (CollectionUtils.isNotEmpty(customFieldValues)) {
							return transitionsFromPreviousState(entity);
						} else {
							return true;
						}
						}
					}
				}
			}
		return true;
	}

	private boolean transitionsFromPreviousState(CustomEntityInstance instance) {
		if (instance.getCfValues() != null && instance.getCfValues().getValuesByCode() != null) {
			for (String key : instance.getCfValues().getValuesByCode().keySet()) {
				CustomFieldTemplate customFieldTemplate = customFieldTemplateService.findByCode(key);
				if (customFieldTemplate != null && customFieldTemplate.getFieldType().equals(CustomFieldTypeEnum.ENTITY) && customFieldTemplate.getStorageType().equals(CustomFieldStorageTypeEnum.LIST) && customFieldTemplate.getEntityClazz().equals("org.meveo.model.wf.Workflow")) {
					List<CustomFieldValue> customFieldValues = instance.getCfValues().getValuesByCode().get(key);
					if (CollectionUtils.isNotEmpty(customFieldValues)) {
						for (CustomFieldValue customFieldValue : customFieldValues) {
							if (CollectionUtils.isNotEmpty(customFieldValue.getListValue())) {
								List<Workflow> workflows = new ArrayList<>();
								for (Object object : customFieldValue.getListValue()) {
									if (object instanceof EntityReferenceWrapper) {
										Workflow workflow = (Workflow) getEntityManager().createNamedQuery("Workflow.findByUUID").setParameter("uuid", ((EntityReferenceWrapper) object).getUuid()).getSingleResult();
										workflows.add(workflow);
									}
								}
								if (CollectionUtils.isNotEmpty(workflows)) {
									for (int i = 0; i < workflows.size(); i++) {
										List<WFTransition> wfTransitions = workflows.get(i).getTransitions();
										List<String> statusWF = new ArrayList<>();
										if (CollectionUtils.isNotEmpty(wfTransitions)) {
											for (WFTransition wfTransition : wfTransitions) {
												statusWF.add(wfTransition.getFromStatus());
											}
											if (CollectionUtils.isNotEmpty(statusWF) && i + 1 < workflows.size()) {
												if (!statusWF.contains(workflows.get(i + 1).getCode())) {
													return false;
												}
											}
										}
									}
								}
							}
							}
						}
					} else {
						return true;
					}
				}
			}
		return true;
	}
}