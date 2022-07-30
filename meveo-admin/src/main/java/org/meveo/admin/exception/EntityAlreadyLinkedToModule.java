/**
 * 
 */
package org.meveo.admin.exception;

import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;

/**
 * 
 * @author ClementBareth
 * @since 
 * @version
 */
public class EntityAlreadyLinkedToModule extends BusinessException {

	public EntityAlreadyLinkedToModule(MeveoModuleItem meveoModuleItem, MeveoModule module) {
		super(meveoModuleItem.toString() + " already belongs to module " + module.getCode());
	}
}
