/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.api.observers;

import java.io.File;
import java.io.IOException;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.security.CodeSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.PostConstruct;
import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Schedule;
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.CustomEntityTemplateApi;
import org.meveo.api.CustomRelationshipTemplateApi;
import org.meveo.api.dto.CustomEntityTemplateDto;
import org.meveo.api.dto.CustomRelationshipTemplateDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.commons.utils.ParamBean;
import org.meveo.commons.utils.StringUtils;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.event.qualifier.git.CommitEvent;
import org.meveo.event.qualifier.git.CommitReceived;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.model.persistence.sql.SQLStorageConfiguration;
import org.meveo.persistence.neo4j.service.graphql.GraphQLService;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.crm.impl.JSONSchemaGenerator;
import org.meveo.service.crm.impl.JSONSchemaIntoJavaClassParser;
import org.meveo.service.crm.impl.JSONSchemaIntoTemplateParser;
import org.meveo.service.custom.CustomEntityTemplateCompiler;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
import org.meveo.service.custom.CustomTableCreatorService;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.MeveoRepository;
import org.meveo.service.script.CustomScriptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.Log;

/**
 * Observer that updates IDL definitions when a CET, CRT or CFT changes
 *
 * @since 6.0.13
 * @version 6.9.0
 * @author Clément Bareth
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class OntologyObserver {

    private static Logger LOGGER = LoggerFactory.getLogger(OntologyObserver.class);
    
    @Inject
    private GitClient gitClient;

    @Inject
    @MeveoRepository
    private GitRepository meveoRepository;

    @Inject
    @CurrentUser
    private MeveoUser currentUser;

    @Inject
    private JSONSchemaGenerator jsonSchemaGenerator;

    @Inject
    private CustomFieldsCacheContainerProvider cache;

    @Inject
    private JSONSchemaIntoTemplateParser jsonSchemaIntoTemplateParser;

    @Inject
    private JSONSchemaIntoJavaClassParser jsonSchemaIntoJavaClassParser;

    @Inject
    private CustomEntityTemplateApi customEntityTemplateApi;
    
    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Inject
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    private CustomRelationshipTemplateApi customRelationshipTemplateApi;
    
    @Inject
    private CustomEntityTemplateCompiler cetCompiler;
    
    @Inject
    private GraphQLService graphQlService;
    
    @Inject
    private CustomTableCreatorService customTableCreatorService;

    private AtomicBoolean hasChange = new AtomicBoolean(true);
    
    /**
     * At startup, update the IDL definitions
     */
    @PostConstruct
    public void init() {
        try {
            updateIDL();
            CustomScriptService.constructClassPath();
        } catch (IOException e) {
        }
    }

    /**
     * Every minute, check if some element of ontology have changed. If it does, update the IDL definitions.
     */
    @Schedule(minute = "*/1", hour = "*", persistent = false)
    @Asynchronous
    public void updateIDL() {
        if (hasChange.get()) {
            hasChange.set(false);
            
            ParamBean instance = ParamBean.getInstance();
			boolean updateGraphQlOnChange = Boolean.parseBoolean(instance.getProperty("meveo.graphql.updateOnChange", "true"));
            if(updateGraphQlOnChange) {
	            LOGGER.info("Ontology has changed, updating IDL definitions");
	            try {
	            	graphQlService.updateIDL();
	            } catch (Exception e) {
	            	LOGGER.error("Fail to update graphql definition", e);
	            	instance.setProperty("meveo.graphql.updateOnChange", "false");
	            	instance.saveProperties();
	            }
	        }
        }
    }

    /* ------------ CET Notifications ------------ */

    /**
     * When a {@link CustomEntityTemplate} is created, create the corresponding JSON Schema and commit it in the meveo directory
     * <br>
     * Note : must run in the current transaction, otherwise scripts relying on it will fail to compile
     * 
     * @param cet The created {@link CustomEntityTemplate}
     * @throws IOException       if we cannot create / write to the JSON Schema file
     * @throws BusinessException if the json schema file already exists
     */
    public void cetCreated(@Observes @Created CustomEntityTemplate cet) throws IOException, BusinessException {
    	hasChange.set(true);

        List<File> commitFiles = new ArrayList<>();

        final String templateSchema = cetCompiler.getTemplateSchema(cet);

        final File cetJsonDir = cetCompiler.getJsonCetDir(cet);
        
        final File cetJavaDir = cetCompiler.getJavaCetDir(cet);

        if (!cetJsonDir.exists()) {
            cetJsonDir.mkdirs();
            commitFiles.add(cetJsonDir);
        }
        if (!cetJavaDir.exists()) {
        	cetJavaDir.mkdir();
        	commitFiles.add(cetJavaDir);
        }

        File schemaFile = new File(cetJsonDir, cet.getCode() + "-schema.json");
        FileUtils.write(schemaFile, templateSchema, StandardCharsets.UTF_8);
        commitFiles.add(schemaFile);

        final CompilationUnit compilationUnit = jsonSchemaIntoJavaClassParser.parseJsonContentIntoJavaFile(templateSchema, cet);
        
        File javaFile = new File(cetJavaDir, cet.getCode() + ".java");
        FileUtils.write(javaFile, compilationUnit.toString(), StandardCharsets.UTF_8);
        commitFiles.add(javaFile);

        gitClient.commitFiles(meveoRepository, commitFiles, "Created custom entity template " + cet.getCode());
    }
    
    /**
     * Removes the files created by {@link #cetCreated(CustomEntityTemplate)} if the transaction fails
     * 
     * @see #cetCreated(CustomEntityTemplate)
     * @param cet The CET which failed to get created
     * @throws BusinessException if error occurs
     */
    public void cetCreationFailure(@Observes(during = TransactionPhase.AFTER_FAILURE) @Created CustomEntityTemplate cet) throws BusinessException {
        List<File> commitFiles = new ArrayList<>();
        final File cetJsonDir = cetCompiler.getJsonCetDir(cet);
        final File cetJavaDir = cetCompiler.getJavaCetDir(cet);
        if (!cetJsonDir.exists()) {
            return;
        }
        if (!cetJavaDir.exists()) {
        	return;
        }
        
        File schemaFile = new File(cetJsonDir, cet.getCode() + ".json");
        if(schemaFile.exists()) {
        	schemaFile.delete();
        }
        commitFiles.add(schemaFile);
        File javaFile = new File(cetJavaDir, cet.getCode() + ".java");
        if(javaFile.exists()) {
        	javaFile.delete();
        }
        commitFiles.add(javaFile);
        gitClient.commitFiles(meveoRepository, commitFiles, "Revert creation of custom entity template " + cet.getCode());
    
        // Remove table and sequence on failure
        if(cet.isStoreAsTable()) {
        	customTableCreatorService.removeTable(SQLStorageConfiguration.getDbTablename(cet));
        }
    }
    

    /**
     * When a {@link CustomEntityTemplate} is updated, update the corresponding JSON Schema and commit it in the meveo directory
     *
     * @param cet The updated {@link CustomEntityTemplate}
     * @throws IOException if we cannot write to the JSON Schema file
     * @throws BusinessException if an error happen during the creation of the related files
     */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cetUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated CustomEntityTemplate cet) throws
            IOException, BusinessException {
    	
    	MeveoModule module = customEntityTemplateService.findModuleOf(cet);
		
    	hasChange.set(true);

        final String templateSchema = cetCompiler.getTemplateSchema(cet);

        final File cetJsonDir = cetCompiler.getJsonCetDir(cet);
        final File cetJavaDir = cetCompiler.getJavaCetDir(cet);

        // This is for retro-compatibility, in case a CET created before 6.4.0 is updated
        if (!cetJsonDir.exists()) {
            cetJsonDir.mkdirs();
        }
        if (!cetJavaDir.exists()) {
            cetJavaDir.mkdirs();
        }
        
        List<File> fileList = new ArrayList<>();

        File schemaFile = new File(cetJsonDir, cet.getCode() + "-schema.json");
        if (schemaFile.exists()) {
            schemaFile.delete();
            fileList.add(schemaFile);
        }
        FileUtils.write(schemaFile, templateSchema, StandardCharsets.UTF_8);

        // Update java source file in git repository
        File javaFile = cetCompiler.generateCETSourceFile(templateSchema, cet);
        fileList.add(javaFile);
        if (module == null) {
        	gitClient.commitFiles(meveoRepository, fileList, "Updated custom entity template " + cet.getCode());
        } else {
        	gitClient.commitFiles(module.getGitRepository(), fileList, "Update custom entity template " + cet.getCode());
        }
//        String sourceCode = Files.readString(javaFile.toPath());
//        File classFile = new File(classDir, "org/meveo/model/customEntities/" + cet.getCode() + ".java");
//        FileUtils.write(classFile, sourceCode, StandardCharsets.UTF_8);
    }

    /**
     * When a {@link CustomEntityTemplate} is removed, remove the corresponding JSON Schema and commit changes in the meveo directory
     *
     * @param cet The removed {@link CustomEntityTemplate}
     * @throws BusinessException if we failed to commit the deletion
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cetRemoved(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed CustomEntityTemplate cet) throws BusinessException {
        final File cetJsonDir = cetCompiler.getJsonCetDir(cet);
        final File cetJavaDir = cetCompiler.getJavaCetDir(cet);
        final File classDir = getClassDir();
        List<File> fileList = new ArrayList<>();

        final File schemaFile = new File(cetJsonDir, cet.getCode() + "-schema.json");
        if (schemaFile.exists()) {
            schemaFile.delete();
            fileList.add(schemaFile);
        }

        final File javaFile = new File(cetJavaDir, cet.getCode() + ".java");
        if (javaFile.exists()) {
            javaFile.delete();
            fileList.add(javaFile);
        }

        final File classFile = new File(classDir, "org/meveo/model/customEntities/" + cet.getCode() + ".class");
        if (classFile.exists()) {
            classFile.delete();
        }
        
        if(!fileList.isEmpty()) {
        	gitClient.commitFiles(meveoRepository, fileList, "Deleted custom entity template " + cet.getCode());
        }
    }

    /* ------------ CRT Notifications ------------ */

    /**
     * When a {@link CustomRelationshipTemplate} is created, create the corresponding JSON Schema and commit it in the meveo directory
     *
     * @param crt The created {@link CustomRelationshipTemplate}
     * @throws IOException       if we cannot create / write to the JSON Schema file
     * @throws BusinessException if exception occurs
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void crtCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created CustomRelationshipTemplate crt) throws IOException, BusinessException {
        hasChange.set(true);

        List<File> commitFiles = new ArrayList<>();

        final String templateSchema = getTemplateSchema(crt);

        final File crtDir = customRelationshipTemplateService.getCrtDir(crt);

        if (!crtDir.exists()) {
            crtDir.mkdirs();
            commitFiles.add(crtDir);
        }

        File schemaFile = new File(crtDir, crt.getCode() + "-schema.json");
        if (schemaFile.exists()) {
        	schemaFile.delete();
        }
        
        File javaFile = cetCompiler.generateCRTSourceFile(templateSchema, crt);
        commitFiles.add(javaFile);

        FileUtils.write(schemaFile, templateSchema, StandardCharsets.UTF_8);
        commitFiles.add(schemaFile);

        gitClient.commitFiles(meveoRepository, commitFiles, "Created custom relationship template " + crt.getCode());
    }

    /**
     * When a {@link CustomRelationshipTemplate} is updated, update the corresponding JSON Schema and commit it in the meveo directory
     *
     * @param crt The updated {@link CustomRelationshipTemplate}
     * @throws IOException if we cannot write to the JSON Schema file
     * @throws BusinessException if an error happen during the creation of the related files
     */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void crtUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated CustomRelationshipTemplate crt) throws IOException, BusinessException {
        
    	MeveoModule module = customRelationshipTemplateService.findModuleOf(crt);
    	
    	hasChange.set(true);

        
        final String templateSchema = getTemplateSchema(crt);

        final File crtDir = customRelationshipTemplateService.getCrtDir(crt);

        // This is for retro-compatibility, in case a CRT created before 6.4.0 is updated
        if (!crtDir.exists()) {
            crtDir.mkdirs();
        }

        File schemaFile = new File(crtDir, crt.getCode() + "-schema.json");
        if (schemaFile.exists()) {
            schemaFile.delete();
        }
        
        File javaFile = cetCompiler.generateCRTSourceFile(templateSchema, crt);

        //Update the origin CET when the CRT is modified
        //If a CFT is modified in the CRT, the origin CET need to be modified too
        CustomEntityTemplate cet = customEntityTemplateService.findById(crt.getStartNode().getId());
        cetUpdated(cet);
        
        FileUtils.write(schemaFile, templateSchema, StandardCharsets.UTF_8);
        
        if (module == null) {
        	gitClient.commitFiles(meveoRepository, List.of(schemaFile, javaFile), "Updated custom relationship template " + crt.getCode());
        } else {
        	gitClient.commitFiles(module.getGitRepository(), List.of(schemaFile, javaFile), "Updated custom relationship template " + crt.getCode());
        }
    }

    /**
     * When a {@link CustomRelationshipTemplate} is removed, remove the corresponding JSON Schema and commit changes in the meveo directory
     *
     * @param crt The removed {@link CustomRelationshipTemplate}
     * @throws BusinessException if we failed to commit the deletion
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void crtRemoved(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed CustomRelationshipTemplate crt) throws BusinessException {
        final File cetDir = customRelationshipTemplateService.getCrtDir(crt);
        final File schemaFile = new File(cetDir, crt.getCode() + "-schema.json");
        if (schemaFile.exists()) {
            schemaFile.delete();
        }
        
        final File javaFile = new File(cetDir, crt.getCode() + ".java");
        if (javaFile.exists()) {
            javaFile.delete();
        }

        gitClient.commitFiles(meveoRepository, List.of(schemaFile, javaFile), "Deleted custom relationship template " + crt.getCode());
    }

    /* ------------ CFT Notifications ------------ */

    /**
     * When a {@link CustomFieldTemplate} is created, update the corresponding JSON Schema of the related CET / CFT
     * and commit it in the meveo directory.
     * 
     * Note : must run in the current transaction, otherwise scripts relying on it will fail to compile
     *
     * @param cft The created {@link CustomFieldTemplate}
     * @throws IOException if we cannot write to the JSON Schema file
     * @throws BusinessException if an error happen during the creation of the related files
     */
    public void cftCreated(@Observes @Created CustomFieldTemplate cft) throws IOException, BusinessException {
        if(cft.isInDraft()) {
        	return;
        }
        
    	hasChange.set(true);

        if (cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            CustomEntityTemplate cet = cache.getCustomEntityTemplate(CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            final File cetJsonDir = cetCompiler.getJsonCetDir(cet);
            final File cetJavaDir = cetCompiler.getJavaCetDir(cet);

            // This is for retro-compatibility, in case a we add a field to a CET created before 6.4.0
            if (!cetJsonDir.exists()) {
                cetJsonDir.mkdirs();
            }
            if (!cetJavaDir.exists()) {
                cetJavaDir.mkdirs();
            }

            List<File> fileList = new ArrayList<>();
            File schemaFile = new File(cetJsonDir, cet.getCode() + "-schema.json");
            File javaFile = new File(cetJavaDir, cet.getCode() + ".java");

            if (schemaFile.exists()) {
                schemaFile.delete();
                fileList.add(schemaFile);
                final String templateSchema = cetCompiler.getTemplateSchema(cet);
                FileUtils.write(schemaFile, templateSchema, StandardCharsets.UTF_8);

                if (javaFile.exists()) {
                    javaFile.delete();
                    fileList.add(javaFile);
                    CompilationUnit compilationUnit = jsonSchemaIntoJavaClassParser.parseJsonContentIntoJavaFile(templateSchema, cet);
                    FileUtils.write(javaFile, compilationUnit.toString(), StandardCharsets.UTF_8);
                }

                gitClient.commitFiles(
                        meveoRepository,
                        fileList,
                        "Add property " + cft.getCode() + " to CET " + cet.getCode()
                );
            }

        } else if (cft.getAppliesTo().startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
            CustomRelationshipTemplate crt = cache.getCustomRelationshipTemplate(cft.getAppliesTo().replaceAll("CRT_(.*)", "$1"));
            final File cetDir = customRelationshipTemplateService.getCrtDir(crt);

            // This is for retro-compatibility, in case a we add a field to a CET created before 6.4.0
            if (!cetDir.exists()) {
                cetDir.mkdirs();
            }

            File schemaFile = new File(cetDir, crt.getCode() + ".json");

            if (schemaFile.exists()) {
                schemaFile.delete();
                final String templateSchema = getTemplateSchema(crt);

                FileUtils.write(schemaFile, templateSchema, StandardCharsets.UTF_8);
                gitClient.commitFiles(
                        meveoRepository,
                        Collections.singletonList(schemaFile),
                        "Add property " + cft.getCode() + " to CRT " + crt.getCode()
                );
            }
        }
    }

    /**
     * When a {@link CustomFieldTemplate} is updated, update the corresponding JSON Schema of the related CET / CFT
     * and commit it in the meveo directory
     *
     * @param cft The updated {@link CustomFieldTemplate}
     * @throws IOException if we cannot write to the JSON Schema file
     * @throws BusinessException if an error happen during the creation of the related files
     */
	@TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cftUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated CustomFieldTemplate cft) throws IOException, BusinessException {
        
    	MeveoModule module = customFieldTemplateService.findModuleOf(cft);
    	
    	hasChange.set(true);

        if (cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            CustomEntityTemplate cet = cache.getCustomEntityTemplate(CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            final File cetJsonDir = cetCompiler.getJsonCetDir(cet);
            final File cetJavaDir = cetCompiler.getJavaCetDir(cet);

            final File classDir = getClassDir();

            // This is for retro-compatibility, in case we update a field of a CET created before 6.4.0
            if (!cetJsonDir.exists()) {
                cetJsonDir.mkdirs();
            }
            if (!cetJavaDir.exists()) {
                cetJavaDir.mkdirs();
            }

            List<File> listFile = new ArrayList<>();
            File schemaFile = new File(cetJsonDir, cet.getCode() + "-schema.json");
            File javaFile = new File(cetJavaDir, cet.getCode() + ".java");

            if (schemaFile.exists()) {
                schemaFile.delete();
                listFile = updateCetFiles(cet, classDir, schemaFile, javaFile);

                listFile.add(schemaFile);

                if (module == null) {
	                gitClient.commitFiles(
	                        meveoRepository,
	                        listFile,
	                        "Update property " + cft.getCode() + " of CET " + cet.getCode()
	                );
                } else {
                	gitClient.commitFiles(
                			module.getGitRepository(),
                			listFile,
                			"Update property " + cft.getCode() + "of CET " + cet.getCode()
                	);
                }

            }

        } else if (cft.getAppliesTo().startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
            CustomRelationshipTemplate crt = cache.getCustomRelationshipTemplate(cft.getAppliesTo().replaceAll("CRT_(.*)", "$1"));
            final File cetDir = customRelationshipTemplateService.getCrtDir(crt);

            // This is for retro-compatibility, in case we update a field of a CET created before 6.4.0
            if (!cetDir.exists()) {
                cetDir.mkdirs();
            }

            File schemaFile = new File(cetDir, crt.getCode() + ".json");

            if (schemaFile.exists()) {
                schemaFile.delete();
                final String templateSchema = getTemplateSchema(crt);

                FileUtils.write(schemaFile, templateSchema, StandardCharsets.UTF_8);
                
                //Update the origin CET when the CFT is modified
                //If a CFT is modified in the CRT, the origin CET need to be modified too
                CustomEntityTemplate cet = customEntityTemplateService.findById(crt.getStartNode().getId());
                cetUpdated(cet);
                
                gitClient.commitFiles(
                        meveoRepository,
                        Collections.singletonList(schemaFile),
                        "Update property " + cft.getCode() + " of CRT " + crt.getCode()
                );
            }
        }
    }

    /**
     * When a {@link CustomFieldTemplate} is removed, update the corresponding JSON Schema of the related CET / CFT
     * and commit it in the meveo directory
     *
     * @param cft The removed {@link CustomFieldTemplate}
     * @throws IOException if we can't create or delete related files
     * @throws BusinessException if we can't create or update related file contents
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cftRemoved(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed CustomFieldTemplate cft) throws BusinessException, IOException {
        if (cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            CustomEntityTemplate cet = cache.getCustomEntityTemplate(CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            if (cet == null) {
                // CET already deleted
                return;
            }

            final File cetJsonDir = cetCompiler.getJsonCetDir(cet);
            final File cetJavaDir = cetCompiler.getJavaCetDir(cet);

            final File classDir = getClassDir();

            if (!cetJsonDir.exists()) {
                // Nothing to delete
                return;
            }
            if (!cetJavaDir.exists()) {
                // Nothing to delete
                return;
            }

            File schemaFile = new File(cetJsonDir, cet.getCode() + "-schema.json");
            File javaFile = new File(cetJavaDir, cet.getCode() + ".java");

            if (schemaFile.exists()) {
                schemaFile.delete();
                updateCetFiles(cet, classDir, schemaFile, javaFile);

                gitClient.commitFiles(
                        meveoRepository,
                        Arrays.asList(schemaFile, javaFile),
                        "Remove property " + cft.getCode() + " of CET " + cet.getCode()
                );

            }

        } else if (cft.getAppliesTo().startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
            CustomRelationshipTemplate crt = cache.getCustomRelationshipTemplate(cft.getAppliesTo().replaceAll("CRT_(.*)", "$1"));
            if (crt == null) {
                // CET already deleted
                return;
            }

            final File cetDir = customRelationshipTemplateService.getCrtDir(crt);

            if (!cetDir.exists()) {
                // Nothing to delete
                return;
            }

            File schemaFile = new File(cetDir, crt.getCode() + ".json");

            if (schemaFile.exists()) {
                schemaFile.delete();

                gitClient.commitFiles(
                        meveoRepository,
                        Collections.singletonList(schemaFile),
                        "Remove property " + cft.getCode() + " of CRT " + crt.getCode()
                );
            }
        }
    }

    /**
     * When a commit concerning cet is received :
     * <ul>
     * <li>If cet file has been created, create the JPA entity</li>
     * <li>If cet file has been modified, re-compile it</li>
     * <li>If cet file has been deleted, remove the JPA entity</li>
     * </ul>
     * @param commitEvent the data of the received commit
     * @throws BusinessException if cets files can't be updated
     * @throws MeveoApiException if cets can't be updated
     */
    public void onCETsChanged(@Observes @CommitReceived CommitEvent commitEvent) throws BusinessException, MeveoApiException {
        if (commitEvent.getGitRepository().getCode().equals(meveoRepository.getCode())) {
            for (String modifiedFile : commitEvent.getModifiedFiles()) {
                String[] cet = modifiedFile.split("/");
                String fileName = cet[cet.length - 1];
                String templateType = null;
				try {
					templateType = cet[cet.length - 2];

				} catch (ArrayIndexOutOfBoundsException e) {
					LOGGER.debug("Not an entity {}", modifiedFile);
				}
                
                if (!StringUtils.isBlank(fileName) && fileName.toLowerCase().endsWith("json") && templateType.equals("entities")) {
                    String[] cetFileName = fileName.split("\\.");
                    String code = cetFileName[0];
                    CustomEntityTemplate customEntityTemplate = customEntityTemplateService.findByCode(code);
                    File repositoryDir = GitHelper.getRepositoryDir(currentUser, commitEvent.getGitRepository().getCode() + "/src/main/java/");
                    File cetFile = new File(repositoryDir, modifiedFile);
                    if (customEntityTemplate == null) {
                        String absolutePath = cetFile.getAbsolutePath();
                        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile(absolutePath);
                        customEntityTemplateApi.create(customEntityTemplateDto);
                    } else if (customEntityTemplate != null && !cetFile.exists()) {
                        customEntityTemplateApi.removeEntityTemplate(code);
                    } else if (customEntityTemplate != null && cetFile.exists()) {
                        CustomEntityTemplateDto customEntityTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFile(cetFile.getAbsolutePath());
                        customEntityTemplateApi.updateEntityTemplate(customEntityTemplateDto);
                    }
                }
            }
        }
    }

    /**
     * When a commit concerning crt is received :
     * <ul>
     * <li>If crt file has been created, create the JPA entity</li>
     * <li>If crt file has been modified, re-compile it</li>
     * <li>If crt file has been deleted, remove the JPA entity</li>
     * </ul>
     * @param commitEvent the data of the received commit
     * @throws BusinessException if crts files can't be updated
     * @throws MeveoApiException if crt can't be updated
     */
    public void onCRTsChanged(@Observes @CommitReceived CommitEvent commitEvent) throws BusinessException, MeveoApiException {
        if (commitEvent.getGitRepository().getCode().equals(meveoRepository.getCode())) {
            for (String modifiedFile : commitEvent.getModifiedFiles()) {
                String[] crt = modifiedFile.split("/");
                String fileName = crt[crt.length - 1];
                
                String templateType = null;
				try {
					templateType = crt[crt.length - 2];

				} catch (ArrayIndexOutOfBoundsException e) {
					LOGGER.debug("Not an entity {}", modifiedFile);
				}
				
                if (!StringUtils.isBlank(fileName) && fileName.toLowerCase().endsWith("json") && templateType.equals("relationships")) {
                    String[] crtFileName = fileName.split("\\.");
                    String code = crtFileName[0];
                    CustomRelationshipTemplate customRelationshipTemplate = customRelationshipTemplateService.findByCode(code);
                    File repositoryDir = GitHelper.getRepositoryDir(currentUser, commitEvent.getGitRepository().getCode());
                    File crtFile = new File(repositoryDir, modifiedFile);
                    if (customRelationshipTemplate == null) {
                        String absolutePath = crtFile.getAbsolutePath();
                        CustomRelationshipTemplateDto customRelationshipTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFileIntoCRT(absolutePath);
                        if (customRelationshipTemplateDto.getStartNodeCode() != null) {    // Make sure we parsed a valid CRT and not a CET
                            customRelationshipTemplateApi.createCustomRelationshipTemplate(customRelationshipTemplateDto);
                        }
                    } else if (customRelationshipTemplate != null && !crtFile.exists()) {
                        customRelationshipTemplateApi.removeCustomRelationshipTemplate(code);
                    } else if (customRelationshipTemplate != null && crtFile.exists()) {
                        CustomRelationshipTemplateDto customRelationshipTemplateDto = jsonSchemaIntoTemplateParser.parseJsonFromFileIntoCRT(crtFile.getAbsolutePath());
                        customRelationshipTemplateApi.updateCustomRelationshipTemplate(customRelationshipTemplateDto);
                    }
                }
            }
        }
    }
    
    private String getTemplateSchema(CustomRelationshipTemplate crt) {
        String schema = jsonSchemaGenerator.generateSchema(crt.getCode(), crt);
        return schema.replaceAll("#/definitions", "../entities");
    }

    private File getClassDir() {
        final File classDir = CustomEntityTemplateService.getClassesDir(currentUser);
        return classDir;
    }
    
    /*
     * @return the list of updated files
     */
	private List<File> updateCetFiles(CustomEntityTemplate cet, final File classDir, File schemaFile, File javaFile) throws IOException {
		List<File> listFile = new ArrayList<>();
		final String templateSchema = cetCompiler.getTemplateSchema(cet);
		FileUtils.write(schemaFile, templateSchema, StandardCharsets.UTF_8);

		if (javaFile.exists()) {
		    javaFile.delete();
		    CompilationUnit compilationUnit = jsonSchemaIntoJavaClassParser.parseJsonContentIntoJavaFile(templateSchema, cet);
		    FileUtils.write(javaFile, compilationUnit.toString(), StandardCharsets.UTF_8);
		    listFile.add(javaFile);
		}
		
		return listFile;
	}
}
