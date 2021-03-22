/**
 * 
 */
package org.meveo.service.custom;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.git.GitRepository;
import org.meveo.model.module.MeveoModule;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.base.BusinessService;
import org.meveo.service.base.BusinessServiceFinder;
import org.meveo.service.crm.impl.JSONSchemaGenerator;
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
    private BusinessServiceFinder businessServiceFinder;
    
    @Inject
    private JSONSchemaIntoJavaClassParser jsonSchemaIntoJavaClassParser;
    
    @Inject
    private JSONSchemaGenerator jsonSchemaGenerator;
    
    @Inject
    @CurrentUser
    private MeveoUser currentUser;
    
    @Inject
    private Logger log;
    
    public String getTemplateSchema(CustomEntityTemplate cet) {
        String schema = jsonSchemaGenerator.generateSchema(cet.getCode(), cet);
        return schema.replaceAll("#/definitions", ".");

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
		
		final File cetDir = getCetDir(cet);
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
    @SuppressWarnings({ "rawtypes", "unchecked" })
	public File getCetDir(CustomEntityTemplate cet) {
    	String path;
    	File repositoryDir;
    	BusinessService businessService = businessServiceFinder.find(cet);
    	MeveoModule module = businessService.findModuleOf(cet);
    	if (module == null) {
	        repositoryDir = GitHelper.getRepositoryDir(currentUser, meveoRepository.getCode()  + "/src/main/java/");
	        path = "org/meveo/model/customEntities";
    	} else {
    		repositoryDir = GitHelper.getRepositoryDir(currentUser, module.getGitRepository().getCode());
    		path = "/customEntityTemplates/" + cet.getCode();
    	}
        return new File(repositoryDir, path);
   	}
}
