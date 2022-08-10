package org.meveo.service.admin.impl;

import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.module.ModuleReleaseDto;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.ModuleRelease;
import org.meveo.model.persistence.JacksonUtil;

public class MeveoModuleUtils {

	public static MeveoModuleDto moduleSourceToDto(MeveoModule module) {
		return JacksonUtil.fromString(module.getModuleSource(), MeveoModuleDto.class);
	}

	public static ModuleReleaseDto moduleSourceToDto(ModuleRelease moduleRelease) {
		return JacksonUtil.fromString(moduleRelease.getModuleSource(), ModuleReleaseDto.class);
	}
}
