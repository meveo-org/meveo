package org.meveo.api;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.TypedQuery;

import org.apache.commons.collections.CollectionUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.MavenDependencyDto;
import org.meveo.api.dto.RoleDto;
import org.meveo.api.dto.ScriptInstanceDto;
import org.meveo.api.dto.ScriptInstanceErrorDto;
import org.meveo.api.dto.script.CustomScriptDto;
import org.meveo.api.exception.BusinessApiException;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.EntityDoesNotExistsException;
import org.meveo.api.exception.InvalidParameterException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.api.exception.MissingParameterException;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.scripts.ScriptInstanceError;
import org.meveo.model.scripts.ScriptSourceTypeEnum;
import org.meveo.model.security.Role;
import org.meveo.service.admin.impl.ModuleInstallationContext;
import org.meveo.service.admin.impl.RoleService;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.script.CustomScriptService;
import org.meveo.service.script.FunctionCategoryService;
import org.meveo.service.script.MavenDependencyService;
import org.meveo.service.script.ScriptInstanceService;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 **/
@Stateless
public class ScriptInstanceApi extends BaseCrudApi<ScriptInstance, ScriptInstanceDto> {

	@Inject
	private ModuleInstallationContext moduleInstallationContext;
	
	@Inject
	private ScriptInstanceService scriptInstanceService;

	@Inject
	private RoleService roleService;

	@Inject
	private MavenDependencyService mavenDependencyService;

	@Inject
	private FunctionCategoryService fcService;

	public ScriptInstanceApi() {
		super(ScriptInstance.class, ScriptInstanceDto.class);
	}

	/**
	 * @param code Optional. Filter on script code
	 * @return A list of {@link ScriptInstanceDto} initialized with id, code, type
	 *         and error.
	 */
	public List<ScriptInstanceDto> getScriptsForTreeView(String code) {
		StringBuffer query = new StringBuffer("SELECT new org.meveo.api.dto.ScriptInstanceDto(id, code, sourceTypeEnum, error) FROM ScriptInstance s");

		if (code != null) {
			query.append(" WHERE upper(s.code) LIKE :code");
		}

		TypedQuery<ScriptInstanceDto> jpaQuery = scriptInstanceService.getEntityManager().createQuery(query.toString(), ScriptInstanceDto.class);

		if (code != null) {
			jpaQuery.setParameter("code", "%" + code.toUpperCase() + "%");
		}

		return jpaQuery.getResultList();
	}

	public List<ScriptInstanceErrorDto> create(ScriptInstanceDto scriptInstanceDto) throws MeveoApiException, BusinessException {

		List<ScriptInstanceErrorDto> result = new ArrayList<>();
		checkDtoAndUpdateCode(scriptInstanceDto);

		if (scriptInstanceService.findByCode(scriptInstanceDto.getCode()) != null) {
			throw new EntityAlreadyExistsException(ScriptInstance.class, scriptInstanceDto.getCode());
		}

		ScriptInstance scriptInstance = scriptInstanceFromDTO(scriptInstanceDto, null);

		scriptInstanceService.create(scriptInstance);

		if (scriptInstance != null && scriptInstance.isError() != null && scriptInstance.isError().booleanValue()) {
			for (ScriptInstanceError error : scriptInstance.getScriptErrors()) {
				ScriptInstanceErrorDto errorDto = new ScriptInstanceErrorDto(error);
				result.add(errorDto);
			}
		}

		return result;
	}

	public List<ScriptInstanceErrorDto> update(ScriptInstanceDto scriptInstanceDto) throws MeveoApiException, BusinessException {

		List<ScriptInstanceErrorDto> result = new ArrayList<>();
		checkDtoAndUpdateCode(scriptInstanceDto);

		ScriptInstance scriptInstance = scriptInstanceService.findByCode(scriptInstanceDto.getCode());

		if (scriptInstance == null) {
			throw new EntityDoesNotExistsException(ScriptInstance.class, scriptInstanceDto.getCode());

		} else if (!scriptInstanceService.isUserHasSourcingRole(scriptInstance)) {
			throw new MeveoApiException("User does not have a permission to update a given script");
		}

		scriptInstanceFromDTO(scriptInstanceDto, scriptInstance);

		scriptInstanceService.updateNoMerge(scriptInstance);

		if (scriptInstance.isError().booleanValue()) {
			for (ScriptInstanceError error : scriptInstance.getScriptErrors()) {
				ScriptInstanceErrorDto errorDto = new ScriptInstanceErrorDto(error);
				result.add(errorDto);
			}
		}
			
		return result;
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.meveo.api.ApiService#find(java.lang.String)
	 */
	@Override
	public ScriptInstanceDto find(String scriptInstanceCode) throws EntityDoesNotExistsException, MissingParameterException, InvalidParameterException, MeveoApiException {

		ScriptInstanceDto scriptInstanceDtoResult = null;

		if (StringUtils.isBlank(scriptInstanceCode)) {
			missingParameters.add("scriptInstanceCode");
			handleMissingParameters();
		}

		ScriptInstance scriptInstance = scriptInstanceService.findByCode(scriptInstanceCode);
		if (scriptInstance == null) {
			throw new EntityDoesNotExistsException(ScriptInstance.class, scriptInstanceCode);
		}

		scriptInstanceDtoResult = toDto(scriptInstance);

		return scriptInstanceDtoResult;
	}

	public void removeScriptInstance(String scriptInstanceCode) throws EntityDoesNotExistsException, MissingParameterException, BusinessException {
		if (StringUtils.isBlank(scriptInstanceCode)) {
			missingParameters.add("scriptInstanceCode");
			handleMissingParameters();
		}
		ScriptInstance scriptInstance = scriptInstanceService.findByCode(scriptInstanceCode);
		if (scriptInstance == null) {
			throw new EntityDoesNotExistsException(ScriptInstance.class, scriptInstanceCode);
		}
		scriptInstanceService.remove(scriptInstance);
	}

	@Override
	public ScriptInstance createOrUpdate(ScriptInstanceDto postData) throws MeveoApiException, BusinessException {
		createOrUpdateWithCompile(postData);

		return scriptInstanceService.findByCode(postData.getCode());
	}

	public List<ScriptInstanceErrorDto> createOrUpdateWithCompile(ScriptInstanceDto postData) throws MeveoApiException, BusinessException {

		List<ScriptInstanceErrorDto> result;
		checkDtoAndUpdateCode(postData);

		ScriptInstance scriptInstance = scriptInstanceService.findByCode(postData.getCode());

		if (scriptInstance == null) {
			result = create(postData);

		} else {
			result = update(postData);
		}

		return result;
	}

	public void checkDtoAndUpdateCode(CustomScriptDto dto) throws BusinessApiException, MissingParameterException, InvalidParameterException {

		if (StringUtils.isBlank(dto.getScript()) && !this.moduleInstallationContext.isActive()) {
			missingParameters.add("script");
		}

		handleMissingParameters();

		if (dto.getType() == ScriptSourceTypeEnum.JAVA) {
			String scriptCode = StringUtils.isBlank(dto.getScript()) ? dto.getCode() : ScriptInstanceService.getFullClassname(dto.getScript());
			if (!StringUtils.isBlank(dto.getCode()) && !dto.getCode().equals(scriptCode)) {
				throw new BusinessApiException("The code and the canonical script class name must be identical");
			}

			// check script existed full class name in class path
			if (CustomScriptService.isOverwritesJavaClass(scriptCode)) {
				throw new InvalidParameterException("The class with such name already exists");
			}

			dto.setCode(scriptCode);
		}
	}

	/**
	 * Convert ScriptInstanceDto to a ScriptInstance instance.
	 *
	 * @param dto                    ScriptInstanceDto object to convert
	 * @param scriptInstanceToUpdate ScriptInstance to update with values from dto,
	 *                               or if null create a new one
	 * @return A new or updated ScriptInstance object
	 * @throws EntityDoesNotExistsException entity does not exist exception.
	 */
	public ScriptInstance scriptInstanceFromDTO(ScriptInstanceDto dto, ScriptInstance scriptInstance) throws EntityDoesNotExistsException, BusinessException {

		if (scriptInstance == null) {
			scriptInstance = new ScriptInstance();
			scriptInstance.setCode(dto.getCode());
		}

		scriptInstance.setDescription(dto.getDescription());
		scriptInstance.setScript(dto.getScript());
		scriptInstance.setSamples(dto.getSamples());
		scriptInstance.setGenerateOutputs(dto.getGenerateOutputs());
		scriptInstance.setTransactionType(dto.getTransactionType());

		if (dto.getCategory() != null) {
			scriptInstance.setCategory(fcService.findByCode(dto.getCategory()));
		}

		if (dto.getType() != null) {
			scriptInstance.setSourceTypeEnum(dto.getType());

		} else {
			scriptInstance.setSourceTypeEnum(ScriptSourceTypeEnum.JAVA);
		}

		for (RoleDto roleDto : dto.getExecutionRoles()) {
			Role role = roleService.findByName(roleDto.getName());
			if (role == null) {
				throw new EntityDoesNotExistsException(Role.class, roleDto.getName(), "name");
			}
			scriptInstance.getExecutionRolesNullSafe().add(role);
		}

		for (RoleDto roleDto : dto.getSourcingRoles()) {
			Role role = roleService.findByName(roleDto.getName());
			if (role == null) {
				throw new EntityDoesNotExistsException(Role.class, roleDto.getName(), "name");
			}
			scriptInstance.getSourcingRolesNullSafe().add(role);
		}

		Set<MavenDependency> mavenDependencyList = new HashSet<>();
		for (MavenDependencyDto mavenDependencyDto : dto.getMavenDependencies()) {
			MavenDependency mavenDependency = new MavenDependency();
			mavenDependency.setGroupId(mavenDependencyDto.getGroupId());
			mavenDependency.setArtifactId(mavenDependencyDto.getArtifactId());
			mavenDependency.setVersion(mavenDependencyDto.getVersion());
			mavenDependency.setClassifier(mavenDependencyDto.getClassifier());
			mavenDependency.getBuiltCoordinates();
			mavenDependencyList.add(mavenDependency);
		}
		scriptInstance.getMavenDependenciesNullSafe().clear();
		scriptInstance.getMavenDependenciesNullSafe().addAll(mavenDependencyList);

		if (!mavenDependencyList.isEmpty()) {
			Integer line = 1;
			Map<String, Integer> map = new HashMap<>();
			for (MavenDependency maven : mavenDependencyList) {
				String key = maven.getGroupId() + maven.getArtifactId();
				if (map.containsKey(key)) {
					Integer position = map.get(key);
					throw new BusinessException("GroupId and artifactId of maven dependency " + line + " and " + position + " are duplicated");
				} else {
					map.put(key, line);
				}
				line++;
			}
			line = 1;
			for (MavenDependency maven : mavenDependencyList) {
				if (!mavenDependencyService.validateUniqueFields(maven.getVersion(), maven.getGroupId(), maven.getArtifactId())) {
					throw new BusinessException("Same artifact with other version already exist" + "(new version : "+maven.getGroupId()+":"+maven.getArtifactId()+":"+maven.getVersion()+" ref by " + dto.getCode() + ")");
				}
				line++;

				MavenDependency existingDependency = mavenDependencyService.find(maven.getBuiltCoordinates());
				if (existingDependency != null) {
					scriptInstance.getMavenDependenciesNullSafe().remove(existingDependency);
					scriptInstance.getMavenDependenciesNullSafe().add(existingDependency);
				}
			}
		}

		List<ScriptInstance> importedScripts = scriptInstanceService.populateImportScriptInstance(scriptInstance.getScript());
		if (CollectionUtils.isNotEmpty(importedScripts)) {
			scriptInstance.getImportScriptInstancesNullSafe().addAll(importedScripts);
		} else {
			scriptInstance.getImportScriptInstancesNullSafe().clear();
		}

		return scriptInstance;
	}

	public String getScriptCodesAsJSON() {
		return (String) scriptInstanceService.getEntityManager()
				.createNativeQuery("SELECT cast(json_agg(code) as text) FROM meveo_script_instance " + "INNER JOIN meveo_function ON meveo_function.id = meveo_script_instance.id")
				.getSingleResult();
	}

	@Override
	public ScriptInstanceDto toDto(ScriptInstance scriptInstance) {
		return new ScriptInstanceDto(scriptInstance, scriptInstance.getScript());
	}

	@Override
	public ScriptInstance fromDto(ScriptInstanceDto dto) throws MeveoApiException {
		try {
			return scriptInstanceFromDTO(dto, null);
		} catch (Exception e) {
			throw new MeveoApiException(e);
		}
	}

	@Override
	public IPersistenceService<ScriptInstance> getPersistenceService() {
		return scriptInstanceService;
	}

	@Override
	public boolean exists(ScriptInstanceDto dto) {
		return scriptInstanceService.findByCode(dto.getCode()) != null;
	}
	
	@Override
	public void remove(ScriptInstanceDto dto) throws MeveoApiException, BusinessException {
		var entity = scriptInstanceService.findByCode(dto.getCode());
		if(entity != null) {
			scriptInstanceService.remove(entity);
		}
	}
}