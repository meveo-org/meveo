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

package org.meveo.observers;

import org.apache.commons.io.FileUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.event.qualifier.Created;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.Updated;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.git.GitRepository;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.crm.impl.JSONSchemaGenerator;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.MeveoRepository;

import javax.annotation.PostConstruct;
import javax.ejb.*;
import javax.enterprise.event.Observes;
import javax.enterprise.event.TransactionPhase;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Observer that updates IDL definitions when a CET, CRT or CFT changes
 *
 * @author Clément Bareth
 */
@Singleton
@Startup
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
public class OntologyObserver {

//    @Inject
//    private GraphQLService graphQLService;
//
//    @Inject
//    private Logger log;

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

    private AtomicBoolean hasChange = new AtomicBoolean(true);

    /**
     * At startup, update the IDL definitions
     */
    @PostConstruct
    public void init() {
        updateIDL();
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
    public void cetCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created CustomEntityTemplate cet) throws IOException, BusinessException {
        hasChange.set(true);

        List<File> commitFiles = new ArrayList<>();

        final String templateSchema = getTemplateSchema(cet);

        final File cetDir = getCetDir();

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

        gitClient.commitFiles(meveoRepository, commitFiles, "Created custom entity template " + cet.getCode());
    }

    /**
     * When a {@link CustomEntityTemplate} is updated, update the corresponding JSON Schema and commit it in the meveo directory
     *
     * @param cet The updated {@link CustomEntityTemplate}
     * @throws IOException if we cannot write to the JSON Schema file
     */
    public void cetUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated CustomEntityTemplate cet) throws IOException, BusinessException {
        hasChange.set(true);

        final String templateSchema = getTemplateSchema(cet);

        final File cetDir = getCetDir();

        // This is for retro-compatibility, in case a CET created before 6.4.0 is updated
        if (!cetDir.exists()) {
            cetDir.mkdirs();
        }

        File schemaFile = new File(cetDir, cet.getCode() + ".json");
        if (schemaFile.exists()) {
            schemaFile.delete();
        }

        FileUtils.write(schemaFile, templateSchema);
        gitClient.commitFiles(meveoRepository, Collections.singletonList(schemaFile), "Updated custom entity template " + cet.getCode());
    }

    /**
     * When a {@link CustomEntityTemplate} is removed, remove the corresponding JSON Schema and commit changes in the meveo directory
     *
     * @param cet The removed {@link CustomEntityTemplate}
     * @throws BusinessException if we failed to commit the deletion
     */
    public void cetRemoved(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed CustomEntityTemplate cet) throws BusinessException {
        final File cetDir = getCetDir();
        final File schemaFile = new File(cetDir, cet.getCode() + ".json");
        if (schemaFile.exists()) {
            schemaFile.delete();
        }

        gitClient.commitFiles(meveoRepository, Collections.singletonList(schemaFile), "Deleted custom entity template " + cet.getCode());
    }

    /* ------------ CRT Notifications ------------ */

    /**
     * When a {@link CustomRelationshipTemplate} is created, create the corresponding JSON Schema and commit it in the meveo directory
     *
     * @param crt The created {@link CustomRelationshipTemplate}
     * @throws IOException       if we cannot create / write to the JSON Schema file
     * @throws BusinessException if the json schema file already exists
     */
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
    public void cftCreated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Created CustomFieldTemplate cft) throws IOException, BusinessException {
        hasChange.set(true);

        if (cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            CustomEntityTemplate cet = cache.getCustomEntityTemplate(CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            final File cetDir = getCetDir();

            // This is for retro-compatibility, in case a we add a field to a CET created before 6.4.0
            if (!cetDir.exists()) {
                cetDir.mkdirs();
            }

            File schemaFile = new File(cetDir, cet.getCode() + ".json");

            if (schemaFile.exists()) {
                schemaFile.delete();
                final String templateSchema = getTemplateSchema(cet);

                FileUtils.write(schemaFile, templateSchema);
                gitClient.commitFiles(
                        meveoRepository,
                        Collections.singletonList(schemaFile),
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
    public void cftUpdated(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Updated CustomFieldTemplate cft) throws IOException, BusinessException {
        hasChange.set(true);

        if (cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            CustomEntityTemplate cet = cache.getCustomEntityTemplate(CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            final File cetDir = getCetDir();

            // This is for retro-compatibility, in case we update a field of a CET created before 6.4.0
            if (!cetDir.exists()) {
                cetDir.mkdirs();
            }

            File schemaFile = new File(cetDir, cet.getCode() + ".json");

            if (schemaFile.exists()) {
                schemaFile.delete();
                final String templateSchema = getTemplateSchema(cet);

                FileUtils.write(schemaFile, templateSchema);
                gitClient.commitFiles(
                        meveoRepository,
                        Collections.singletonList(schemaFile),
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
    public void cftRemoved(@Observes(during = TransactionPhase.AFTER_SUCCESS) @Removed CustomFieldTemplate cft) throws BusinessException {
        if (cft.getAppliesTo().startsWith(CustomEntityTemplate.CFT_PREFIX)) {
            CustomEntityTemplate cet = cache.getCustomEntityTemplate(CustomEntityTemplate.getCodeFromAppliesTo(cft.getAppliesTo()));
            if (cet == null) {
                // CET already deleted
                return;
            }

            final File cetDir = getCetDir();

            if (!cetDir.exists()) {
                // Nothing to delete
                return;
            }

            File schemaFile = new File(cetDir, cet.getCode() + ".json");

            if (schemaFile.exists()) {
                schemaFile.delete();

                gitClient.commitFiles(
                        meveoRepository,
                        Collections.singletonList(schemaFile),
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

    private File getCetDir() {
        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository);
        return new File(repositoryDir, "custom/entities");
    }

    private File getCrtDir() {
        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository);
        return new File(repositoryDir, "custom/relationships");
    }

    private String getTemplateSchema(CustomEntityTemplate cet) {
        String schema = jsonSchemaGenerator.generateSchema(cet.getCode(), cet);
        return schema.replaceAll("\"\\$ref\".*:.*\"#/definitions/([^\"]+)\"", "\"\\$ref\": \"./$1\"");

    }

    private String getTemplateSchema(CustomRelationshipTemplate crt) {
        String schema = jsonSchemaGenerator.generateSchema(crt.getCode(), crt);
        return schema.replaceAll("\"\\$ref\".*:.*\"#/definitions/([^\"]+)\"", "\"\\$ref\": \"../entities/$1\"");
    }

}
