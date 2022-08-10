/**
 * 
 */
package org.meveo.api.module;

import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.ejb.TransactionManagement;
import javax.ejb.TransactionManagementType;
import javax.inject.Inject;
import javax.transaction.HeuristicMixedException;
import javax.transaction.HeuristicRollbackException;
import javax.transaction.NotSupportedException;
import javax.transaction.RollbackException;
import javax.transaction.Status;
import javax.transaction.SystemException;
import javax.transaction.UserTransaction;
import javax.ws.rs.client.*;
import javax.ws.rs.core.Response;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.git.GitRepositoryDto;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.module.ModuleDependencyDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.git.GitRepositoryApi;
import org.meveo.commons.utils.ParamBean;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.git.GitClient;
import org.slf4j.Logger;


@TransactionManagement(TransactionManagementType.BEAN)
public class DefaultMeveoModuleInitializer {

	@Inject
	private UserTransaction userTx;

	@Inject
	private GitClient gitClient;

	@Inject
	private GitRepositoryApi gitRepositoryApi;

	@Inject
	private MeveoModuleApi moduleApi;

	@Inject
	private MeveoModuleService meveoModuleService;

	@Inject
	private Logger log;

	public Map<String, String> init(UpdateModulesParameters parameters) throws MeveoApiException, IOException, BusinessException {
		String username = parameters.getGitCredentials().getUsername();
		String password = parameters.getGitCredentials().getPassword();

		try {
			userTx.setTransactionTimeout((int) Duration.ofHours(1).toSeconds());
			
			Map<String, String> message = new HashMap<String, String>();
			String path = ParamBean.getInstance().getProperty("meveo.module.default", "/opt/jboss/wildfly/meveodata/default/module.json");
			File moduleFile = new File(path);
			MeveoModuleDto moduleDto = JacksonUtil.read(moduleFile, MeveoModuleDto.class);
			List<ModuleDependencyDto> moduleDependencyDtos = moduleDto.getModuleDependencies();
			for (ModuleDependencyDto moduleDependencyDto : moduleDependencyDtos) {
				
				userTx.begin();
				String moduleCode = moduleDependencyDto.getCode();
				String moduleGitUrl = moduleDependencyDto.getGitUrl();
				String moduleBranch = moduleDependencyDto.getGitBranch();
				try {
					GitRepository repository = null;
					GitRepositoryDto repositoryDto = null;

					// Check if module exists
					MeveoModule dependency = meveoModuleService.findByCode(moduleCode);

					if (dependency != null) {
						MeveoModule module = meveoModuleService.findByCodeWithFetchEntities(moduleCode);
						// git fetch
						repository = module.getGitRepository();
						// necessary?
						// if (repository.getRemoteOrigin()!=moduleGitUrl) {
						// repository.setRemoteOrigin(moduleGitUrl);
						// }
						gitClient.fetch(repository, username, password);
						// git checkout
						repositoryDto = gitRepositoryApi.toDto(repository);
						repositoryDto.setDefaultBranch(moduleBranch);
						repository = gitRepositoryApi.update(repositoryDto);
						// gitClient.checkout(repository, moduleBranch, false);
						// git pull
						gitClient.pull(repository, username, password);
					} else {
						repositoryDto = new GitRepositoryDto();
						repositoryDto.setCode(moduleCode);
						repositoryDto.setRemoteOrigin(moduleGitUrl);
						repositoryDto.setDefaultBranch(moduleBranch);

						repository = gitRepositoryApi.exists(repositoryDto) ? gitRepositoryApi.update(repositoryDto) : gitRepositoryApi.create(repositoryDto, false, username, password);

						// Manage dependencies
						LinkedHashMap<GitRepository, ModuleDependencyDto> gitRepos = moduleApi.retrieveModuleDependencies(List.of(moduleDependencyDto), username, password);
						for (GitRepository repo : gitRepos.keySet()) {
							ModuleDependencyDto dependencyDto = gitRepos.get(repo);
							dependencyDto.setInstalling(true);
							moduleApi.install(null, repo);
							dependencyDto.setInstalled(true);
						}

						moduleApi.install(null, repository);
					}
					message.put(moduleCode, "SUCCESS");
				} catch (Exception e) {
					log.error("Failed to install / update module", e);
					message.put(moduleCode, e.getMessage());
					userTx.rollback();
					continue;
				}

				userTx.commit();
			}
			if (parameters.getCallbackUrl()!=null && !parameters.getCallbackUrl().isEmpty()) {
				returnResponse(parameters, message);
			}
			return message;

		} catch (NotSupportedException | RollbackException | SystemException | HeuristicRollbackException | HeuristicMixedException e1) {
			throw new BusinessException("Error managing transaction", e1);
		}
	}

	public void returnResponse(UpdateModulesParameters parameters, Map<String, String> message) throws BusinessException {
		Map<String, Object> body = new HashMap<String, Object>();
		body.put("id", parameters.getId());
		body.put("message", message);
		Client client = ClientBuilder.newClient();
		WebTarget target = client.target(parameters.getCallbackUrl());
		String resp = JacksonUtil.toStringPrettyPrinted(body);
		Response response = target.request().post(Entity.json(resp));
		String responseContent = "";
		if (response.getStatus() == Response.Status.OK.getStatusCode()) {
			log.info("Response sent successfully");
		} else {
			responseContent += response.readEntity(String.class);
			log.error("Error sending response \n "+responseContent);
		}
	}
}
