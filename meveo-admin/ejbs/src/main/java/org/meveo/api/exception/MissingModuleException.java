/**
 * 
 */
package org.meveo.api.exception;

import java.util.List;
import java.util.stream.Collectors;

import org.meveo.api.MeveoApiErrorCodeEnum;
import org.meveo.api.dto.module.ModuleDependencyDto;

/**
 * 
 * @author ClementBareth
 * @since 
 * @version
 */
public class MissingModuleException extends EntityDoesNotExistsException {
	
	private List<ModuleDependencyDto> missingModules;
	
	public MissingModuleException(List<ModuleDependencyDto> missingModules) {
		super(buildMessage(missingModules));
		setErrorCode(MeveoApiErrorCodeEnum.ENTITY_DOES_NOT_EXISTS_EXCEPTION);
		this.missingModules = missingModules;
	}
	
	private static String buildMessage(List<ModuleDependencyDto> missingModules) {
		return missingModules.stream()
			.map(module -> module.getCode() + ":" + module.getCurrentVersion())
			.collect(Collectors.joining(", ", "The following required modules are not installed : ", ""));
	}

	/**
	 * @return the {@link #missingModules}
	 */
	public List<ModuleDependencyDto> getMissingModules() {
		return missingModules;
	}
	
}
