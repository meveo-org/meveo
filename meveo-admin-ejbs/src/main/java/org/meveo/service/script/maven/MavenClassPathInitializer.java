/**
 * 
 */
package org.meveo.service.script.maven;

import java.util.List;

import javax.inject.Inject;
import javax.transaction.Transactional;

import org.meveo.admin.listener.MeveoInitializer;
import org.meveo.admin.listener.StartupListener;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.service.script.MavenDependencyService;
import org.meveo.service.script.ScriptInstanceService;
import org.slf4j.Logger;

/**
 * Add all defined maven libaries to class path. <br>
 * Note : Called at startup by the {@linkplain StartupListener}
 * 
 * @author clement.bareth
 * @since 6.14.0
 * @version 6.14.0
 */
public class MavenClassPathInitializer implements MeveoInitializer {

	
	@Inject
	private ScriptInstanceService scriptService;
	
	@Inject
	private MavenDependencyService mavenDependencyService;
	
	@Inject
	private Logger log;
	
	@Override
	public void init() throws Exception {
		log.info("Adding maven dependencies to classpath");
		List<MavenDependency> resultList = mavenDependencyService.list();
		scriptService.addMavenLibrariesToClassPath(resultList);
		log.info("Added {} libraries to class path", resultList.size());
	}

}
