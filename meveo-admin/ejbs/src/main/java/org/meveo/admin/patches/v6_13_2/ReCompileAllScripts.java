/**
 * 
 */
package org.meveo.admin.patches.v6_13_2;

import javax.inject.Inject;

import org.meveo.admin.patches.Patch;
import org.meveo.service.script.ScriptInstanceService;

/**
 * Re-compile all scripts to make sure their .class files are written to the disk
 */
public class ReCompileAllScripts implements Patch {
	
	@Inject
	private ScriptInstanceService scriptService;

	@Override
	public void execute() throws Exception {
		for(var script : scriptService.list()) {
			scriptService.update(script);
		}
	}

	@Override
	public int order() {
		return 0;
	}

	@Override
	public String name() {
		return "ReCompileAllScripts";
	}

}
