package org.meveo.service.script.module;

import java.util.Map;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.module.MeveoModulePatch;
import org.meveo.service.script.Script;

/**
 * This interface is use when executing a script patch.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 * @see MeveoModulePatch
 */
public abstract class PatchScript extends Script {

	public static final String EXECUTION_MODE_BEFORE = "BEFORE";
	public static final String EXECUTION_MODE_AFTER = "AFTER";

	@Override
	public void execute(Map<String, Object> methodContext) throws BusinessException {

		if (methodContext.get("execution_mode").equals(EXECUTION_MODE_BEFORE)) {
			preModuleUpgrade(methodContext);

		} else {
			postModuleUpgrade(methodContext);
		}
	}

	public abstract void preModuleUpgrade(Map<String, Object> methodContext) throws BusinessException;

	public abstract void postModuleUpgrade(Map<String, Object> methodContext) throws BusinessException;
}
