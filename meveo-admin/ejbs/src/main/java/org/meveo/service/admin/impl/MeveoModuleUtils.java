package org.meveo.service.admin.impl;

import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.persistence.JacksonUtil;

public class MeveoModuleUtils {

	@SuppressWarnings("unchecked")
	public static MeveoModuleDto moduleSourceToDto(MeveoModule module) {
	    Class<? extends MeveoModuleDto> dtoClass = (Class<? extends MeveoModuleDto>) ReflectionUtils.getClassBySimpleNameAndParentClass(module.getClass().getSimpleName() + "Dto",
	        MeveoModuleDto.class);

		return JacksonUtil.fromString(module.getModuleSource(), dtoClass);
	}

}
