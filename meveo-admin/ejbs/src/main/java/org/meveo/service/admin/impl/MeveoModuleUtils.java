package org.meveo.service.admin.impl;

import java.io.StringReader;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.module.MeveoModule;

public class MeveoModuleUtils {

	@SuppressWarnings("unchecked")
	public static MeveoModuleDto moduleSourceToDto(MeveoModule module) throws JAXBException {
	    Class<? extends MeveoModuleDto> dtoClass = (Class<? extends MeveoModuleDto>) ReflectionUtils.getClassBySimpleNameAndParentClass(module.getClass().getSimpleName() + "Dto",
	        MeveoModuleDto.class);
	
	    MeveoModuleDto moduleDto = (MeveoModuleDto) JAXBContext.newInstance(dtoClass).createUnmarshaller().unmarshal(new StringReader(module.getModuleSource()));
	
	    return moduleDto;
	}

}
