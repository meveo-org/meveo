package org.meveo.service.custom;

import java.io.IOException;
import java.util.List;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.model.customEntities.CustomEntityInstanceAudit;
import org.meveo.model.customEntities.CustomEntityInstanceAuditParameter;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.service.storage.RepositoryService;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.11.0
 */
@Stateless
public class CustomEntityInstanceAuditWriterService {
	
	@Inject
	private RepositoryService repositoryService;

	@Inject
	private CustomTableService customTableService;

	public void writeChanges(CustomEntityInstanceAuditParameter param, List<CustomEntityInstanceAudit> result)
			throws BusinessException, BusinessApiException, EntityDoesNotExistsException, IOException {

		String tableName = CustomEntityTemplate.AUDIT_PREFIX + param.getCetCode();

		for (CustomEntityInstanceAudit audit : result) {
			customTableService.create(repositoryService.findDefaultRepository().getCode(), tableName, audit.toMap(), false);
		}
	}
}
