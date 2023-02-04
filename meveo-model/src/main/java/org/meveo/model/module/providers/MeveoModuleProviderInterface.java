/**
 * 
 */
package org.meveo.model.module.providers;

import java.util.List;

import org.meveo.model.module.InstallableModule;
import org.meveo.model.module.MeveoModuleProvider;

public interface MeveoModuleProviderInterface {

	void init (MeveoModuleProvider provider);
	
	List<InstallableModule> list() throws Exception;
}
