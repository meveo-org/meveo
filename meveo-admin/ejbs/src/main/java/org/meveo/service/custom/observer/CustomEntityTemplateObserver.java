package org.meveo.service.custom.observer;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.meveo.event.logging.LoggedEvent;
import org.meveo.event.qualifier.Created;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.service.custom.CustomTableCreatorService;
import org.meveo.service.storage.RepositoryService;

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
	private CustomTableCreatorService customTableCreatorService;

	@Inject
	private RepositoryService repositoryService;

	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void onCetCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created CustomEntityTemplate cet) {

		if (cet.isAudited()) {
			createAuditTable(repositoryService.findDefaultRepository().getCode(), "audit_" + SQLStorageConfiguration.getDbTablename(cet));
		}
	}

	private void createAuditTable(String sqlConnectionCode, String dbTableName) {

		// create table
		customTableCreatorService.createTable(sqlConnectionCode, dbTableName, false);

		// add fields

		// cei_uuid
		CustomFieldTemplate cft = createCft("cei_uuid", CustomFieldTypeEnum.STRING, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);

		// user
		cft = createCft("user", CustomFieldTypeEnum.STRING, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);

		// date
		cft = createCft("date", CustomFieldTypeEnum.DATE, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);

		// action
		cft = createCft("action", CustomFieldTypeEnum.STRING, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);
		
		// field
		cft = createCft("field", CustomFieldTypeEnum.STRING, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);
		
		// oldValue
		cft = createCft("old_value", CustomFieldTypeEnum.STRING, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);
		
		// newValue
		cft = createCft("new_value", CustomFieldTypeEnum.STRING, false, false);
		customTableCreatorService.addField(sqlConnectionCode, dbTableName, cft);
	}

	public CustomFieldTemplate createCft(String code, CustomFieldTypeEnum fieldType, boolean unique, boolean identifier) {

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
