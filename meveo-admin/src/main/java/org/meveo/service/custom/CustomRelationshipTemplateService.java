/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
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
package org.meveo.service.custom;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.annotation.Resource;
import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.persistence.Tuple;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.infinispan.Cache;
import org.jboss.weld.contexts.ContextNotActiveException;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.listener.CommitMessageBean;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.CustomRelationshipTemplateDto;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.commons.utils.ParamBean;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.module.MeveoModuleItem;
import org.meveo.model.persistence.DBStorageType;
import org.meveo.persistence.StorageImplProvider;
import org.meveo.service.admin.impl.PermissionService;
import org.meveo.service.base.BusinessService;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.crm.impl.JSONSchemaGenerator;
import org.meveo.service.crm.impl.JSONSchemaIntoJavaClassParser;
import org.meveo.service.git.GitHelper;
import org.meveo.service.storage.RepositoryService;
import org.meveo.util.EntityCustomizationUtils;

import com.github.javaparser.ast.CompilationUnit;

/**
 * Class used for persisting CustomRelationshipTemplate entities
 * @author Clément Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.12
 */
@Stateless
public class CustomRelationshipTemplateService extends BusinessService<CustomRelationshipTemplate> {

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;
    
    @Inject
    private PermissionService permissionService;

	@Inject
	private CustomFieldsCacheContainerProvider customFieldsCache;

	@Resource(lookup = "java:jboss/infinispan/cache/meveo/unique-crt")
	private Cache<String, Boolean> uniqueRelations;

	@Inject
	private RepositoryService repositoryService;

	@Inject
	private CustomRelationshipTemplateService customRelationshipTemplateService;

	@Inject
	private JSONSchemaIntoJavaClassParser jSONSchemaIntoJavaClassParser;

	@Inject
	private JSONSchemaGenerator jSONSchemaGenerator;

	@Inject
	private CustomEntityTemplateCompiler cetCompiler;

	@Inject
	private CustomEntityTemplateService cetService;
	
	@Inject
	private StorageImplProvider provider;

	@Inject
	CommitMessageBean commitMessageBean;

	private ParamBean paramBean = ParamBean.getInstance();

	@Override
	public void create(CustomRelationshipTemplate crt) throws BusinessException {
		if (!EntityCustomizationUtils.validateOntologyCode(crt.getCode())) {
			throw new IllegalArgumentException("The code of ontology elements must not contain numbers");
		}

		if(crt.getStartNode() == null) {
			throw new IllegalArgumentException("Can't create relation " + crt.getCode() + ": start node can't be null");
		}

		if(crt.getEndNode() == null) {
			throw new IllegalArgumentException("Can't create relation " + crt.getCode() + ": end node can't be null");
		}

		super.create(crt);

		try {
			permissionService.createIfAbsent(crt.getModifyPermission(), paramBean.getProperty("role.modifyAllCR", "ModifyAllCR"));
			permissionService.createIfAbsent(crt.getReadPermission(), paramBean.getProperty("role.readAllCR", "ReadAllCR"));
            for (var storage : crt.getAvailableStorages()) {
            	provider.findImplementation(storage).crtCreated(crt);
            }
			customFieldsCache.addUpdateCustomRelationshipTemplate(crt);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

		// Synchronize start and end CETs
		CustomEntityTemplate startCet = crt.getStartNode();
		MeveoModule cetModule = cetService.findModuleOf(startCet);
		cetService.addFilesToModule(startCet, cetModule);
		CustomEntityTemplate endCrt = crt.getEndNode();
		MeveoModule cet2Module = cetService.findModuleOf(endCrt);
		cetService.addFilesToModule(endCrt, cet2Module);

	}

	@Override
	public CustomRelationshipTemplate update(CustomRelationshipTemplate crt) throws BusinessException {
		if (!EntityCustomizationUtils.validateOntologyCode(crt.getCode())) {
			throw new IllegalArgumentException("The code of ontology elements must not contain numbers");
		}
		CustomRelationshipTemplate cetUpdated = super.update(crt);

		permissionService.createIfAbsent(crt.getModifyPermission(), paramBean.getProperty("role.modifyAllCR", "ModifyAllCR"));
		permissionService.createIfAbsent(crt.getReadPermission(), paramBean.getProperty("role.readAllCR", "ReadAllCR"));

        for (var storage : crt.getAvailableStorages()) {
        	provider.findImplementation(storage).crtUpdated(crt);
        }

		customFieldsCache.addUpdateCustomRelationshipTemplate(crt);

		CustomEntityTemplate startCet = crt.getStartNode();
		MeveoModule cetModule = cetService.findModuleOf(startCet);
		cetService.addFilesToModule(startCet, cetModule);
		CustomEntityTemplate endCrt = crt.getEndNode();
		MeveoModule cet2Module = cetService.findModuleOf(endCrt);
		cetService.addFilesToModule(endCrt, cet2Module);

		return cetUpdated;
	}

	/**
	 * Synchronize storages.
	 *
	 * @param crt the crt
	 * @throws BusinessException if we can't remove a storage
	 */
	public void synchronizeStorages(CustomRelationshipTemplate crt) throws BusinessException {
		// Synchronize custom fields storages with CRT available storages
		for (CustomFieldTemplate cft : customFieldTemplateService.findByAppliesToNoCache(crt.getAppliesTo()).values()) {
			if(cft.getStoragesNullSafe() == null){
				cft.setStorages(new ArrayList<>());
			}

			for (DBStorageType storage : new ArrayList<>(cft.getStoragesNullSafe())) {
				if (!crt.getAvailableStorages().contains(storage)) {
					log.info("Remove storage '{}' from CFT '{}' of CRT '{}'", storage, cft.getCode(), crt.getCode());
					cft.getStoragesNullSafe().remove(storage);
					customFieldTemplateService.update(cft);
				}
			}
		}
	}


	@Override
	public void remove(CustomRelationshipTemplate crt) throws BusinessException {
		Map<String, CustomFieldTemplate> fields = customFieldTemplateService.findByAppliesToNoCache(crt.getAppliesTo());

		for (CustomFieldTemplate cft : fields.values()) {
			customFieldTemplateService.remove(cft.getId());
		}

        for (var storage : crt.getAvailableStorages()) {
        	provider.findImplementation(storage).removeCrt(crt);
        }
        
        customFieldsCache.removeCustomRelationshipTemplate(crt);

		permissionService.removeIfPresent(crt.getModifyPermission());
		permissionService.removeIfPresent(crt.getReadPermission());

		super.remove(crt);

		CustomEntityTemplate startCet = crt.getStartNode();
		MeveoModule cetModule = cetService.findModuleOf(startCet);
		cetService.addFilesToModule(startCet, cetModule);
		CustomEntityTemplate endCrt = crt.getEndNode();
		MeveoModule cet2Module = cetService.findModuleOf(endCrt);
		cetService.addFilesToModule(endCrt, cet2Module);
	}

	/**
	 * Whether the relation is unique
	 *
	 * @param code Code of the relationship template
	 * @return {@code true} if the relationship is unique
	 */
	public boolean isUnique(String code){
		return uniqueRelations.computeIfAbsent(code, key -> {
			try {
				CriteriaBuilder cb = getEntityManager().getCriteriaBuilder();
				CriteriaQuery<Boolean> query = cb.createQuery(Boolean.class);
				Root<CustomRelationshipTemplate> root = query.from(getEntityClass());
				query.select(root.get("unique"));
				query.where(cb.equal(root.get("code"), key));
				query.distinct(true);
				return getEntityManager().createQuery(query).getSingleResult();
			} catch (NoResultException e) {
				return false;
			}
		});
	}

	/**
	 * Get a list of custom entity templates for cache
	 *
	 * @return A list of custom entity templates
	 */
	public List<CustomRelationshipTemplate> getCRTForCache() {
		return getEntityManager().createNamedQuery("CustomRelationshipTemplate.getCRTForCache", CustomRelationshipTemplate.class).getResultList();
	}

	/**
	 * Find entity by code
	 *
	 * @param code Code to match
	 */
	@Override
	public CustomRelationshipTemplate findByCode(String code){
		return super.findByCode(code, List.of("availableStorages"));
	}

	/**
	 * Find {@link CustomRelationshipTemplate} by start code, end code and name.
	 *
	 * @param startCode the start code
	 * @param endCode   the end code
	 * @param name      the name
	 * @return the query results
	 */
	public List<CustomRelationshipTemplate> findByStartEndAndName(String startCode, String endCode, String name){
		return getEntityManager().createNamedQuery("CustomRelationshipTemplate.findByStartEndAndName", CustomRelationshipTemplate.class)
				.setParameter("startCode", startCode)
				.setParameter("endCode", endCode)
				.setParameter("name", name)
				.getResultList();

	}

	/**
	 * Find all relationships related to a given custom entity template
	 *
	 * @param cet the custom entity template
	 * @param name the name of the relationship
	 * @return all relationships related to the entity template
	 */
	@SuppressWarnings("unchecked")
	public List<String> findByCetAndName(CustomEntityTemplate cet, String name) {
		String query = "WITH RECURSIVE ancestors AS (\n" +
				"   SELECT id, code, super_template_id FROM cust_cet\n" +
				"   WHERE id = :cetId\n" +
				"   UNION\n" +
				"      SELECT cet.id, cet.code, cet.super_template_id FROM cust_cet cet\n" +
				"      INNER JOIN ancestors s ON s.super_template_id = cet.id\n" +
				") \n" +
				"\n" +
				"SELECT crt.code FROM cust_crt crt\n" +
				"WHERE crt.name = :crtName\n" +
				"AND EXISTS(SELECT 1 FROM ancestors a WHERE crt.start_node_id = a.id OR crt.end_node_id = a.id)";

		List<Tuple> tuples = getEntityManager().createNativeQuery(query, Tuple.class)
				.setParameter("cetId", cet.getId())
				.setParameter("crtName", name)
				.getResultList();

		return tuples.stream().map(t -> t.get("code", String.class))
				.collect(Collectors.toList());

	}

	/**
	 * Find all relationships with the given name that links source and target
	 *
	 * @param source Code of the source template
	 * @param target Code of the target template
	 * @param name   Name of the relationships
	 * @return the matching relationships
	 */
	public List<CustomRelationshipTemplate> findByNameAndSourceOrTarget(String source, String target, String name) {
		return getEntityManager().createQuery("FROM CustomRelationshipTemplate crt "
								+ "WHERE crt.name = :name "
								+ "AND ("
								+ "    (crt.startNode.code = :source AND crt.endNode.code = :target)"
								+ "    OR (crt.startNode.code = :target AND crt.endNode.code = :source)"
								+ ")",
						CustomRelationshipTemplate.class)
				.setParameter("name", name)
				.setParameter("target", target)
				.setParameter("source", source)
				.getResultList();
	}

	/**
	 * Find all relationships that links source and target
	 *
	 * @param source Code of the source template
	 * @param target Code of the target template
	 * @return the matching relationships
	 */
	public List<CustomRelationshipTemplate> findBySourceOrTarget(String source, String target) {
		return getEntityManager().createQuery("FROM CustomRelationshipTemplate crt "
								+ "WHERE crt.startNode.code = :source AND crt.endNode.code = :target "
								+ "OR (crt.startNode.code = :target AND crt.endNode.code = :source)",
						CustomRelationshipTemplate.class)
				.setParameter("target", target)
				.setParameter("source", source)
				.getResultList();
	}

	/**
	 * Get directory the java or the json schema of a crt
	 * @param crt code your a looking for
	 * @param extension the file you target (json or java)
	 * @return file
	 */
	public File getCrtDir(CustomRelationshipTemplate crt, String extension) {
		File repositoryDir;
		String path;
		String directory = "";

		if (extension == "json") {
			directory = "/facets/json";
		}else if (extension == "java") {
			directory = "/facets/java/org/meveo/model/customEntities";// + crt.getClass().getAnnotation(ModuleItem.class).path();
		}
		MeveoModule module = customRelationshipTemplateService.findModuleOf(crt);
		if (module == null) {
			repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode());
			path = directory;
		} else {
			repositoryDir = GitHelper.getRepositoryDir(currentUser, module.getGitRepository().getCode());
			path = directory;
		}
		return new File(repositoryDir, path);
	}

	@Override
	public void onAddToModule(CustomRelationshipTemplate entity, MeveoModule module) throws BusinessException {
		super.onAddToModule(entity, module);

		for (var cft : customFieldTemplateService.findByAppliesTo(entity.getAppliesTo()).values()) {
			meveoModuleService.addModuleItem(new MeveoModuleItem(cft), module);
		}
	}


	@Override
	public void addFilesToModule(CustomRelationshipTemplate entity, MeveoModule module) throws BusinessException {
		super.addFilesToModule(entity, module);

		File gitDirectory = GitHelper.getRepositoryDir(currentUser, module.getGitRepository().getCode());
		String pathJavaFile = "facets/java/org/meveo/model/customEntities/" + entity.getCode() + ".java";
		String pathJsonSchemaFile = "facets/json/" + entity.getCode() + "-schema" + ".json";

		File newJavaFile = new File (gitDirectory, pathJavaFile);
		File newJsonSchemaFile = new File(gitDirectory, pathJsonSchemaFile);

		try {
			MeveoFileUtils.writeAndPreserveCharset(this.jSONSchemaGenerator.generateSchema(pathJsonSchemaFile, entity), newJsonSchemaFile);
		} catch (IOException e) {
			throw new BusinessException("File cannot be write", e);
		}

		String message = "Add the crt json schema : " + entity.getCode()+".json" + " in the module : " + module.getCode();
		try {
			message+=" "+commitMessageBean.getCommitMessage();
		} catch (ContextNotActiveException e) {
			log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
		}
		gitClient.commitFiles(module.getGitRepository(), Collections.singletonList(newJsonSchemaFile), message);

		String schemaLocation = this.cetCompiler.getTemplateSchema(entity);

		final CompilationUnit compilationUnit = this.jSONSchemaIntoJavaClassParser.parseJsonContentIntoJavaFile(schemaLocation, entity);

		try {
			MeveoFileUtils.writeAndPreserveCharset(compilationUnit.toString(), newJavaFile);
		} catch (IOException e) {
			throw new BusinessException("File cannot be write", e);
		}

		message = "Add the crt java source file : " + entity.getCode()+".java" + "in the module : " + module.getCode();
		try {
			message+=" "+commitMessageBean.getCommitMessage();
		} catch (ContextNotActiveException e) {
			log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
		}
		gitClient.commitFiles(module.getGitRepository(), Collections.singletonList(newJavaFile), message);
	}

	@Override
	public void moveFilesToModule(CustomRelationshipTemplate entity, MeveoModule oldModule, MeveoModule newModule) throws BusinessException, IOException {
		super.moveFilesToModule(entity, oldModule, newModule);

		// Move CFTs at the same time
		for (CustomFieldTemplate cft : customFieldTemplateService.findByAppliesTo(entity.getAppliesTo()).values()) {
			customFieldTemplateService.moveFilesToModule(cft, oldModule, newModule);
		}
	}

	@Override
	public void removeFilesFromModule(CustomRelationshipTemplate entity, MeveoModule module) throws BusinessException {
		super.removeFilesFromModule(entity, module);
		
        List<File> fileList = new ArrayList<>();

        final File cftDir = new File(GitHelper.getRepositoryDir(null, module.getCode()), "customFieldTemplates/" + entity.getAppliesTo());
        if (cftDir.exists()) {
	        for (File cftFile : cftDir.listFiles()) {
	        	cftFile.delete();
	        	fileList.add(cftFile);
	        }
	        cftDir.delete();
	        fileList.add(cftDir);
        }
        
        if(!fileList.isEmpty()) {
            String message = "Deleted custom relationship template " + entity.getCode();
            try {
                message+=" "+commitMessageBean.getCommitMessage();
            } catch (ContextNotActiveException e) {
                log.warn("No active session found for getting commit message when  "+message+" to "+module.getCode());
            }
            gitClient.commitFiles(meveoRepository, fileList,message);
        }
	}

	@Override
	protected BaseEntityDto getDto(CustomRelationshipTemplate entity) throws BusinessException {
		CustomRelationshipTemplateDto dto = (CustomRelationshipTemplateDto) super.getDto(entity);
		dto.setFields(null);
		return dto;
	}
}