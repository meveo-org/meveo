/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 * (C) Copyright 2015-2018 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.service.script;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.annotation.Priority;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.enterprise.inject.Default;
import javax.inject.Inject;
import javax.persistence.NoResultException;

import org.jboss.weld.contexts.ContextNotActiveException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementNotFoundException;
import org.meveo.admin.exception.InvalidPermissionException;
import org.meveo.admin.exception.InvalidScriptException;
import org.meveo.admin.exception.ScriptExecutionException;
import org.meveo.admin.listener.CommitMessageBean;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.ScriptInstanceDto;
import org.meveo.commons.utils.EjbUtils;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.jpa.JpaAmpNewTx;
import org.meveo.model.ModulePostInstall;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.scripts.CustomScript;
import org.meveo.model.scripts.FunctionServiceFor;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.scripts.ScriptSourceTypeEnum;
import org.meveo.model.scripts.ScriptTransactionType;
import org.meveo.model.security.Role;
import org.meveo.service.admin.impl.ModuleInstallationContext;
import org.meveo.service.base.BusinessService;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.MeveoRepository;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.10
 **/
@FunctionServiceFor(ScriptInstance.TYPE)
@Stateless
@Default
public class ScriptInstanceService extends CustomScriptService<ScriptInstance> {

	@Inject
	private ModuleInstallationContext moduleInstallationContext;

	@Inject
	private MavenDependencyService mdService;

	@Inject
	private ScriptInstanceExecutor scriptExecutor;

	@Inject
	private GitClient gitClient;

	@Inject
	@MeveoRepository
	private GitRepository meveoRepository;

	@Inject
	private CommitMessageBean commitMessageBean;

	@Override
	protected void beforeUpdateOrCreate(ScriptInstance script) throws BusinessException {
		super.beforeUpdateOrCreate(script);

		// Fetch maven dependencies
		Set<MavenDependency> mavenDependencies = new HashSet<>();
		for(MavenDependency md : script.getMavenDependenciesNullSafe()) {
			MavenDependency persistentMd = mdService.find(md.getBuiltCoordinates());
			if(persistentMd != null) {
				mavenDependencies.add(persistentMd);
			} else {
				getEntityManager().persist(md);
				mavenDependencies.add(md);
			}
		}
		script.setMavenDependencies(mavenDependencies);
	}

	@Override
	public void afterUpdateOrCreate(ScriptInstance script) throws BusinessException {
		super.afterUpdateOrCreate(script);

		mdService.removeOrphans(script);
	}



	/**
	 * Get all ScriptInstances with error.
	 *
	 * @return list of custom script.
	 */
	public List<CustomScript> getScriptInstancesWithError() {
		return getEntityManager().createNamedQuery("CustomScript.getScriptInstanceOnError", CustomScript.class).setParameter("isError", Boolean.TRUE)
				.getResultList();
	}

	/**
	 * Count scriptInstances with error.
	 *
	 * @return number of script instances with error.
	 */
	public long countScriptInstancesWithError() {
		return getEntityManager().createNamedQuery("CustomScript.countScriptInstanceOnError", Long.class).setParameter("isError", Boolean.TRUE).getSingleResult();
	}

	/**
	 * Compile all scriptInstances.
	 */
	public void compileAll() {

		List<ScriptInstance> scriptInstances = findByType(ScriptSourceTypeEnum.JAVA);
		compile(scriptInstances);
	}

	/**
	 * Execute the script identified by a script code. No init nor finalize methods are called.
	 *
	 * @param scriptCode ScriptInstanceCode
	 * @param context Context parameters (optional)
	 * @return Context parameters. Will not be null even if "context" parameter is null.
	 * @throws org.meveo.admin.exception.InvalidPermissionException Insufficient access to run the script
	 * @throws org.meveo.admin.exception.ElementNotFoundException Script not found
	 * @throws org.meveo.admin.exception.BusinessException Any execution exception
	 */
	@Override
	@TransactionAttribute(TransactionAttributeType.SUPPORTS)
	public Map<String, Object> execute(String scriptCode, Map<String, Object> context) throws BusinessException {
		if(scriptCode == null) {
			throw new IllegalArgumentException("Script code should not be null");
		}

		ScriptInstance scriptInstance = findByCode(scriptCode, List.of("executionRoles"));

		if(scriptInstance == null) {
			throw new ElementNotFoundException( scriptCode, "ScriptInstance");
		}
		// Check access to the script
		isUserHasExecutionRole(scriptInstance);


		ScriptInterface executionEngine = getExecutionEngine(scriptInstance, context);
		return super.execute(executionEngine, context);
	}

	/**
	 * Wrap the logger and execute script.
	 *
	 * @param scriptCode code of script
	 * @param context context used in execution of script.
	 */
	public void test(String scriptCode, Map<String, Object> context) {

		try {
			execute(scriptCode, context);
		} catch (BusinessException e) {
			log.error("Script test execution failed", e);
		}
	}

	/**
	 * Only users having a role in executionRoles can execute the script, not having the role should throw an InvalidPermission exception that extends businessException. A script
	 * with no executionRoles can be executed by any user.
	 *
	 * @param scriptInstance instance of script
	 * @throws org.meveo.admin.exception.InvalidPermissionException invalid permission exception.
	 */
	public void isUserHasExecutionRole(ScriptInstance scriptInstance) throws InvalidPermissionException {
		if (scriptInstance != null && scriptInstance.getExecutionRolesNullSafe() != null && !scriptInstance.getExecutionRolesNullSafe().isEmpty()) {
			Set<Role> execRoles = scriptInstance.getExecutionRolesNullSafe();
			for (Role role : execRoles) {
				if (currentUser.hasRole(role.getName())) {
					return;
				}
			}
			throw new InvalidPermissionException();
		}
	}

	/**
	 * @param scriptInstance instance of script
	 * @return true if user have the souring role.
	 */
	public boolean isUserHasSourcingRole(ScriptInstance scriptInstance) {
		if (scriptInstance != null && scriptInstance.getSourcingRolesNullSafe() != null && !scriptInstance.getSourcingRolesNullSafe().isEmpty()) {
			Set<Role> sourcingRoles = scriptInstance.getSourcingRolesNullSafe();
			for (Role role : sourcingRoles) {
				if (currentUser.hasRole(role.getName())) {
					return true;
				}
			}
			return false;
		}
		return true;
	}

	/**
	 * This is used to invoke a method in a new transaction from a script.<br>
	 * This will prevent DB errors in the script from affecting notification history creation.
	 *
	 * @param workerName The name of the API or service that will be invoked.
	 * @param methodName The name of the method that will be invoked.
	 * @param parameters The array of parameters accepted by the method. They must be specified in exactly the same order as the target method.
	 * @throws org.meveo.admin.exception.BusinessException business exception.
	 */
	@JpaAmpNewTx
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	public void callWithNewTransaction(String workerName, String methodName, Object... parameters) throws BusinessException {
		try {
			Object worker = EjbUtils.getServiceInterface(workerName);
			String workerClassName = ReflectionUtils.getCleanClassName(worker.getClass().getName());
			Class<?> workerClass = Class.forName(workerClassName);
			Method method = null;
			if (parameters.length < 1) {
				method = workerClass.getDeclaredMethod(methodName);
			} else {
				String className = null;
				Object parameter = null;
				Class<?>[] parameterTypes = new Class<?>[parameters.length];
				for (int i = 0; i < parameters.length; i++) {
					parameter = parameters[i];
					className = ReflectionUtils.getCleanClassName(parameter.getClass().getName());
					parameterTypes[i] = Class.forName(className);
				}
				method = workerClass.getDeclaredMethod(methodName, parameterTypes);
			}
			method.setAccessible(true);
			method.invoke(worker, parameters);
		} catch (Exception e) {
			if (e.getCause() != null) {
				throw new BusinessException(e.getCause());
			} else {
				throw new BusinessException(e);
			}
		}
	}

	/**
	 * Get all script interfaces with compiling those that are not compiled yet
	 *
	 * @return the allScriptInterfaces
	 */
	public List<ScriptInterfaceSupplier> getAllScriptInterfacesWCompile() {

		List<ScriptInterfaceSupplier> scriptInterfaces = new ArrayList<>();

		List<ScriptInstance> scriptInstances = findByType(ScriptSourceTypeEnum.JAVA);
		for (ScriptInstance scriptInstance : scriptInstances) {
			try {
				scriptInterfaces.add(getScriptInterfaceWCompile(scriptInstance.getCode()));
			} catch (ElementNotFoundException | InvalidScriptException e) {
				// Ignore errors here as they were logged in a call before
			}
		}

		return scriptInterfaces;
	}

	@Override
	protected void executeEngine(ScriptInterface engine, Map<String, Object> context) throws ScriptExecutionException {
		ScriptTransactionType txType;

		try {
			txType = getEntityManager()
					.createQuery("SELECT transactionType FROM ScriptInstance WHERE code = :code", ScriptTransactionType.class)
					.setParameter("code", engine.getClass().getName())
					.getSingleResult();
		} catch (NoResultException e) {
			txType = ScriptTransactionType.SAME;
		}

		switch(txType) {
			case MANUAL:
				scriptExecutor.executeManualTx(engine, context);
				break;
			case NEW:
				scriptExecutor.executeInNewTx(engine, context);
				break;
			case NONE:
				scriptExecutor.executeNoTx(engine, context);
				break;
			case SAME:
			default:
				super.executeEngine(engine, context);
		}
	}

	/**
	 * Compile the scripts the has been installed with the module
	 *
	 * @param module installed module
	 * @throws InvalidScriptException if one of the script can't be compiled
	 */
	public void postModuleInstall(@Observes @ModulePostInstall @Priority(2) MeveoModule module) throws InvalidScriptException {
		reCompileAll(module);
	}
	
	public void reCompileAll(MeveoModule module) throws InvalidScriptException {
		var scripts = module.getModuleItems().stream()
				.filter(item -> item.getItemClass().equals(ScriptInstance.class.getName()))
				.map(item -> findByCode(item.getItemCode()))
				.collect(Collectors.toList());

		if (!scripts.isEmpty()) {
			compileScripts(scripts);
		}
	}

	/**
	 * see java-doc {@link BusinessService#addFilesToModule(org.meveo.model.BusinessEntity, MeveoModule)}
	 */
	@Override
	public void addFilesToModule(ScriptInstance entity, MeveoModule module) throws BusinessException {
		super.addFilesToModule(entity, module);
		String extension = entity.getSourceTypeEnum() == ScriptSourceTypeEnum.ES5 ? ".js" : ".java";
		if (extension == ".java") {
			String path = entity.getCode().replaceAll("\\.", "/");

			File gitDirectory = GitHelper.getRepositoryDir(currentUser, module.getCode() + "/facets/java/");
			String pathNewFile = path + ".java";

			File newFile = new File(gitDirectory, pathNewFile);

			try {
				MeveoFileUtils.writeAndPreserveCharset(entity.getScript(), newFile);
			} catch (IOException e) {
				throw new BusinessException("File cannot be write", e);
			}

			String message = "Add the script File : " + entity.getCode() + "in the module : " + module.getCode();
			try {
				message+=" "+commitMessageBean.getCommitMessage();
			} catch (ContextNotActiveException e) {
				log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
			}
			gitClient.commitFiles(module.getGitRepository(), Collections.singletonList(newFile), message);
		}
	}

	@Override
	protected BaseEntityDto getDto(ScriptInstance entity) throws BusinessException {
		ScriptInstanceDto dto = (ScriptInstanceDto) super.getDto(entity);
		dto.setScript(null);
		return dto;
	}

}