/**
 * 
 */
package org.meveo.api;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.model.IEntity;
import org.meveo.service.base.EntitySerializer;

/**
 * Implementation of {@link EntitySerializer}
 * 
 * @author clement.bareth
 * @since 6.14.0
 * @version 6.14.0
 */
public class EntitySerializerImpl implements EntitySerializer {

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public BaseEntityDto serialize(IEntity<?> entity) throws BusinessException {
		var api = (BaseCrudApi) ApiUtils.getApiService(entity.getClass(), true);
		
		try {
			return api.toDto(entity);
		} catch (MeveoApiException e) {
			throw new BusinessException(e);
		}
	}

}
