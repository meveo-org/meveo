package org.meveo.service.admin.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;

import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.module.MeveoModulePatchDto;
import org.meveo.commons.utils.QueryBuilder;
import org.meveo.jpa.EntityManagerWrapper;
import org.meveo.jpa.MeveoJpa;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModulePatch;
import org.meveo.model.module.MeveoModulePatchId;
import org.meveo.model.scripts.ScriptInstance;

/**
 * Service use in managing a module's patch.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 */
@Stateless
public class MeveoModulePatchService {

	@Inject
	@MeveoJpa
	private EntityManagerWrapper emWrapper;

	public EntityManager getEntityManager() {
		return emWrapper.getEntityManager();
	}

	/**
	 * Creates a module patch.
	 * 
	 * @param entity the entity to be created
	 * @throws BusinessException when a patch already exists
	 */
	public void create(MeveoModulePatch entity) throws BusinessException {

		if (find(entity.getMeveoModulePatchId()) != null) {
			throw new BusinessException("Patch already exists");
		}

		Set<MeveoModulePatch> patches = entity.getMeveoModulePatchId().getMeveoModule().getPatches();
		if (patches == null) {
			patches = new HashSet<>();
		}
		patches.add(entity);
		entity.getMeveoModulePatchId().getMeveoModule().setPatches(patches);
		getEntityManager().persist(entity);
	}

	/**
	 * Deletes a selected patch.
	 * 
	 * @param entity the patch to be deleted
	 */
	public void delete(MeveoModulePatch entity) {
		getEntityManager().remove(entity);
	}

	/**
	 * Retrieves a patch given a patch id.
	 * 
	 * @param patchId the patch id that will be search
	 * @return the matching patch
	 */
	public MeveoModulePatch find(MeveoModulePatchId patchId) {
		return find(patchId.getMeveoModule(), patchId.getScriptInstance(), patchId.getSourceVersion(), patchId.getTargetVersion());
	}

	/**
	 * Retrieves a patch with the given filter.
	 * 
	 * @param meveoModule    the meveo module
	 * @param scriptInstance the script instance
	 * @param sourceVersion  source version
	 * @param targetVersion  the target version
	 * @return the entity matched or null if none exists
	 */
	public MeveoModulePatch find(MeveoModule meveoModule, ScriptInstance scriptInstance, String sourceVersion, String targetVersion) {

		QueryBuilder qb = new QueryBuilder(MeveoModulePatch.class, "m");
		qb.addCriterionEntity("meveoModulePatchId.meveoModule", meveoModule);
		qb.addCriterionEntity("meveoModulePatchId.scriptInstance", scriptInstance);
		qb.addCriterionEntity("meveoModulePatchId.sourceVersion", sourceVersion);
		qb.addCriterionEntity("meveoModulePatchId.targetVersion", targetVersion);

		try {
			return (MeveoModulePatch) qb.getQuery(getEntityManager()).getSingleResult();

		} catch (NoResultException e) {
			return null;
		}
	}

	/**
	 * Converts a string version to integer removing the "." character.
	 * 
	 * @param version the string version
	 * @return the integer version
	 */
	public Integer convertVersionToInt(String version) {
		return Integer.parseInt(version.replace(".", ""));
	}

	/**
	 * Calculates if a patch exists from the patch source to target version. For
	 * example if we want to update from version 1.0.0 to 2.0.0, but no patch
	 * exists, then we can check for pre 2.0.0. Example 1.0.0-1.5.0 and 1.5.0-2.0.0,
	 * therefore a path exists.
	 * 
	 * @param patches       list of patches that will be examine for a path
	 * @param sourceVersion the source version
	 * @param targetVersion the target version
	 * @return a patch or a list of patches
	 */
	public List<MeveoModulePatchDto> findPath(List<MeveoModulePatchDto> patches, Integer sourceVersion, Integer targetVersion) {

		List<MeveoModulePatchDto> result = new ArrayList<>();
		TreeMap<Integer, MeveoModulePatchDto> treeMap = new TreeMap<>();

		for (MeveoModulePatchDto patch : patches) {
			treeMap.put(patch.getSourceVersionAsInt(), patch);
		}

		// check if a patch exists from source to target
		findPath(treeMap, sourceVersion, targetVersion, result);

		return result;
	}

	/**
	 * Calculates if a patch exists from the patch source to target version. For
	 * example if we want to update from version 1.0.0 to 2.0.0, but no patch
	 * exists, then we can check for pre 2.0.0. Example 1.0.0-1.5.0 and 1.5.0-2.0.0,
	 * therefore a path exists.
	 * 
	 * @param treeMap a sorted list of patches
	 * @param from    the source version
	 * @param to      the target version
	 * @param patches list of patches
	 * @return true if a patch exists
	 */
	public static boolean findPath(SortedMap<Integer, MeveoModulePatchDto> treeMap, Integer from, Integer to, List<MeveoModulePatchDto> patches) {

		boolean flag = false;
		while (true) {
			MeveoModulePatchDto currentPatch = treeMap.get(from);
			if (currentPatch == null) {
				break;
			}
			patches.add(currentPatch);

			Integer value = currentPatch.getTargetVersionAsInt();

			if (value.equals(to)) {
				flag = true;
				break;
			}

			from = value;
		}

		return flag;
	}

	/**
	 * Removes a patch.
	 * 
	 * @param e the patch to be remove
	 */
	public void remove(MeveoModulePatch e) {
		e = find(e.getMeveoModulePatchId().getMeveoModule(), e.getMeveoModulePatchId().getScriptInstance(), e.getMeveoModulePatchId().getSourceVersion(),
				e.getMeveoModulePatchId().getTargetVersion());
		getEntityManager().remove(e);
	}

}
