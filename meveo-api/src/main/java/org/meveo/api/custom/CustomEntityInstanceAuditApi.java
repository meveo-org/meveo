package org.meveo.api.custom;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.api.BaseApi;
import org.meveo.api.dto.custom.CustomEntityInstanceAuditsResponseDto;
import org.meveo.api.dto.response.PagingAndFiltering;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityInstanceAudit;
import org.meveo.model.customEntities.CustomEntityInstanceAudit.CustomEntityInstanceAuditType;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.CEIUtils;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.service.custom.CustomEntityInstanceAuditService;
import org.primefaces.model.SortOrder;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.11.0
 */
@Stateless
public class CustomEntityInstanceAuditApi extends BaseApi {

	@Inject
	CustomEntityInstanceAuditService customEntityInstanceAuditService;

	public CustomEntityInstanceAuditsResponseDto list(String cet, String ceiUuid, PagingAndFiltering pagingAndFiltering) throws InvalidParameterException {

		if (pagingAndFiltering == null) {
			pagingAndFiltering = new PagingAndFiltering();
		}

		CustomEntityInstanceAuditsResponseDto result = new CustomEntityInstanceAuditsResponseDto();

		PaginationConfiguration paginationConfig = toPaginationConfiguration("cei_uuid", SortOrder.ASCENDING, null, pagingAndFiltering, CustomEntityInstanceAudit.class);

		paginationConfig.getFiltersNullSafe().put("cei_uuid", ceiUuid);

		List<Map<String, Object>> auditLogs = customEntityInstanceAuditService.list(SqlConfiguration.DEFAULT_SQL_CONNECTION, CustomEntityTemplate.AUDIT_PREFIX + cet,
				paginationConfig);
		
		result.setPaging(pagingAndFiltering);
		result.getPaging().setTotalNumberOfRecords(
				(int) customEntityInstanceAuditService.count(SqlConfiguration.DEFAULT_SQL_CONNECTION, CustomEntityTemplate.AUDIT_PREFIX + cet, paginationConfig));

		if (auditLogs != null) {
			auditLogs.forEach(e -> {
				CustomEntityInstance cei = CEIUtils.pojoToCei(e);
				CustomFieldValues cfValues = cei.getCfValues();
				
				CustomEntityInstanceAudit audit = new CustomEntityInstanceAudit();
				audit.setAction(CustomEntityInstanceAuditType.valueOf(e.get("action").toString()));
				audit.setCeiUuid(e.get("cei_uuid").toString());
				audit.setEventDate(cfValues.getCfValue("event_date").getDateValueOld());
				audit.setField(e.get("field").toString());
				audit.setUser(e.get("user").toString());
				if (e.containsKey("new_value")) {
					if (!StringUtils.isBlank(e.containsKey("new_value")) && e.get("new_value") != null) {
						audit.setNewValue(e.get("new_value").toString());
					}
				}
				if (e.containsKey("old_value")) {
					if (!StringUtils.isBlank(e.containsKey("old_value")) && e.get("old_value") != null) {
						audit.setOldValue(e.get("old_value").toString());
					}
				}
				result.getCustomEntityInstanceAuditdAudits().add(audit);
			});
		}

		return result;
	}
}
