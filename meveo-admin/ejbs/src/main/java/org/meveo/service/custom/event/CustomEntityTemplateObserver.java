package org.meveo.service.custom.event;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.meveo.commons.utils.StringUtils;
import org.meveo.event.logging.LoggedEvent;
import org.meveo.event.qualifier.CreatedAfterTx;
import org.meveo.event.qualifier.PostRemoved;
import org.meveo.event.qualifier.UpdatedAfterTx;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.model.sql.SqlConfiguration;
import org.meveo.service.custom.CustomTableCreatorService;
import org.meveo.service.storage.RepositoryService;
import org.slf4j.Logger;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.11.0
 */
@Singleton
@Startup
@LoggedEvent
@Lock(LockType.READ)
public class CustomEntityTemplateObserver {

	@Inject
	private Logger log;

	@Inject
	private CustomTableCreatorService customTableCreatorService;

	@Inject
	private RepositoryService repositoryService;
	
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onCetDeleted(@Observes(during = TransactionPhase.AFTER_SUCCESS) @PostRemoved CustomEntityTemplate cet) {

		log.debug("CET onDeleted observer={}", cet);
		if (cet.isAudited()) {
			customTableCreatorService.removeTable(repositoryService.findDefaultRepository().getCode(),
					CustomEntityTemplate.AUDIT_PREFIX + SQLStorageConfiguration.getDbTablename(cet));
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onCetCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @CreatedAfterTx CustomEntityTemplate cet) {

		log.debug("CET onCreated observer={}", cet);
		if (cet.isAudited()) {
			createAuditTable(repositoryService.findDefaultRepository().getCode(), CustomEntityTemplate.AUDIT_PREFIX + SQLStorageConfiguration.getDbTablename(cet));
		}
	}

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onCetUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @UpdatedAfterTx CustomEntityTemplate cet) {

		log.debug("CET onUpdated observer={}", cet);
		SqlConfiguration sqlConfiguration = repositoryService.findDefaultRepository().getSqlConfiguration();
		String tableName = CustomEntityTemplate.AUDIT_PREFIX + SQLStorageConfiguration.getDbTablename(cet);
		String schema = StringUtils.isBlank(sqlConfiguration.getSchema()) ? "public" : sqlConfiguration.getSchema();
		String sqlConnCode = repositoryService.findDefaultRepository().getSqlConfigurationCode();
		boolean isTableExists = customTableCreatorService.isTableExists(sqlConnCode, schema, tableName);

		if (cet.isAudited() && !isTableExists) {
			createAuditTable(sqlConnCode, tableName);

		}
	}

	private void createAuditTable(String sqlConnectionCode, String dbTableName) {

		// create table
		CustomEntityTemplate template = new CustomEntityTemplate();
		template.setCode(dbTableName);
		customTableCreatorService.createTable(sqlConnectionCode, template, false);

		// add fields

		// cei_uuid
		CustomFieldTemplate cft = createCft("cei_uuid", CustomFieldTypeEnum.STRING, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);

		// user
		cft = createCft("user", CustomFieldTypeEnum.STRING, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);

		// date
		cft = createCft("event_date", CustomFieldTypeEnum.DATE, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);

		// action
		cft = createCft("action", CustomFieldTypeEnum.STRING, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);

		// field
		cft = createCft("field", CustomFieldTypeEnum.STRING, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);

		// oldValue
		cft = createCft("old_value", CustomFieldTypeEnum.LONG_TEXT, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);

		// newValue
		cft = createCft("new_value", CustomFieldTypeEnum.LONG_TEXT, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);
	}

	private CustomFieldTemplate createCft(String code, CustomFieldTypeEnum fieldType, boolean unique, boolean identifier) {

		CustomFieldTemplate cft = new CustomFieldTemplate();
		cft.setSummary(true);
		cft.setCode(code);
		cft.setFieldType(fieldType);
		cft.setUnique(unique);
		cft.setIdentifier(identifier);
		cft.getStoragesNullSafe().add(DBStorageType.SQL);

		return cft;
	}
}
