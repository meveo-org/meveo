/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr) and contributors.
 * (C) Copyright 2015-2018 Opencell SAS (http://opencellsoft.com/) and contributors.
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
package org.meveo.service.script;

import static org.meveo.model.scripts.ScriptSourceTypeEnum.JAVA;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.enterprise.event.Observes;
import javax.inject.Inject;
import javax.persistence.FlushModeType;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementNotFoundException;
import org.meveo.admin.exception.InvalidScriptException;
import org.meveo.admin.util.ResourceBundle;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.event.qualifier.Removed;
import org.meveo.event.qualifier.git.CommitEvent;
import org.meveo.event.qualifier.git.CommitReceived;
import org.meveo.model.git.GitRepository;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.scripts.Accessor;
import org.meveo.model.scripts.CustomScript;
import org.meveo.model.scripts.FunctionIO;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.scripts.ScriptSourceTypeEnum;
import org.meveo.model.scripts.test.ExpectedOutput;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.git.GitClient;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.MeveoRepository;
import org.meveo.service.script.cache.ScriptInstancesCache;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.type.ClassOrInterfaceType;

/**
 * @param <T> Type of script
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.11
 */
public abstract class CustomScriptService<T extends CustomScript> extends FunctionService<T, ScriptInterface> {

    @Inject
    private ResourceBundle resourceMessages;
    
    @Inject
    protected ScriptInstancesCache scriptInstancesCache;

    @Inject
    @CurrentUser
    protected MeveoUser currentUser;

    @Inject
    @MeveoRepository
    private GitRepository meveoRepository;

    @Inject
    private GitClient gitClient;
    
    /**
     * Constructor.
     */
    public CustomScriptService() {

        super();
    }

    /**
     * Find scripts by source type.
     *
     * @param type script source type
     * @return list of scripts
     */
    @SuppressWarnings("unchecked")
    public List<T> findByType(ScriptSourceTypeEnum type) {
        List<T> result = new ArrayList<>();
        try {
            result = (List<T>) getEntityManager().createNamedQuery("CustomScript.getScriptInstanceByTypeActive").setParameter("sourceTypeEnum", type).getResultList();
        } catch (NoResultException ignored) {

        }
        return result;
    }
    
    /**
     * Remove compiled script, its logs and cached instances for given script code
     *
     * @param scriptCode Script code
     */
    public void clearCompiledScripts(String scriptCode) {
        super.clear(scriptCode);
        scriptInstancesCache.clearCompiledScripts(scriptCode);
    }

    /**
     * If script file has been changed, commit the differences. <br>
     * Re-compile the script
     *
     * @param script Created or updated {@link CustomScript}
     */
    @Override
    protected void afterUpdateOrCreate(T script) {
    	
        try {
        	boolean commitFile = true;
            File scriptFile = scriptInstancesCache.findScriptFile(script);
            if (scriptFile.exists()) {
                String previousScript = MeveoFileUtils.readString(scriptFile.getAbsolutePath());
                if (previousScript.equals(script.getScript())) {
                    // Don't commit if there are no difference
                	commitFile = false;
                }
            }

            if(commitFile) {
	            buildScriptFile(scriptFile, script);
	            gitClient.commitFiles(meveoRepository, Collections.singletonList(scriptFile), "Create or update script " + script.getCode());
            }
            
        } catch (Exception e) {
            log.error("Error committing script", e);
        }

        scriptInstancesCache.compileScript(script, false);
    }

    @Override
    protected void validate(T script) throws BusinessException {
        if (script.getSourceTypeEnum() == ScriptSourceTypeEnum.ES5) {
            return;
        }
        String className = getClassName(script.getScript());
        if (className == null) {
            throw new BusinessException(resourceMessages.getString("message.scriptInstance.sourceInvalid"));
        }
        String fullClassName = getFullClassname(script.getScript());
        if (isOverwritesJavaClass(fullClassName)) {
            throw new BusinessException(resourceMessages.getString("message.scriptInstance.classInvalid", fullClassName));
        }
    }

    @Override
    protected String getCode(T script) {
        if (script.getSourceTypeEnum() == JAVA) {
            return getFullClassname(script.getScript());
        } else {
            return script.getCode();
        }
    }

    @Override
    public ScriptInterface getExecutionEngine(String scriptCode, Map<String, Object> context) {
        T script = this.findByCode(scriptCode);
        detach(script);
        return getExecutionEngine(script, context);
    }

    @Override
    public ScriptInterface getExecutionEngine(T script, Map<String, Object> context) {
        try {
            ScriptInterface scriptInstance = this.getScriptInstance(script.getCode());

            // Call setters if those are provided
            if (script.getSourceTypeEnum() == ScriptSourceTypeEnum.JAVA && context != null) {
                for (Accessor setter : script.getSettersNullSafe()) {
                    Object setterValue = context.get(setter.getName());
                    if (setterValue != null) {
                        // In case the parameters are initialized by a get request, we might need to
                        // convert the input to their right types
                        ScriptUtils.ClassAndValue classAndValue = new ScriptUtils.ClassAndValue();
                        if (!setter.getType().equals("String") && setterValue instanceof String) {
                            classAndValue = ScriptUtils.findTypeAndConvert(setter.getType(), (String) setterValue);
                        } else {
                            classAndValue.setValue(setterValue);
                            classAndValue.setClass(setterValue.getClass());
                        }

                        Method method = ReflectionUtils.getSetterByNameAndSimpleClassName(scriptInstance.getClass(), setter.getMethodName(), setter.getType())
                        		.orElse(null);
                        
                        if(method.getParameters()[0].getType() != classAndValue.getTypeClass()) {
                        	// If value is a map, convert the map into target class
                        	if(classAndValue.getValue() instanceof Map) {
                        		Object convertedValue = JacksonUtil.convert(classAndValue.getValue(), method.getParameters()[0].getType());
                            	method.invoke(scriptInstance, convertedValue);
                        	}
                        	
                        } else {
                        	method.invoke(scriptInstance, classAndValue.getValue());
                        }
                        
                       // , classAndValue.getTypeClass()).invoke(scriptInstance, classAndValue.getValue());
                    }
                }
            }
            return scriptInstance;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected Map<String, Object> buildResultMap(ScriptInterface engine, Map<String, Object> context) {
        CustomScript script = this.findByCode(engine.getClass().getName());

        if (script == null) {
            // The script is probably not a Java script and we cannot retrieve its code
            // using its class name
            return context;
        }

        // Put getters' values to context
        if (script.getSourceTypeEnum() == ScriptSourceTypeEnum.JAVA) {
            for (Accessor getter : script.getGettersNullSafe()) {
                try {
                    Object getterValue = engine.getClass().getMethod(getter.getMethodName()).invoke(engine);
                    context.put(getter.getName(), getterValue);
                } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return super.buildResultMap(engine, context);
    }

    /**
     * Check full class name is existed class path or not.
     *
     * @param fullClassName full class name
     * @return true i class is overridden
     */
    public static boolean isOverwritesJavaClass(String fullClassName) {
        try {
            Class.forName(fullClassName);
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }
    

    private void checkEndpoints(CustomScript scriptInstance, List<Accessor> setters) throws BusinessException {

        // Check if endpoints parameters are not bound to deleted inputs
        final List<String> newInputs = setters.stream().map(Accessor::getName).collect(Collectors.toList());

        List<String> deletedProperties = scriptInstance.getInputs().stream().map(FunctionIO::getName).collect(Collectors.toList());

        deletedProperties.removeAll(newInputs);
    }

    /**
     * Parse the java source code and extract getters and setters
     *
     * @param script Script to parse
     */
    @Override
    protected void beforeUpdateOrCreate(T script) throws BusinessException {
        if (script.getSourceTypeEnum() != ScriptSourceTypeEnum.JAVA) {
            return;
        }

        CompilationUnit compilationUnit;

        try {
            compilationUnit = JavaParser.parse(script.getScript());
        } catch (Exception e) {
            // Skip getter and setters parsing. We don't need to log errors as they will be
            // logged later in code.
            return;
        }

        final ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit.getChildNodes().stream().filter(e -> e instanceof ClassOrInterfaceDeclaration)
                .map(e -> (ClassOrInterfaceDeclaration) e)
                .findFirst()
                .get();

        final List<MethodDeclaration> methods = classOrInterfaceDeclaration.getMembers()
        		.stream().filter(e -> e instanceof MethodDeclaration)
        		.map(e -> (MethodDeclaration) e)
                .collect(Collectors.toList());

        final List<Accessor> setters = ScriptUtils.getSetters(methods);

        final List<Accessor> getters = ScriptUtils.getGetters(methods);
        
        // Find getter and setter defined in super types
        for(ClassOrInterfaceType type : classOrInterfaceDeclaration.getExtendedTypes()) {
        	Class<?> typeClass = null;
        	
        	try {
        		typeClass = Class.forName(type.toString());
        	} catch (Exception e) {
            	String className = compilationUnit.getImports().stream()
                		.filter(importEntry -> importEntry.getNameAsString().endsWith("." + type.getNameAsString()))
                		.map(ImportDeclaration::getNameAsString)
                		.findFirst()
                		.orElseThrow(() -> new RuntimeException("No declaration found for extended type " + type.getNameAsString()));
            	
            	try {
    				typeClass = Class.forName(className);
    			} catch (ClassNotFoundException e1) {
    				try {
    					typeClass = getScriptInterfaceWCompile(className).getScriptInterface().getClass();
    				} catch (Exception e2) {
    					throw new BusinessException(e2);
    				}
				}
        	}
				
			// Build getters and setter of extended types
			Arrays.stream(typeClass.getMethods())
					.filter(m -> m.getName().startsWith(Accessor.SET))
					.filter(m -> m.getParameterCount() == 1)
					.filter(m -> m.getReturnType() == void.class)
					.map(Accessor::new)
					.forEach(setters::add);
			
			Arrays.stream(typeClass.getMethods())
					.filter(m -> m.getName().startsWith(Accessor.GET) && !m.getName().equals("getClass"))
					.filter(m -> m.getParameterCount() == 0)
					.filter(m -> m.getReturnType() != void.class)
					.map(Accessor::new)
					.forEach(getters::add);
        }

        checkEndpoints(script, setters);

        script.setGetters(getters);
        script.setSetters(setters);
    }

    

    
    
    
    
    /**
     * Find the script class for a given script code
     *
     * @param scriptCode Script code
     * @return Script interface Class
     * @throws Exception if error occurs
     */
    public synchronized ScriptInterface getScriptInterface(String scriptCode) throws Exception {
    	log.debug("Retrieving script interface for {}", scriptCode);
        ScriptInterfaceSupplier supplier = scriptInstancesCache.getScriptInterfaceSupplier(scriptCode);//ALL_SCRIPT_INTERFACES.get(new CacheKeyStr(currentUser.get().getProviderCode(), scriptCode));
        if(supplier == null) {
        	return getScriptInterfaceWCompile(scriptCode).getScriptInterface();
        }
        return supplier.getScriptInterface();
    }

    /**
     * Compile the script class for a given script code if it is not compile yet.
     * NOTE: method is executed synchronously due to WRITE lock. DO NOT CHANGE IT,
     * so there would be only one attempt to compile a new script class
     *
     * @param scriptCode Script code
     * @return Script interface Class
     * @throws InvalidScriptException   Were not able to instantiate or compile a
     *                                  script
     * @throws ElementNotFoundException Script not found
     */
    protected ScriptInterfaceSupplier getScriptInterfaceWCompile(String scriptCode) throws ElementNotFoundException, InvalidScriptException {
        ScriptInterfaceSupplier result = scriptInstancesCache.getScriptInterfaceSupplier(scriptCode);//ALL_SCRIPT_INTERFACES.get(new CacheKeyStr(currentUser.getProviderCode(), scriptCode));
        if (result == null) {
        	List<String> fetchFields = Arrays.asList("mavenDependencies");
            T script = findByCode(scriptCode, fetchFields);
            if (script == null) {
                log.debug("ScriptInstance with {} does not exist", scriptCode);
                throw new ElementNotFoundException(scriptCode, getEntityClass().getName());
            }
            
            scriptInstancesCache.compileScript(script, false);
            if (script.isError()) {
                log.debug("ScriptInstance {} failed to compile. Errors: {}", scriptCode, script.getScriptErrors());
                throw new InvalidScriptException(scriptCode, getEntityClass().getName());
            }
            
            result = scriptInstancesCache.getScriptInterfaceSupplier(scriptCode);//ALL_SCRIPT_INTERFACES.get(new CacheKeyStr(currentUser.getProviderCode(), scriptCode));
            detach(script);
        }

        if (result == null) {
            log.debug("ScriptInstance with {} does not exist", scriptCode);
            throw new ElementNotFoundException(scriptCode, getEntityClass().getName());
        }

        return result;
    }

    /**
     * Get a compiled script class
     *
     * @param scriptCode Script code
     * @return A compiled script class
     * @throws InvalidScriptException Were not able to instantiate or compile a
     *                                script
     */
    public ScriptInterface getScriptInstance(String scriptCode) throws InvalidScriptException {
        try {
            return getScriptInterface(scriptCode);
        } catch (Exception e) {
            log.error("Failed to instantiate script {}", scriptCode, e);
            throw new InvalidScriptException(scriptCode, getEntityClass().getName());
        }
    }

    /**
     * Find the package name in a source java text.
     *
     * @param src Java source code
     * @return Package name
     */
    public static String getPackageName(String src) {
        return StringUtils.patternMacher("package (.*?);", src);
    }

    /**
     * Find the class name in a source java text
     *
     * @param src Java source code
     * @return Class name
     */
    public static String getClassName(String src) {
        String className = StringUtils.patternMacher("public class (.*) extends", src);
        if (className == null) {
            className = StringUtils.patternMacher("public class (.*) implements", src);
        }
        return className != null ? className.trim() : null;
    }

    /**
     * Gets a full classname of a script by combining a package (if applicable) and
     * a classname
     *
     * @param script Java source code
     * @return Full classname
     */
    public static String getFullClassname(String script) {
        String packageName = getPackageName(script);
        String className = getClassName(script);
        return (packageName != null ? packageName.trim() + "." : "") + className;
    }

    @Override
    public List<ExpectedOutput> compareResults(List<ExpectedOutput> expectedOutputs, Map<String, Object> results) {
        return null;
    }

    /**
     * When a script is deleted, remove the file from git repository
     *
     * @param scriptInstance Removed {@link ScriptInstance}
     * @throws BusinessException if error occurs during changes commit
     */
    public void onScriptRemoved(@Observes @Removed ScriptInstance scriptInstance) throws BusinessException {
        File file = scriptInstancesCache.findScriptFile(scriptInstance);
        if (file.exists()) {
            file.delete();
            gitClient.commitFiles(meveoRepository, Collections.singletonList(file), "Remove script " + scriptInstance.getCode());
        }
    }

    /**
     * When a commit concerning script is received :
     * <ul>
     * <li>If script file has been created, create the JPA entity</li>
     * <li>If script file has been modified, re-compile it</li>
     * <li>If script file has been deleted, remove the JPA entity</li>
     * </ul>
     * @param commitEvent Emmited commit event
     * @throws BusinessException if we can't create / update / delete the scripts corresponding to the commited files
     * @throws IOException if a commited file can't be read
     */
    @SuppressWarnings("unchecked")
    public void onScriptUploaded(@Observes @CommitReceived CommitEvent commitEvent) throws  IOException, BusinessException {
        if (commitEvent.getGitRepository().getCode().equals(meveoRepository.getCode())) {
            for (String modifiedFile : commitEvent.getModifiedFiles()) {
                if (modifiedFile.startsWith("src/main/java/scripts")) {
                    String scriptCode = modifiedFile.replaceAll("src/main/java/scripts/(.*)\\..*$", "$1").replaceAll("/", ".");
                    T script = findByCode(scriptCode);
                    File repositoryDir = GitHelper.getRepositoryDir(currentUser, commitEvent.getGitRepository().getCode());
                    File scriptFile = new File(repositoryDir, modifiedFile);

                    if (script == null && scriptFile.exists()) {
                        // Script has been created
                        CustomScript scriptInstance = new ScriptInstance();
                        scriptInstance.setCode(scriptCode);
                        String absolutePath = scriptFile.getAbsolutePath();
                        ScriptSourceTypeEnum scriptType = absolutePath.endsWith(".js") ? ScriptSourceTypeEnum.ES5 : JAVA;
                        scriptInstance.setSourceTypeEnum(scriptType);
                        scriptInstance.setScript(MeveoFileUtils.readString(absolutePath));
                        create((T) scriptInstance);

                    } else if (script != null && !scriptFile.exists()) {
                        // Script has been removed
                        remove(script);

                    } else if (script != null && scriptFile.exists()) {
                        // Scipt has been updated
                    	script.setScript(scriptInstancesCache.readScriptFile(script));
                        update(script);
                        scriptInstancesCache.compileScript(script, false);
                    }
                }
            }
        }
    }

    private void buildScriptFile(File scriptFile, CustomScript scriptInstance) throws IOException {
        if (!scriptFile.getParentFile().exists()) {
            scriptFile.getParentFile().mkdirs();
        }

        FileUtils.write(scriptFile, scriptInstance.getScript(), StandardCharsets.UTF_8);
    }

    /**
     * @param javaSource java source
     * @return list of import scripts
     */
    public List<String> getImportScripts(String javaSource) {
        String regexImport = "import (.*?);";
        Pattern patternImport = Pattern.compile(regexImport);
        Matcher matcherImport = patternImport.matcher(javaSource);
        List<String> results = new ArrayList<>();
        while (matcherImport.find()) {
            String nameImport = matcherImport.group(1);
            results.add(nameImport);
        }
        return results;
    }

    /**
     * @param script The script to analyze
     * @return the list of {@link ScriptInstance} that the script is dependent of
     */
    public List<ScriptInstance> populateImportScriptInstance(String script) {
        List<String> importedScripts = getImportScripts(script);
        List<ScriptInstance> scriptInstances = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(importedScripts)) {
            for (String scriptCode : importedScripts) {
            	String query = "FROM ScriptInstance si WHERE si.code = :scriptCode";
            	Query q = getEntityManager().createQuery(query)
        			.setParameter("scriptCode", scriptCode)
            		.setFlushMode(FlushModeType.COMMIT);
            	
            	try {
	                ScriptInstance scriptInstance = (ScriptInstance) q.getSingleResult();
	                scriptInstances.add(scriptInstance);
            	} catch(NoResultException e) {}
            }
        }
        return scriptInstances;
    }
   
}
