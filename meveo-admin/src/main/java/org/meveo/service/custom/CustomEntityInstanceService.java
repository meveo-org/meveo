package org.meveo.service.custom;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.ejb.Stateless;
import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.jboss.weld.contexts.ContextNotActiveException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.listener.CommitMessageBean;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.elresolver.ELException;
import org.meveo.model.ModuleItem;
import org.meveo.model.ModulePostUninstall;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.EntityReferenceWrapper;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.model.wf.WFAction;
import org.meveo.model.wf.WFTransition;
import org.meveo.model.wf.Workflow;
import org.meveo.service.admin.impl.MeveoModuleHelper;
import org.meveo.service.admin.impl.ModuleUninstall;
import org.meveo.service.base.BusinessService;
import org.meveo.service.base.MeveoValueExpressionWrapper;
import org.meveo.service.crm.impl.CustomFieldInstanceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.git.GitHelper;
import org.meveo.service.wf.WFActionService;
import org.meveo.service.wf.WFTransitionService;
import org.meveo.service.wf.WorkflowService;

import com.ibm.icu.math.BigDecimal;

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

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	@Inject
	CommitMessageBean commitMessageBean;

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

		var ownValues = new HashMap<>(values);
		for (var entry : values.entrySet()) {
			if (entry.getValue() instanceof EntityReferenceWrapper) {
				ownValues.remove(entry.getKey());
			}
		}

		if (ownValues != null && !ownValues.isEmpty()) {
			return resultList.stream()
					.filter(customEntityInstance -> filterOnValues(ownValues, customEntityInstance))
					.collect(Collectors.toList());
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
			return resultList.stream()
					.filter(customEntityInstance -> filterOnValues(values, customEntityInstance, isStoreAsTable))
					.collect(Collectors.toList());
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

			if(filterValue.getValue() instanceof Collection) {
				continue; //FIXME
			}

			String[] fieldInfo = filterValue.getKey().split(" ");
			String condition = fieldInfo.length == 1 ? null : fieldInfo[0];
			String fieldName = fieldInfo.length == 1 ? fieldInfo[0] : fieldInfo[1];

			if (filterValue.getValue() instanceof Date) {
				filterValue.setValue(((Date) filterValue.getValue()).getTime());
			}

			Pattern pattern;
			if (filterValue.getValue().toString().contains("*")) {
				String strPattern = filterValue.getValue().toString().replace("*", ".*");
				pattern = Pattern.compile(strPattern, Pattern.CASE_INSENSITIVE);
			} else {
				pattern = Pattern.compile(filterValue.getValue().toString(), Pattern.LITERAL);
			}


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
					Matcher matcher = pattern.matcher(referenceValue.toString());
					if (!matcher.matches()) {
						return false;
					}
				} else {
					return filterValue.getValue().equals(referenceValue);
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

	public boolean transitionsFromPreviousState(String cftCode, CustomEntityInstance instance) throws ELException {
		Workflow workflow = workflowService.findByCetCodeAndWFType(instance.getCetCode(), cftCode);
		if (workflow != null) {
			List<WFTransition> transitions = new ArrayList<>();
			List<String> statusWF = new ArrayList<>();
			List<WFTransition> wfTransitions = workflow.getTransitions();
			if (CollectionUtils.isNotEmpty(wfTransitions)) {
				for (WFTransition wfTransition : wfTransitions) {
					wfTransition = wfTransitionService.findById(wfTransition.getId());

					boolean isTransitionApplicable = MeveoValueExpressionWrapper.evaluateToBooleanOneVariable(wfTransition.getConditionEl(), "entity", instance);
					String targetStatus = instance.getCfValues().getValuesByCode().get(cftCode).get(0).getStringValue();
					String startStatus = (String) instance.getCfValuesOldNullSafe().getValue(cftCode);

					boolean isSameTargetStatus = wfTransition.getToStatus().equals(targetStatus);
					boolean isSameStartStatus = wfTransition.getFromStatus().equals(startStatus);
					if(isTransitionApplicable && isSameTargetStatus && isSameStartStatus) {
						transitions.add(wfTransition);
						statusWF.add(wfTransition.getToStatus());
					}

				}
			}

			if (CollectionUtils.isEmpty(transitions)) {
				log.debug("Update refused because no transition matched");
				return false;
			}

			for (WFTransition wfTransition : transitions) {
				if (CollectionUtils.isNotEmpty(wfTransition.getWfActions())) {
					for (WFAction action : wfTransition.getWfActions()) {
						WFAction wfAction = wfActionService.findById(action.getId());
						if (action.getConditionEl() == null || MeveoValueExpressionWrapper.evaluateToBooleanOneVariable(action.getConditionEl(), "entity", instance)) {
							Object actionResult;

							if (wfAction.getActionScript() != null) {
								try {
									actionResult = workflowService.executeActionScript(instance, wfAction);
								} catch (BusinessException e) {
									log.error("Error execution workflow action script", e);
								}
							} else if (StringUtils.isNotBlank(wfAction.getActionEl())) {
								actionResult = workflowService.executeExpression(wfAction.getActionEl(), instance);
							} else {
								log.error("WFAction {} has no action EL or action script", wfAction.getId());
								continue;
							}

							//TODO: Log action result ?
						}
					}
				}
			}
		}

		return true;
	}

	/**
	 *  Returns a list of states for a given CEI.
	 */
	public List<String> statesOfCEI(String cetCode, String cftCode, String uuid) throws BusinessException, ELException, EntityDoesNotExistsException {
		List<String> states = new ArrayList<>();
		CustomEntityTemplate customEntityTemplate = customEntityTemplateService.findByCode(cetCode);
		if (customEntityTemplate != null) {
			Workflow workflow = workflowService.findByCetCodeAndWFType(cetCode, cftCode);
			if (workflow != null) {
				String originStatus = null;
				if (customEntityTemplate.isStoreAsTable()) {
					Map<String, Object> ceiMap = customTableService.findById(SqlConfiguration.DEFAULT_SQL_CONNECTION, customEntityTemplate, uuid);
					if (ceiMap != null) {
						CustomEntityInstance customEntityInstance = new CustomEntityInstance();
						customEntityInstance.setCetCode(cetCode);
						customEntityInstance.setCet(customEntityTemplate);
						customEntityInstance.setUuid(uuid);
						customFieldInstanceService.setCfValues(customEntityInstance, cetCode, ceiMap);
						originStatus = (String) ceiMap.get(cftCode);
						if (CollectionUtils.isNotEmpty(workflow.getTransitions())) {
							for (WFTransition wfTransition : workflow.getTransitions()) {
								if (wfTransition != null && wfTransition.getFromStatus().equals(originStatus) && MeveoValueExpressionWrapper.evaluateToBooleanOneVariable(wfTransition.getConditionEl(), "entity", customEntityInstance)) {
									states.add(wfTransition.getToStatus());
								}
							}
						}
					}
				} else {
					CustomEntityInstance customEntityInstance = findByUuid(cetCode, uuid);
					if (customEntityInstance != null) {
						originStatus = (String) customEntityInstance.getCfValues().getValue(cftCode);
						if (CollectionUtils.isNotEmpty(workflow.getTransitions())) {
							for (WFTransition wfTransition : workflow.getTransitions()) {
								if (wfTransition != null && wfTransition.getFromStatus().equals(originStatus) && MeveoValueExpressionWrapper.evaluateToBooleanOneVariable(wfTransition.getConditionEl(), "entity", customEntityInstance)) {
									states.add(wfTransition.getToStatus());
								}
							}
						}
					}
				}
			}
		}
		return states;
	}

	/**
	 *  Returns the target states from a origin state of a given CEI where applicationEL evaluates to true.
	 */
	public List<String> targetStates (CustomEntityInstance cei) throws ELException {
		List<String> targetStates = new ArrayList<>();
		Map<String, Set<String>> map = getValueCetCodeAndWfTypeFromWF();
		if (cei.getCfValues() != null && cei.getCfValues().getValuesByCode() != null) {
			for (String key : cei.getCfValues().getValuesByCode().keySet()) {
				CustomFieldTemplate customFieldTemplate = customFieldTemplateService.findByCodeAndAppliesTo(key, "CE_" + cei.getCetCode());
				if (customFieldTemplate != null && !map.isEmpty() && map.keySet().contains(cei.getCetCode()) && map.values().contains(customFieldTemplate.getCode())
						&& MeveoValueExpressionWrapper.evaluateToBooleanOneVariable(customFieldTemplate.getApplicableOnEl(), "entity", cei)) {
					Workflow workflow = workflowService.findByCetCodeAndWFType(cei.getCetCode(), customFieldTemplate.getCode());
					if (CollectionUtils.isNotEmpty(workflow.getTransitions())) {
						for (WFTransition wfTransition : workflow.getTransitions()) {
							targetStates.add(wfTransition.getToStatus());
						}
					}
				}
			}
		}
		return targetStates;
	}

	public Map<String, Set<String>> getValueCetCodeAndWfTypeFromWF () {
		List<Workflow> workflowList = workflowService.list();
		Map<String, Set<String>> map = new HashMap<>();
		if (CollectionUtils.isNotEmpty(workflowList)) {
			for (Workflow workflow : workflowList) {
				Set<String> wfTypes = new HashSet<>();
				if (map.keySet().contains(workflow.getCetCode())) {
					Set<String> types = map.get(workflow.getCetCode());
					for (String type : types) {
						wfTypes.add(type);
					}
				}
				wfTypes.add(workflow.getWfType());
				map.put(workflow.getCetCode(), wfTypes);
			}
		}
		return map;
	}

	@Override
	public void addFilesToModule(CustomEntityInstance entity, MeveoModule module) throws BusinessException {
		String cetCode = entity.getCetCode();
		String ceiJson = CEIUtils.serialize(entity);

		File gitDirectory = GitHelper.getRepositoryDir(currentUser, module.getCode());
		String path = entity.getClass().getAnnotation(ModuleItem.class).path() + "/" + cetCode;
		File newDir = new File(gitDirectory, path);
		newDir.mkdirs();

		File newJsonFile = new File(gitDirectory, path + "/" + entity.getUuid() + ".json");
		try {
			MeveoFileUtils.writeAndPreserveCharset(ceiJson, newJsonFile);
		} catch (IOException e) {
			throw new BusinessException("File cannot be updated or created", e);
		}

		String message = "Add JSON file for entity " + entity.getCode();
		try {
			message+=" "+commitMessageBean.getCommitMessage();
		} catch (ContextNotActiveException e) {
			log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
		}
		gitClient.commitFiles(module.getGitRepository(), Collections.singletonList(newDir), message);
	}

}