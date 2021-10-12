package org.meveo.service.custom;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstanceAudit;
import org.meveo.model.customEntities.CustomEntityInstanceAudit.CustomEntityInstanceAuditType;
import org.meveo.model.customEntities.CustomEntityInstanceAuditParameter;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.base.NativePersistenceService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.11.0
 */
@Stateless
public class CustomEntityInstanceAuditService extends NativePersistenceService {

	@Inject
	@CurrentUser
	protected MeveoUser currentUser;

	@Inject
	private CustomEntityInstanceAuditWriterService customEntityInstanceAuditWriterService;

	@Inject
	private CustomFieldTemplateService customFieldTemplateService;

	public void auditChanges(CustomEntityInstanceAuditParameter param) throws BusinessException, BusinessApiException, EntityDoesNotExistsException, IOException {

		List<CustomEntityInstanceAudit> result = computeDifference(param.getCeiUuid(), param.getAppliesTo(), param.getOldValues(), param.getNewValues());
		customEntityInstanceAuditWriterService.writeChanges(param, result);
	}

	private List<CustomEntityInstanceAudit> computeDifference(String ceiUuid, String appliesTo, CustomFieldValues oldValues, CustomFieldValues newValues) {

		List<CustomEntityInstanceAudit> result = new ArrayList<>();

		if (oldValues == null) {
			oldValues = new CustomFieldValues();
		}

		if (newValues == null) {
			newValues = new CustomFieldValues();
		}

		result.addAll(computeCreatedFields(ceiUuid, appliesTo, oldValues, newValues));
		result.addAll(computeUpdatedFields(ceiUuid, appliesTo, oldValues, newValues));
		result.addAll(computeRemovedFields(ceiUuid, appliesTo, oldValues, newValues));

		return result;
	}

	private List<CustomEntityInstanceAudit> computeCreatedFields(String ceiUuid, String appliesTo, final CustomFieldValues oldValues, final CustomFieldValues newValues) {

		List<CustomEntityInstanceAudit> result = new ArrayList<>();

		List<Entry<String, Object>> processedFields = new ArrayList<>();
		newValues.getValues().entrySet().forEach(e -> {
			Optional<Entry<String, Object>> matchFound = oldValues.getValues().entrySet().stream()
					.filter(f -> !Objects.isNull(e.getValue()) && Objects.isNull(f.getValue()) && e.getKey().equals(f.getKey())).findFirst();

			if (matchFound.isPresent()) {
				processedFields.add(matchFound.get());
			}
		});

		processedFields.forEach(e -> {
			CustomFieldTemplate cft = customFieldTemplateService.findByCodeAndAppliesTo(e.getKey(), appliesTo);
			if (cft != null && cft.isAudited()) {
				CustomEntityInstanceAudit ceiAudit = new CustomEntityInstanceAudit();
				ceiAudit.setAction(CustomEntityInstanceAuditType.CREATED);
				ceiAudit.setCeiUuid(ceiUuid);
				ceiAudit.setEventDate(new Date());
				ceiAudit.setField(e.getKey());
				ceiAudit.setNewValue(newValues.getCfValue(e.getKey()).getValue());
				ceiAudit.setOldValue(oldValues.getCfValue(e.getKey()).getValue());
				ceiAudit.setUser(currentUser.getUserName());

				result.add(ceiAudit);
			}
		});

		return result;
	}

	private List<CustomEntityInstanceAudit> computeUpdatedFields(String ceiUuid, String appliesTo, final CustomFieldValues oldValues, final CustomFieldValues newValues) {

		List<CustomEntityInstanceAudit> result = new ArrayList<>();

		List<Entry<String, Object>> processedFields = new ArrayList<>();
		newValues.getValues().entrySet().forEach(e -> {
			Optional<Entry<String, Object>> matchFound = oldValues.getValues().entrySet().stream()
					.filter(f -> !Objects.isNull(e.getValue()) && !Objects.isNull(f.getValue()) && !e.getValue().equals(f.getValue()) && e.getKey().equals(f.getKey())).findFirst();

			if (matchFound.isPresent()) {
				processedFields.add(matchFound.get());
			}
		});

		processedFields.forEach(e -> {
			CustomFieldTemplate cft = customFieldTemplateService.findByCodeAndAppliesTo(e.getKey(), appliesTo);
			if (cft != null && cft.isAudited()) {
				CustomEntityInstanceAudit ceiAudit = new CustomEntityInstanceAudit();
				ceiAudit.setAction(CustomEntityInstanceAuditType.UPDATED);
				ceiAudit.setCeiUuid(ceiUuid);
				ceiAudit.setEventDate(new Date());
				ceiAudit.setField(e.getKey());
				ceiAudit.setNewValue(newValues.getCfValue(e.getKey()).getValue());
				ceiAudit.setOldValue(oldValues.getCfValue(e.getKey()).getValue());
				ceiAudit.setUser(currentUser.getUserName());

				result.add(ceiAudit);
			}
		});

		return result;
	}

	private List<CustomEntityInstanceAudit> computeRemovedFields(String ceiUuid, String appliesTo, final CustomFieldValues oldValues, final CustomFieldValues newValues) {

		List<CustomEntityInstanceAudit> result = new ArrayList<>();

		List<Entry<String, Object>> processedFields = new ArrayList<>();
		oldValues.getValues().entrySet().forEach(e -> {
			Optional<Entry<String, Object>> matchFound = newValues.getValues().entrySet().stream()
					.filter(f -> Objects.isNull(f.getValue()) && !Objects.isNull(e.getValue()) && e.getKey().equals(f.getKey())).findFirst();

			if (matchFound.isPresent()) {
				processedFields.add(matchFound.get());
			}
		});

		processedFields.forEach(e -> {
			CustomFieldTemplate cft = customFieldTemplateService.findByCodeAndAppliesTo(e.getKey(), appliesTo);
			if (cft != null && cft.isAudited()) {
				CustomEntityInstanceAudit ceiAudit = new CustomEntityInstanceAudit();
				ceiAudit.setAction(CustomEntityInstanceAuditType.REMOVED);
				ceiAudit.setCeiUuid(ceiUuid);
				ceiAudit.setEventDate(new Date());
				ceiAudit.setField(e.getKey());
				ceiAudit.setOldValue(oldValues.getCfValue(e.getKey()).getValue());
				ceiAudit.setUser(currentUser.getUserName());

				result.add(ceiAudit);
			}
		});

		return result;
	}
}
