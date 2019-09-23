package org.meveo.service.admin.impl;

import javax.ejb.Stateless;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.service.base.PersistenceService;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 */
@Stateless
public class MeveoModuleItemService extends PersistenceService<MeveoModuleItem> {

	@Override
	public void create(MeveoModuleItem entity) throws BusinessException {
		super.create(entity);
	}
}
