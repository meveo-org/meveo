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
import javax.ejb.Singleton;
import javax.ejb.Startup;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;

import org.apache.commons.io.FileUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.CustomEntityTemplateApi;
import org.meveo.api.CustomRelationshipTemplateApi;
import org.meveo.api.dto.CustomEntityTemplateDto;
import org.meveo.api.dto.CustomRelationshipTemplateDto;
import org.meveo.api.exception.MeveoApiException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
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
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.crm.impl.JSONSchemaGenerator;
import org.meveo.service.crm.impl.JSONSchemaIntoJavaClassParser;
import org.meveo.service.crm.impl.JSONSchemaIntoTemplateParser;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.custom.CustomRelationshipTemplateService;
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
    private CustomEntityTemplateService customEntityTemplateService;

    @Inject
    private CustomRelationshipTemplateService customRelationshipTemplateService;

    @Inject
    private CustomRelationshipTemplateApi customRelationshipTemplateApi;

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
     * Every 5 minutes, check if some element of ontology have changed. If it does, update the IDL definitions.
     */
    //@Schedule(minute = "*/5", hour = "*", persistent = false)
    @Asynchronous
    public void updateIDL() {
        // log.debug("Checking for ontology changes");
        if (hasChange.get()) {
            hasChange.set(false);
            // log.info("Ontology has changed, updating IDL definitions");
            // graphQLService.updateIDL(); TODO: Reactive once graphql validation feature is done
        }
    }

    /* ------------ CET Notifications ------------ */

    /**
     * When a {@link CustomEntityTemplate} is created, create the corresponding JSON Schema and commit it in the meveo directory
     *
     * @param cet The created {@link CustomEntityTemplate}
     * @throws IOException       if we cannot create / write to the JSON Schema file
     * @throws BusinessException if the json schema file already exists
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cetCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created CustomEntityTemplate cet) throws IOException, BusinessException {
        hasChange.set(true);

        List<File> commitFiles = new ArrayList<>();

        final String templateSchema = getTemplateSchema(cet);

        final File cetDir = getCetDir();

        final File classDir = getClassDir();

        if (!cetDir.exists()) {
            cetDir.mkdirs();
            commitFiles.add(cetDir);
        }

        File schemaFile = new File(cetDir, cet.getCode() + ".json");
        if (schemaFile.exists()) {
            throw new BusinessException("Schema for CET " + cet.getCode() + " already exists");
        }

        FileUtils.write(schemaFile, templateSchema);
        commitFiles.add(schemaFile);

        final CompilationUnit compilationUnit = jsonSchemaIntoJavaClassParser.parseJsonContentIntoJavaFile(templateSchema);

        File javaFile = new File(cetDir, cet.getCode() + ".java");
        if (javaFile.exists()) {
            throw new BusinessException("Java class file from CET " + cet.getCode() + " already exists");
        }
        FileUtils.write(javaFile, compilationUnit.toString());
        commitFiles.add(javaFile);

        File classFile = new File(classDir, "org/meveo/model/customEntities/" + cet.getCode() + ".java");
        FileUtils.write(classFile, compilationUnit.toString());
        compileClassJava(classDir, compilationUnit.toString(), classFile);
        gitClient.commitFiles(meveoRepository, commitFiles, "Created custom entity template " + cet.getCode());
    }

    /**
     * When a {@link CustomEntityTemplate} is updated, update the corresponding JSON Schema and commit it in the meveo directory
     *
     * @param cet The updated {@link CustomEntityTemplate}
     * @throws IOException if we cannot write to the JSON Schema file
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cetUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated CustomEntityTemplate cet) throws
            IOException, BusinessException {
        hasChange.set(true);

        final String templateSchema = getTemplateSchema(cet);

        final File cetDir = getCetDir();

        final File classDir = getClassDir();

        // This is for retro-compatibility, in case a CET created before 6.4.0 is updated
        if (!cetDir.exists()) {
            cetDir.mkdirs();
        }

        List<File> fileList = new ArrayList<>();

        File schemaFile = new File(cetDir, cet.getCode() + ".json");
        if (schemaFile.exists()) {
            schemaFile.delete();
            fileList.add(schemaFile);
        }

        FileUtils.write(schemaFile, templateSchema);

        final CompilationUnit compilationUnit = jsonSchemaIntoJavaClassParser.parseJsonContentIntoJavaFile(templateSchema);
        File javaFile = new File(cetDir, cet.getCode() + ".java");
        if (javaFile.exists()) {
            javaFile.delete();
            fileList.add(javaFile);
        }

        FileUtils.write(javaFile, compilationUnit.toString());

        File classFile = new File(classDir, "org/meveo/model/customEntities/" + cet.getCode() + ".java");
        FileUtils.write(classFile, compilationUnit.toString());
        compileClassJava(classDir, compilationUnit.toString(), classFile);

        gitClient.commitFiles(meveoRepository, fileList, "Updated custom entity template " + cet.getCode());
    }

    /**
     * When a {@link CustomEntityTemplate} is removed, remove the corresponding JSON Schema and commit changes in the meveo directory
     *
     * @param cet The removed {@link CustomEntityTemplate}
     * @throws BusinessException if we failed to commit the deletion
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cetRemoved(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed CustomEntityTemplate cet) throws BusinessException {
        final File cetDir = getCetDir();
        final File classDir = getClassDir();
        List<File> fileList = new ArrayList<>();

        final File schemaFile = new File(cetDir, cet.getCode() + ".json");
        if (schemaFile.exists()) {
            schemaFile.delete();
            fileList.add(schemaFile);
        }

        final File javaFile = new File(cetDir, cet.getCode() + ".java");
        if (javaFile.exists()) {
            javaFile.delete();
            fileList.add(javaFile);
        }

        final File classFile = new File(classDir, "org/meveo/model/customEntities/" + cet.getCode() + ".class");
        if (classFile.exists()) {
            classFile.delete();
        }

        gitClient.commitFiles(meveoRepository, fileList, "Deleted custom entity template " + cet.getCode());
    }

    /* ------------ CRT Notifications ------------ */

    /**
     * When a {@link CustomRelationshipTemplate} is created, create the corresponding JSON Schema and commit it in the meveo directory
     *
     * @param crt The created {@link CustomRelationshipTemplate}
     * @throws IOException       if we cannot create / write to the JSON Schema file
     * @throws BusinessException if the json schema file already exists
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void crtCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created CustomRelationshipTemplate crt) throws IOException, BusinessException {
        hasChange.set(true);

        List<File> commitFiles = new ArrayList<>();

        final String templateSchema = getTemplateSchema(crt);

        final File crtDir = getCrtDir();

        if (!crtDir.exists()) {
            crtDir.mkdirs();
            commitFiles.add(crtDir);
        }

        File schemaFile = new File(crtDir, crt.getCode() + ".json");
        if (schemaFile.exists()) {
            throw new BusinessException("Schema for CRT " + crt.getCode() + " already exists");
        }

        FileUtils.write(schemaFile, templateSchema);
        commitFiles.add(schemaFile);

        gitClient.commitFiles(meveoRepository, commitFiles, "Created custom relationship template " + crt.getCode());
    }

    /**
     * When a {@link CustomRelationshipTemplate} is updated, update the corresponding JSON Schema and commit it in the meveo directory
     *
     * @param crt The updated {@link CustomRelationshipTemplate}
     * @throws IOException if we cannot write to the JSON Schema file
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void crtUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated CustomRelationshipTemplate crt) throws IOException, BusinessException {
        hasChange.set(true);

        final String templateSchema = getTemplateSchema(crt);

        final File crtDir = getCrtDir();

        // This is for retro-compatibility, in case a CRT created before 6.4.0 is updated
        if (!crtDir.exists()) {
            crtDir.mkdirs();
        }

        File schemaFile = new File(crtDir, crt.getCode() + ".json");
        if (schemaFile.exists()) {
            schemaFile.delete();
        }

        FileUtils.write(schemaFile, templateSchema);
        gitClient.commitFiles(meveoRepository, Collections.singletonList(schemaFile), "Updated custom relationship template " + crt.getCode());
    }

    /**
     * When a {@link CustomRelationshipTemplate} is removed, remove the corresponding JSON Schema and commit changes in the meveo directory
     *
     * @param crt The removed {@link CustomRelationshipTemplate}
     * @throws BusinessException if we failed to commit the deletion
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void crtRemoved(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed CustomRelationshipTemplate crt) throws BusinessException {
        final File cetDir = getCrtDir();
        final File schemaFile = new File(cetDir, crt.getCode() + ".json");
        if (schemaFile.exists()) {
            schemaFile.delete();
        }

        gitClient.commitFiles(meveoRepository, Collections.singletonList(schemaFile), "Deleted custom relationship template " + crt.getCode());
    }

    /* ------------ CFT Notifications ------------ */

    /**
     * When a {@link CustomFieldTemplate} is created, update the corresponding JSON Schema of the related CET / CFT
     * and commit it in the meveo directory
     *
     * @param cft The created {@link CustomFieldTemplate}
     * @throws IOException if we cannot write to the JSON Schema file
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cftCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created CustomFieldTemplate cft) throws IOException, BusinessException {
        hasChange.set(true);

        if (cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            CustomEntityTemplate cet = cache.getCustomEntityTemplate(CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            final File cetDir = getCetDir();

            final File classDir = getClassDir();

            // This is for retro-compatibility, in case a we add a field to a CET created before 6.4.0
            if (!cetDir.exists()) {
                cetDir.mkdirs();
            }

            List<File> fileList = new ArrayList<>();
            File schemaFile = new File(cetDir, cet.getCode() + ".json");
            File javaFile = new File(cetDir, cet.getCode() + ".java");

            if (schemaFile.exists()) {
                schemaFile.delete();
                fileList.add(schemaFile);
                final String templateSchema = getTemplateSchema(cet);
                FileUtils.write(schemaFile, templateSchema);

                if (javaFile.exists()) {
                    javaFile.delete();
                    fileList.add(javaFile);
                    CompilationUnit compilationUnit = jsonSchemaIntoJavaClassParser.parseJsonContentIntoJavaFile(templateSchema);
                    FileUtils.write(javaFile, compilationUnit.toString());

                    File classFile = new File(classDir, "org/meveo/model/customEntities/" + cet.getCode() + ".java");
                    FileUtils.write(classFile, compilationUnit.toString());
                    compileClassJava(classDir, compilationUnit.toString(), classFile);
                }

                gitClient.commitFiles(
                        meveoRepository,
                        fileList,
                        "Add property " + cft.getCode() + " to CET " + cet.getCode()
                );
            }

        } else if (cft.getAppliesTo().startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
            CustomRelationshipTemplate crt = cache.getCustomRelationshipTemplate(cft.getAppliesTo().replaceAll("CRT_(.*)", "$1"));
            final File cetDir = getCrtDir();

            // This is for retro-compatibility, in case a we add a field to a CET created before 6.4.0
            if (!cetDir.exists()) {
                cetDir.mkdirs();
            }

            File schemaFile = new File(cetDir, crt.getCode() + ".json");

            if (schemaFile.exists()) {
                schemaFile.delete();
                final String templateSchema = getTemplateSchema(crt);

                FileUtils.write(schemaFile, templateSchema);
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
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cftUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated CustomFieldTemplate cft) throws IOException, BusinessException {
        hasChange.set(true);

        if (cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            CustomEntityTemplate cet = cache.getCustomEntityTemplate(CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            final File cetDir = getCetDir();

            final File classDir = getClassDir();

            // This is for retro-compatibility, in case we update a field of a CET created before 6.4.0
            if (!cetDir.exists()) {
                cetDir.mkdirs();
            }

            List<File> listFile = new ArrayList<>();
            File schemaFile = new File(cetDir, cet.getCode() + ".json");
            File javaFile = new File(cetDir, cet.getCode() + ".java");

            if (schemaFile.exists()) {
                schemaFile.delete();
                listFile.add(schemaFile);
                final String templateSchema = getTemplateSchema(cet);
                FileUtils.write(schemaFile, templateSchema);

                if (javaFile.exists()) {
                    javaFile.delete();
                    CompilationUnit compilationUnit = jsonSchemaIntoJavaClassParser.parseJsonContentIntoJavaFile(templateSchema);
                    FileUtils.write(javaFile, compilationUnit.toString());
                    listFile.add(javaFile);

                    File classFile = new File(classDir, "org/meveo/model/customEntities/" + cet.getCode() + ".java");
                    FileUtils.write(classFile, compilationUnit.toString());
                    compileClassJava(classDir, compilationUnit.toString(), classFile);

                }

                gitClient.commitFiles(
                        meveoRepository,
                        listFile,
                        "Update property " + cft.getCode() + " of CET " + cet.getCode()
                );

            }

        } else if (cft.getAppliesTo().startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
            CustomRelationshipTemplate crt = cache.getCustomRelationshipTemplate(cft.getAppliesTo().replaceAll("CRT_(.*)", "$1"));
            final File cetDir = getCrtDir();

            // This is for retro-compatibility, in case we update a field of a CET created before 6.4.0
            if (!cetDir.exists()) {
                cetDir.mkdirs();
            }

            File schemaFile = new File(cetDir, crt.getCode() + ".json");

            if (schemaFile.exists()) {
                schemaFile.delete();
                final String templateSchema = getTemplateSchema(crt);

                FileUtils.write(schemaFile, templateSchema);
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
     */
    @TransactionAttribute(TransactionAttributeType.REQUIRES_NEW)
    public void cftRemoved(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed CustomFieldTemplate cft) throws BusinessException {
        if (cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            CustomEntityTemplate cet = cache.getCustomEntityTemplate(CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            if (cet == null) {
                // CET already deleted
                return;
            }

            final File cetDir = getCetDir();

            final File classDir = getClassDir();

            if (!cetDir.exists()) {
                // Nothing to delete
                return;
            }

            File schemaFile = new File(cetDir, cet.getCode() + ".json");
            File javaFile = new File(cetDir, cet.getCode() + ".java");
            File classFile = new File(classDir, "org/meveo/model/customEntities/" + cet.getCode() + ".class");

            if (schemaFile.exists()) {
                schemaFile.delete();

                if (javaFile.exists()) {
                    javaFile.delete();
                }

                if (classFile.exists()) {
                    classFile.delete();
                }

                gitClient.commitFiles(
                        meveoRepository,
                        Collections.singletonList(schemaFile),
                        "Remove property " + cft.getCode() + " of CET " + cet.getCode()
                );

                gitClient.commitFiles(
                        meveoRepository,
                        Collections.singletonList(javaFile),
                        "Remove property " + cft.getCode() + " of CET " + cet.getCode()
                );
            }

        } else if (cft.getAppliesTo().startsWith(CustomRelationshipTemplate.CRT_PREFIX)) {
            CustomRelationshipTemplate crt = cache.getCustomRelationshipTemplate(cft.getAppliesTo().replaceAll("CRT_(.*)", "$1"));
            if (crt == null) {
                // CET already deleted
                return;
            }

            final File cetDir = getCrtDir();

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
     */
    @SuppressWarnings("unchecked")
    public void onCETsChanged(@Observes @CommitReceived CommitEvent commitEvent) throws BusinessException, MeveoApiException {
        if (commitEvent.getGitRepository().getCode().equals(meveoRepository.getCode())) {
            for (String modifiedFile : commitEvent.getModifiedFiles()) {
                String[] cet = modifiedFile.split("/");
                String fileName = cet[cet.length - 1];
                String templateType = cet[cet.length - 2];
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

    @SuppressWarnings("unchecked")
    public void onCRTsChanged(@Observes @CommitReceived CommitEvent commitEvent) throws BusinessException, MeveoApiException {
        if (commitEvent.getGitRepository().getCode().equals(meveoRepository.getCode())) {
            for (String modifiedFile : commitEvent.getModifiedFiles()) {
                String[] crt = modifiedFile.split("/");
                String fileName = crt[crt.length - 1];
                String templateType = crt[crt.length - 2];
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
    
	public String findCetJsonSchema(CustomEntityTemplate cet) throws IOException {

		final File cetDir = getCetDir();

		if (!cetDir.exists()) {
			return "";
		}

		File schemaFile = new File(cetDir, cet.getCode() + ".json");

		return FileUtils.readFileToString(schemaFile, StandardCharsets.UTF_8);
	}

    public void compileClassJava(File classDir, String compilationUnit, File classFile) {

        try {
            List<File> fileList = supplementClassPathWithMissingImports(compilationUnit, getCetDir().getAbsolutePath());
            String classPackage = classDir.getAbsolutePath();
            if (!StringUtils.isBlank(classPackage) && !CustomScriptService.CLASSPATH_REFERENCE.get().contains(classPackage)) {
                CustomScriptService.CLASSPATH_REFERENCE.set(CustomScriptService.CLASSPATH_REFERENCE.get() + File.pathSeparator + classPackage);
            }
            String classPath = CustomScriptService.CLASSPATH_REFERENCE.get();

            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
            if (!classDir.exists()) {
                classDir.mkdirs();
            }

            List<File> files = new ArrayList<>();
            for (File file : fileList) {
                String content = FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                File importFile = new File(classDir, "org/meveo/model/customEntities/" + file.getName());
                FileUtils.write(importFile, content);
                files.add(importFile);
            }
            files.add(classFile);

            Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(files);
            Boolean isOK = compiler.getTask(null, fileManager, null, Arrays.asList("-cp", classPath), null, compilationUnits).call();
            if (isOK) {
                for (File file : files) {
                    if (file != null) {
                        file.delete();
                    }
                }
            }

        } catch (IOException e) {
            LOGGER.error("Error compiling java class", e);
        }
    }

    private List<File> supplementClassPathWithMissingImports(String javaSrc, String pathJava) {

        List<File> files = new ArrayList<>();

        String regex = "import (.*?);";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(javaSrc);
        while (matcher.find()) {
            String className = matcher.group(1);
            if (className.startsWith("org.meveo.model.customEntities")) {
                String fileName = className.split("\\.")[4];
                File file = new File(pathJava, fileName + ".java");
                files.add(file);
                continue;
            }
            try {
                Class<?> clazz;
                
                try {
                    URLClassLoader classLoader = (URLClassLoader) ClassLoader.getSystemClassLoader();
                    clazz = classLoader.loadClass(className);
                } catch (ClassNotFoundException e) {
                    clazz = Class.forName(className);
                }

                CodeSource codeSource = clazz.getProtectionDomain().getCodeSource();
                if(codeSource != null) {
					String location = codeSource.getLocation().getFile();
                    
					if (location.startsWith("file:")) {
                        location = location.substring(5);
                    }
					
                    if (location.endsWith("!/")) {
                        location = location.substring(0, location.length() - 2);
                    }

                    if (!CustomScriptService.CLASSPATH_REFERENCE.get().contains(location)) {
                        synchronized (CustomScriptService.CLASSPATH_REFERENCE) {
                            if (!CustomScriptService.CLASSPATH_REFERENCE.get().contains(location)) {
                                CustomScriptService.CLASSPATH_REFERENCE.set(CustomScriptService.CLASSPATH_REFERENCE.get() + File.pathSeparator + location);
                            }
                        }
                    }
                }

            } catch (Exception e) {
            	Log.error("Error supplementing class path", e);
            }
        }
        
        return files;
    }

    private File getCetDir() {
        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode()  + "/src/main/java/");
        return new File(repositoryDir, "custom/entities");
    }

    private File getCrtDir() {
        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode());
        return new File(repositoryDir, "custom/relationships");
    }

    private String getTemplateSchema(CustomEntityTemplate cet) {
        String schema = jsonSchemaGenerator.generateSchema(cet.getCode(), cet);
        return schema.replaceAll("\"\\$ref\".*:.*\"#/definitions/([^\"]+)\"", "\"\\$ref\": \"./$1\"");

    }

    private String getTemplateSchema(CustomRelationshipTemplate crt) {
        String schema = jsonSchemaGenerator.generateSchema(crt.getCode(), crt);
        return schema.replaceAll("#/definitions", "../entities");
    }

    private File getClassDir() {
        final File classDir = CustomEntityTemplateService.getClassesDir(currentUser);
        return classDir;
    }
}
