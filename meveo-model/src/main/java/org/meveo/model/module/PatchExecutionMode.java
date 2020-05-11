package org.meveo.model.module;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since
 * @version
 */
public enum PatchExecutionMode {
	BEFORE_UPGRADE, AFTER_UPGRADE;
	
	public String getLabel() {
		return "meveoModule.patch.executionMode." + name();
	}
}
