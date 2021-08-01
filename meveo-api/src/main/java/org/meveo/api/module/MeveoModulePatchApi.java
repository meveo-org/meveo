package org.meveo.api.module;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.ejb.EJB;
import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ValidationException;
import org.meveo.api.BaseApi;
import org.meveo.api.ScriptInstanceApi;
import org.meveo.api.dto.module.MeveoModuleDto;
import org.meveo.api.dto.module.MeveoModulePatchDto;
import org.meveo.api.dto.module.ModuleReleaseDto;
import org.meveo.api.exception.EntityAlreadyExistsException;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.jpa.JpaAmpNewTx;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModulePatch;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.service.admin.impl.MeveoModulePatchService;
import org.meveo.service.admin.impl.MeveoModuleService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.script.module.PatchScript;

/**
 * API for managine a module patch.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 * @see MeveoModulePatch
 */
@Stateless
public class MeveoModulePatchApi extends BaseApi {

	@Inject
	private MeveoModuleService meveoModuleService;

	@Inject
	private ScriptInstanceService scriptInstanceService;

	@Inject
	private MeveoModulePatchService meveoModulePatchService;

	@Inject
	private ScriptInstanceApi scriptInstanceApi;

	@Inject
	private ModuleReleaseApi moduleReleaseApi;

	@EJB
	private MeveoModuleApi meveoModuleApi;

	@Inject
	private MeveoModulePatchApi meveoModulePatchApi;

	/**
	 * Converts this entity to its dtos representation.
	 * 
	 * @param entity the patch entity
	 * @return dto representation
	 */
	public MeveoModulePatchDto toDto(MeveoModulePatch entity) {
		return toDto(entity, true);
	}

	/**
	 * Converts this entity to its dtos representation.
	 * 
	 * @param entity the patch
	 * @param isFull when true, will also convert the script entity
	 * @return dto representation
	 */
	public MeveoModulePatchDto toDto(MeveoModulePatch entity, boolean isFull) {

		MeveoModulePatchDto dto = new MeveoModulePatchDto();
		dto.setModuleCode(entity.getMeveoModulePatchId().getMeveoModule().getCode());
		if (entity.getMeveoModulePatchId().getScriptInstance() != null) {
			if (isFull) {
				dto.setScriptInstance(scriptInstanceApi.toDto(entity.getMeveoModulePatchId().getScriptInstance()));

			} else {
				dto.getScriptInstance().setCode(entity.getMeveoModulePatchId().getScriptInstance().getCode());
			}
		}
		dto.setSourceVersion(entity.getMeveoModulePatchId().getSourceVersion());
		dto.setTargetVersion(entity.getMeveoModulePatchId().getTargetVersion());

		return dto;
	}

	/**
	 * Creates a patch entity.
	 * 
	 * @param postData patch dto data
	 * @return the created patch entity
	 * @throws MeveoApiException when creation fails
	 * @throws BusinessException when creation fails
	 */
	public MeveoModulePatchDto create(MeveoModulePatchDto postData) throws MeveoApiException, BusinessException {
		return toDto(createAndReturnEntity(postData));
	}

	/**
	 * Creates a patch entity in a new transaction.
	 * 
	 * @param postData patch data
	 * @return the created patch entity
	 * @throws MeveoApiException when creation fails
	 * @throws BusinessException when creation fails
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	@JpaAmpNewTx
	public MeveoModulePatch createAndReturnEntityInNewTx(MeveoModulePatchDto postData) throws MeveoApiException, BusinessException {
		return createAndReturnEntity(postData);
	}

	/**
	 * Creates and return the patch entity.
	 * 
	 * @param postData the patch data
	 * @return the created patch entity
	 * @throws MeveoApiException when some dependent entities does not exists
	 * @throws BusinessException when creation fails
	 */
	public MeveoModulePatch createAndReturnEntity(MeveoModulePatchDto postData) throws MeveoApiException, BusinessException {

		validate(postData);
		MeveoModule module = meveoModuleService.findByCodeWithFetchEntities(postData.getModuleCode());
		if (module == null) {
			throw new EntityDoesNotExistsException(MeveoModule.class, postData.getModuleCode());
		}

		ScriptInstance scriptInstance = scriptInstanceService.findByCode(postData.getScriptInstance().getCode());
		if (scriptInstance == null) {
			throw new EntityDoesNotExistsException(ScriptInstance.class, postData.getScriptInstance().getCode());
		}

		if (findPatchEntity(postData.getModuleCode(), postData.getScriptInstance().getCode(), postData.getSourceVersion(), postData.getTargetVersion()) != null) {
			throw new EntityAlreadyExistsException(MeveoModulePatch.class,
					String.format("moduleCode=%s, scriptCode=%s, sourceVersion=%s", postData.getModuleCode(), postData.getScriptInstance().getCode(), postData.getSourceVersion()));
		}

		MeveoModulePatch patch = new MeveoModulePatch();
		patch.setMeveoModulePatchId(module, scriptInstance, postData.getSourceVersion(), postData.getTargetVersion());

		meveoModulePatchService.create(patch);

		return patch;
	}

	/**
	 * Deletes a patch.
	 * 
	 * @param moduleCode         code of module
	 * @param scriptInstanceCode code of script
	 * @param sourceVersion      the source version
	 * @param targetVersion      the target version
	 * @throws MeveoApiException            when deletion failed
	 * @throws EntityDoesNotExistsException no match is found
	 */
	public void delete(String moduleCode, String scriptInstanceCode, String sourceVersion, String targetVersion) throws MeveoApiException, EntityDoesNotExistsException {

		MeveoModulePatch patch = findPatchEntity(moduleCode, scriptInstanceCode, sourceVersion, targetVersion);
		if (patch == null) {
			throw new EntityDoesNotExistsException(MeveoModulePatch.class,
					String.format("moduleCode=%s, scriptCode=%s, sourceVersion=%s", moduleCode, scriptInstanceCode, sourceVersion));
		}

		meveoModulePatchService.delete(patch);
	}

	/**
	 * Retrieves a patch.
	 */
	public MeveoModulePatchDto find(String moduleCode, String scriptInstanceCode, String sourceVersion, String targetVersion) throws EntityDoesNotExistsException {

		MeveoModulePatch patch = findPatchEntity(moduleCode, scriptInstanceCode, sourceVersion, targetVersion);
		if (patch == null) {
			throw new EntityDoesNotExistsException(MeveoModulePatch.class,
					String.format("moduleCode=%s, scriptCode=%s, sourceVersion=%s, targetVersion=%s", moduleCode, scriptInstanceCode, sourceVersion, targetVersion));
		}

		return toDto(patch, false);
	}

	/**
	 * Retrieves a patch.
	 * 
	 * @param moduleCode         code of module
	 * @param scriptInstanceCode code of script
	 * @param sourceVersion      the source version
	 * @param targetVersion      the target version
	 * @return matched entity
	 * @throws EntityDoesNotExistsException when no match is found
	 */
	public MeveoModulePatch findPatchEntity(String moduleCode, String scriptInstanceCode, String sourceVersion, String targetVersion) throws EntityDoesNotExistsException {

		MeveoModule module = meveoModuleService.findByCodeWithFetchEntities(moduleCode);
		if (module == null) {
			throw new EntityDoesNotExistsException(MeveoModule.class, moduleCode);
		}

		ScriptInstance scriptInstance = scriptInstanceService.findByCode(scriptInstanceCode);
		if (scriptInstance == null) {
			throw new EntityDoesNotExistsException(ScriptInstance.class, scriptInstanceCode);
		}

		return meveoModulePatchService.find(module, scriptInstance, sourceVersion, targetVersion);
	}

	/**
	 * Retrieves all the patches associated with the given module.
	 * 
	 * @param moduleCode code of module
	 * @return list of associated patches
	 * @throws EntityDoesNotExistsException when no match is found
	 */
	public List<MeveoModulePatchDto> list(String moduleCode) throws EntityDoesNotExistsException {

		MeveoModule meveoModule = meveoModuleService.findByCode(moduleCode, Arrays.asList("patches"));
		if (meveoModule == null) {
			throw new EntityDoesNotExistsException(MeveoModule.class, moduleCode);
		}

		if (meveoModule.getPatches() != null && !meveoModule.getPatches().isEmpty()) {
			return meveoModule.getPatches().stream().map(e -> toDto(e, false)).collect(Collectors.toList());
		}

		return new ArrayList<>();
	}

	/**
	 * Triggers dependent module entity creation or update.
	 * 
	 * @param meveoModule
	 * @param moduleDto
	 * @throws BusinessException
	 * @throws MeveoApiException
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRED)
	public void postCreateOrUpdate(MeveoModule meveoModule, MeveoModuleDto moduleDto) throws MeveoApiException, BusinessException {

		if (moduleDto.getPatches() != null) {
			Set<MeveoModulePatch> patches = new HashSet<>();
			if (meveoModule.getPatches() == null) {
				meveoModule.setPatches(new HashSet<>());
			}
			for (MeveoModulePatchDto patchDto : moduleDto.getPatches()) {
				MeveoModulePatch patch = meveoModulePatchApi.createAndReturnEntityInNewTx(patchDto);
				patches.add(patch);
			}
		}
	}

	/**
	 * Uploads a module release and it as a patch. The module release must be linked
	 * to a patch during exported.
	 * 
	 * @param moduleReleases list of module releases, only the first release is
	 *                       applied.
	 * @throws BusinessException when patch application failed
	 * @throws MeveoApiException when patch application failed
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@JpaAmpNewTx
	public void uploadAndApplyPatch(List<ModuleReleaseDto> moduleReleases) throws BusinessException, MeveoApiException {
		if (moduleReleases != null && !moduleReleases.isEmpty()) {
			ModuleReleaseDto moduleRelease = moduleReleases.get(0);

			// create the scripts attached to the patches
			if (moduleRelease.getPatches() != null && !moduleRelease.getPatches().isEmpty()) {
				for (MeveoModulePatchDto patch : moduleRelease.getPatches()) {
					if (scriptInstanceService.findByCode(patch.getScriptInstance().getCode()) == null) {
						scriptInstanceApi.create(patch.getScriptInstance());
						scriptInstanceService.flush();
					}

					ScriptInstance scriptInstance = scriptInstanceService.findByCode(patch.getScriptInstance().getCode());
					boolean checkTestSuite = meveoModuleService.checkTestSuites(scriptInstance.getCode());
					if (!checkTestSuite) {
						throw new ValidationException("There some test suits failed", "meveoModule.checkTestSuitsReleaseFailed");
					}
				}
			}

			// apply the patches
			meveoModulePatchApi.apply(moduleRelease);
		}
	}

	/**
	 * Parse and sort the patches from module release dto.
	 * 
	 * @param moduleRelease The exported module release
	 * @throws BusinessException when patch application fail
	 * @throws MeveoApiException when patch application fail
	 */
	public void apply(ModuleReleaseDto moduleRelease) throws BusinessException, MeveoApiException {

		List<MeveoModulePatchDto> meveoModulePatches = moduleRelease.getPatches();
		if (meveoModulePatches != null && !meveoModulePatches.isEmpty()) {
			List<MeveoModulePatchDto> patchesToExecute = new ArrayList<>();
			Integer targetVersion = meveoModulePatchService.convertVersionToInt(moduleRelease.getCurrentVersion());

			Collections.sort(meveoModulePatches);

			MeveoModule meveoModule = meveoModuleService.findByCodeWithFetchEntities(moduleRelease.getCode());
			Integer sourceVersion = meveoModulePatchService.convertVersionToInt(meveoModule.getCurrentVersion());

			if (meveoModule.getCurrentVersion().equals(moduleRelease.getCurrentVersion())) {
				throw new BusinessException("MeveoModule with code=" + meveoModule.getCode() + " is already updated.");
			}

			if (meveoModulePatchService.convertVersionToInt(moduleRelease.getCurrentVersion()) < sourceVersion) {
				throw new BusinessException("Patch application failed. Patch is less than the current version " + meveoModule.getCurrentVersion());
			}

			// can be multiple patch
			patchesToExecute.addAll(meveoModulePatchService.findPath(meveoModulePatches, sourceVersion, targetVersion));

			if (!patchesToExecute.isEmpty()) {
				applyPatches(moduleRelease, patchesToExecute);
			}

			addPatchesToModule(moduleRelease.getCode(), meveoModulePatches);
		}
	}

	/**
	 * Add a list of patches to a module.
	 * 
	 * @param moduleCode         code of module
	 * @param meveoModulePatches list of patches
	 * @throws MeveoApiException when adding of patches to a module failed
	 * @throws BusinessException when adding of patches to a module failed
	 */
	private void addPatchesToModule(String moduleCode, List<MeveoModulePatchDto> meveoModulePatches) throws MeveoApiException, BusinessException {

		for (MeveoModulePatchDto dto : meveoModulePatches) {
			if (findPatchEntity(dto.getModuleCode(), dto.getScriptInstance().getCode(), dto.getSourceVersion(), dto.getTargetVersion()) != null) {
				delete(dto.getModuleCode(), dto.getScriptInstance().getCode(), dto.getSourceVersion(), dto.getTargetVersion());
			}

			meveoModulePatchApi.createInNewTx(dto);
		}
	}

	/**
	 * Creates a new module patch in a new transaction.
	 * 
	 * @param dto the patch data
	 * @throws MeveoApiException when creation fail
	 * @throws BusinessException when creation fail
	 */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
	@JpaAmpNewTx
	private void createInNewTx(MeveoModulePatchDto dto) throws MeveoApiException, BusinessException {
		create(dto);
	}

	/**
	 * Apply the actual patch by executing the pre and post update scripts. Module
	 * is also updated at this point.
	 * 
	 * @param moduleRelease    the exported released module
	 * @param patchesToExecute list of patches to execute
	 * @throws BusinessException when patch application fails
	 * @throws MeveoApiException when patch application fails
	 */
	@SuppressWarnings("serial")
	private void applyPatches(ModuleReleaseDto moduleRelease, List<MeveoModulePatchDto> patchesToExecute) throws BusinessException, MeveoApiException {

		for (MeveoModulePatchDto patchDto : patchesToExecute) {
			scriptInstanceService.execute(patchDto.getScriptInstance().getCode(), new HashMap<String, Object>() {
				{
					put("execution_mode", PatchScript.EXECUTION_MODE_BEFORE);
				}
			});
		}

		meveoModuleApi.createOrUpdateInNewTx(moduleReleaseApi.convertToMeveoModule(moduleRelease));

		for (MeveoModulePatchDto patchDto : patchesToExecute) {
			scriptInstanceService.execute(patchDto.getScriptInstance().getCode(), new HashMap<String, Object>() {
				{
					put("execution_mode", PatchScript.EXECUTION_MODE_AFTER);
				}
			});
		}
	}
}
