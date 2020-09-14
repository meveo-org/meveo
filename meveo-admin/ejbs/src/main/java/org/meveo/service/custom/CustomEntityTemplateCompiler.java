/**
 * 
 */
package org.meveo.service.custom;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.cache.CustomFieldsCacheContainerProvider;
import org.meveo.exceptions.EntityDoesNotExistsException;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.git.GitRepository;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.crm.impl.JSONSchemaIntoJavaClassParser;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.MeveoRepository;
import org.slf4j.Logger;

import com.github.javaparser.ast.CompilationUnit;

/**
 * Service for generating and compiling CET source file
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
public class CustomEntityTemplateCompiler {
	
    @Inject
    @MeveoRepository
    private GitRepository meveoRepository;
	
    @Inject
    private JSONSchemaIntoJavaClassParser jsonSchemaIntoJavaClassParser;
    
    @Inject
    private CustomFieldsCacheContainerProvider cache;
    
    @Inject
    @CurrentUser
    private MeveoUser currentUser;
    
    @Inject
    private Logger log;
    
    /**
     * Retrieve the java source file for a given CET. <br>
     * <br><b>Note</b>: if the source file does not exists, it will be generated
     * 
     * @param cetCode Code of the cet
     * @return the java source file
     * @throws BusinessException if a read / write operation fails
     */
    public File getCETSourceFile(String cetCode) throws BusinessException {
		final File cetDir = getCetDir();
        File javaFile = new File(cetDir, cetCode + ".java");
        if(!javaFile.exists()) {
        	var cet = cache.getCustomEntityTemplate(cetCode);
        	if (cet == null)
        		throw new EntityDoesNotExistsException("CET does not exists : " + cetCode);
            File schemaFile = new File(cetDir, cet.getCode() + ".json");
            try {
				javaFile = generateCETSourceFile(Files.readString(schemaFile.toPath()), cet);
			} catch (IOException e) {
				throw new BusinessException("Can't read schema file", e);
			}
        }
    	return javaFile;
    }

    /**
     * Create the java source file for a given CET
     * 
     * @param templateSchema the json schema of the CET
     * @param cet The custom entity template
     * @return the java source file
     * @throws BusinessException if the file can't be written
     */
	public File generateCETSourceFile(String templateSchema, CustomEntityTemplate cet) throws BusinessException {
		log.info("Generating source file for {}", cet);
		
		final File cetDir = getCetDir();
        final CompilationUnit compilationUnit = jsonSchemaIntoJavaClassParser.parseJsonContentIntoJavaFile(templateSchema, cet);
        File javaFile = new File(cetDir, cet.getCode() + ".java");
        if (javaFile.exists()) {
            javaFile.delete();
        }
        
        try {
			FileUtils.write(javaFile, compilationUnit.toString(), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new BusinessException("Can't write to file", e);
		}
        
        return javaFile;
	}
	
	/**
	 * @return the directory where custom entity templates source files are stored
	 */
    public File getCetDir() {
        final File repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode()  + "/src/main/java/");
        return new File(repositoryDir, "custom/entities");
    }
}
