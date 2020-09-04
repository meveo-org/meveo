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
import org.meveo.elresolver.ELException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.storage.Repository;
import org.meveo.model.wf.WFAction;
import org.meveo.model.wf.WFTransition;
import org.meveo.model.wf.Workflow;
import org.meveo.service.base.BusinessService;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.crm.impl.CustomFieldInstanceService;

import com.ibm.icu.math.BigDecimal;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.wf.WFActionService;
import org.meveo.service.wf.WFTransitionService;
import org.meveo.service.wf.WorkflowService;

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

	@Inject
	private WorkflowService workflowService;

	@Inject
	private WFTransitionService wfTransitionService;

	@Inject
	private WFActionService wfActionService;

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

			if (filterValue.getValue() == null) {
				continue;
			}

			String[] fieldInfo = filterValue.getKey().split(" ");
			String condition = fieldInfo.length == 1 ? null : fieldInfo[0];
			String fieldName = fieldInfo.length == 1 ? fieldInfo[0] : fieldInfo[1];

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
				if (filterValue.getValue().toString().contains("*")) {
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
		}

		return true;
	}

	private void filterValues(CustomEntityInstance cei) {
		CustomEntityTemplate cet = cetCache.getCustomEntityTemplate(cei.getCetCode());
		CustomFieldValues cfValues = cei.getCfValues();
		if (cfValues != null && cfValues.getValuesByCode() != null) {
			for (String valueCode : new HashSet<>(cfValues.getValuesByCode().keySet())) {
				CustomFieldTemplate cft = cetCache.getCustomFieldTemplate(valueCode, cet.getAppliesTo());
				if (cft != null && (cft.getStoragesNullSafe() == null || !cft.getStoragesNullSafe().contains(DBStorageType.SQL))) {
					cfValues.removeValue(valueCode);
				}
			}
		}
	}

	public boolean checkBeforeUpdate(Repository repository, CustomEntityInstance entity) throws EntityDoesNotExistsException, ELException {
		Map<String, Set<String>> map = getValueCetCodeAndWfTypeFromWF();
		if (entity.getCet().isStoreAsTable()) {
			Map<String, Object> values = customTableService.findById(repository.getSqlConfigurationCode(), entity.getCet(), entity.getUuid());
			if (values != null) {
				if (map.isEmpty()) {
					return true;
				}
				for (String key : values.keySet()) {
					if (map.keySet().contains(entity.getCetCode()) && map.get(entity.getCetCode()).contains(key)) {
						if (values.get(key).equals(entity.getCfValues().getValuesByCode().get(key).get(0).getStringValue())) {
							continue;
						} else {
							if (transitionsFromPreviousState(key, entity)) {
								continue;
							} else {
								return false;
							}
						}
					}
				}
			}
		} else {
			CustomEntityInstance customEntityInstance = findByUuid(entity.getCetCode(), entity.getUuid());
			if (customEntityInstance != null && customEntityInstance.getCfValues() != null) {
				if (map.isEmpty()) {
					return true;
				}
				for (String key : customEntityInstance.getCfValues().getValuesByCode().keySet()) {
					if (map.keySet().contains(entity.getCetCode()) && map.get(entity.getCetCode()).contains(key)) {
						if (customEntityInstance.getCfValues().getValuesByCode().get(key).get(0).getStringValue().equals(entity.getCfValues().getValuesByCode().get(key).get(0).getStringValue())) {
							continue;
						} else {
							if (transitionsFromPreviousState(key, entity)) {
								continue;
							} else {
								return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	private boolean transitionsFromPreviousState(String cftCode, CustomEntityInstance instance) throws ELException {
		List<Workflow> workflows = workflowService.findByCetCodeAndWFType(instance.getCetCode(), cftCode);
		CustomFieldTemplate customFieldTemplate = customFieldTemplateService.findByCodeAndAppliesTo(cftCode, "CE_" + instance.getCetCode());
		if (CollectionUtils.isNotEmpty(workflows)) {
			List<String> conditionEls = new ArrayList<>();
			List<WFTransition> transitions = new ArrayList<>();
			List<String> statusWF = new ArrayList<>();
			for (Workflow workflow: workflows) {
				List<WFTransition> wfTransitions = workflow.getTransitions();
				if (CollectionUtils.isNotEmpty(wfTransitions)) {
					for (WFTransition wfTransition : wfTransitions) {
						wfTransition = wfTransitionService.findById(wfTransition.getId());
						transitions.add(wfTransition);
						statusWF.add(wfTransition.getToStatus());
						if (wfTransition.getConditionEl() != null) {
							conditionEls.add(wfTransition.getConditionEl());
						}
					}
				}
			}
			if (CollectionUtils.isNotEmpty(statusWF)) {
				if (!statusWF.contains(instance.getCfValues().getValuesByCode().get(cftCode).get(0).getStringValue())) {
					return false;
				}
			}
			if (customFieldTemplate.getApplicableOnEl() != null && CollectionUtils.isNotEmpty(transitions)) {
				if (!CollectionUtils.isNotEmpty(conditionEls) || !conditionEls.contains(customFieldTemplate.getApplicableOnEl())) {
					return false;
				} else {
					for (WFTransition wfTransition : transitions) {
						if (wfTransition.getConditionEl() != null && MeveoValueExpressionWrapper.evaluateToBooleanOneVariable(wfTransition.getConditionEl(), "entity", instance) && CollectionUtils.isNotEmpty(wfTransition.getWfActions())) {
							for (WFAction action : wfTransition.getWfActions()) {
								action = wfActionService.findById(action.getId());
								if (action.getConditionEl() != null && MeveoValueExpressionWrapper.evaluateToBooleanOneVariable(action.getConditionEl(), "entity", instance)) {
									workflowService.executeExpression(action.getActionEl(), instance);
								}
							}
						}
					}
				}
			}
		}
		return true;
	}

    /**
     *  Returns a list of states for a given CET.
     */
	public Map<String, List<String>> statesOfCET(String cetCode) {
		Map<String, List<String>> states = new HashMap<>();
		Map<String, Set<String>> map = getValueCetCodeAndWfTypeFromWF();
		if (!map.isEmpty()) {
			Set<String> cftCodes = map.get(cetCode);
			if (CollectionUtils.isNotEmpty(cftCodes)) {
				for (String code: cftCodes) {
					CustomFieldTemplate customFieldTemplate = customFieldTemplateService.findByCodeAndAppliesTo(code, "CE_" + cetCode);
					if (customFieldTemplate != null) {
						List<String> values = new ArrayList<>();
						values.addAll(customFieldTemplate.getListValues().keySet());
						states.put(code, values);
					}
				}
			}
		}
		return states;
	}

    /**
     *  Returns the target states from a origin state of a given CEI where applicationEL evaluates to true.
     */
	public List<String> targetStates(CustomEntityInstance cei) throws ELException {
		List<String> targetStates = new ArrayList<>();
		Map<String, Set<String>> map = getValueCetCodeAndWfTypeFromWF();
		if (cei.getCfValues() != null && cei.getCfValues().getValuesByCode() != null) {
			for (String key : cei.getCfValues().getValuesByCode().keySet()) {
				CustomFieldTemplate customFieldTemplate = customFieldTemplateService.findByCodeAndAppliesTo(key, "CE_" + cei.getCetCode());
				if (customFieldTemplate != null && !map.isEmpty() && map.keySet().contains(cei.getCetCode()) && map.values().contains(customFieldTemplate.getCode())
						&& MeveoValueExpressionWrapper.evaluateToBooleanOneVariable(customFieldTemplate.getApplicableOnEl(), "entity", cei)) {
					List<Workflow> workflows = workflowService.findByCetCodeAndWFType(cei.getCetCode(), customFieldTemplate.getCode());
					if (CollectionUtils.isNotEmpty(workflows)) {
						for (Workflow workflow: workflows) {
							if (CollectionUtils.isNotEmpty(workflow.getTransitions())) {
								for (WFTransition wfTransition: workflow.getTransitions()) {
									targetStates.add(wfTransition.getToStatus());
								}
							}
						}
					}
				}
			}
		}
		return targetStates;
	}

	private Map<String, Set<String>> getValueCetCodeAndWfTypeFromWF () {
		List<Workflow> workflowList = workflowService.list();
		Map<String, Set<String>> map = new HashMap<>();
		if (CollectionUtils.isNotEmpty(workflowList)) {
			for (Workflow workflow: workflowList) {
				Set<String> wfTypes = new HashSet<>();
				if (map.keySet().contains(workflow.getCetCode())) {
					Set<String> types = map.get(workflow.getCetCode());
					for (String type: types) {
						wfTypes.add(type);
					}
				}
				wfTypes.add(workflow.getWfType());
				map.put(workflow.getCetCode(), wfTypes);
			}
		}
		return map;
	}
}