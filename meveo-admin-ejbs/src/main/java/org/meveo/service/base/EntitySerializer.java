/**
 * 
 */
package org.meveo.service.base;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.model.IEntity;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public interface EntitySerializer {

	BaseEntityDto serialize(IEntity<?> entity) throws BusinessException;
}
