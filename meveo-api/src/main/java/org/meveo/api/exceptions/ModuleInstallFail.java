/**
 * 
 */
package org.meveo.api.exceptions;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.module.ModuleInstallResult;
import org.meveo.model.module.MeveoModule;

/**
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
public class ModuleInstallFail extends BusinessException {
	
	private MeveoModule module;
	private ModuleInstallResult result;
	private BusinessException exception;

	public ModuleInstallFail(MeveoModule module, ModuleInstallResult result, Exception e) {
		this.module = module;
		this.result = result;
		this.exception = e instanceof BusinessException ? (BusinessException) e : new BusinessException(e);
	}

	public MeveoModule getModule() {
		return module;
	}

	public ModuleInstallResult getResult() {
		return result;
	}

	public BusinessException getException() {
		return exception;
	}
	
}
