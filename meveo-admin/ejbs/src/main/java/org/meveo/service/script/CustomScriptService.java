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

import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.javadoc.JavadocBlockTag;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementNotFoundException;
import org.meveo.admin.exception.InvalidScriptException;
import org.meveo.admin.util.ResourceBundle;
import org.meveo.cache.CacheKeyStr;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.scripts.*;
import org.meveo.model.scripts.test.ExpectedOutput;
import org.meveo.service.technicalservice.endpoint.EndpointService;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.meveo.model.scripts.ScriptSourceTypeEnum.JAVA;

public abstract class CustomScriptService<T extends CustomScript> extends FunctionService<T, ScriptInterface> {

    private static final String SET = "set";
    private static final String GET = "get";
    private static final String IS = "is";
    @Inject
    private ResourceBundle resourceMessages;

    @Inject
    private EndpointService endpointService;

//    protected final Class<ScriptInterface> scriptInterfaceClass;

    private Map<CacheKeyStr, ScriptInterfaceSupplier> allScriptInterfaces = new HashMap<>();

    private CharSequenceCompiler<ScriptInterface> compiler;

    private String classpath = "";

    /**
     * Constructor.
     */
//    @SuppressWarnings({ "unchecked", "rawtypes" })
    public CustomScriptService() {
        super();
//        Class clazz = getClass();
//        while (!(clazz.getGenericSuperclass() instanceof ParameterizedType)) {
//            clazz = clazz.getSuperclass();
//        }
//        Object o = ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[1];
//
//        if (o instanceof TypeVariable) {
//            this.scriptInterfaceClass = (Class<ScriptInterface>) ((TypeVariable) o).getBounds()[0];
//        } else {
//            this.scriptInterfaceClass = (Class<ScriptInterface>) o;
//        }
    }

    /**
     * Find scripts by source type.
     *
     * @param type script source type
     * @return list of scripts
     */
    @SuppressWarnings("unchecked")
    public List<T> findByType(ScriptSourceTypeEnum type) {
        List<T> result = new ArrayList<T>();
        try {
            result = (List<T>) getEntityManager().createNamedQuery("CustomScript.getScriptInstanceByTypeActive").setParameter("sourceTypeEnum", type).getResultList();
        } catch (NoResultException e) {

        }
        return result;
    }

    @Override
    protected void afterUpdateOrCreate(T script) {
        compileScript(script, false);
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
        return getExecutionEngine(script, context);
    }

    @Override
    public ScriptInterface getExecutionEngine(T script, Map<String, Object> context) {
        try {
            ScriptInterface scriptInstance = this.getScriptInstance(script.getCode());

            // Call setters if those are provided
            if (script.getSourceTypeEnum() == ScriptSourceTypeEnum.JAVA) {
                for (Accessor setter : script.getSetters()) {
                    Object setterValue = context.get(setter.getName());
                    if (setterValue != null) {
                        // In case the parameters are initialized by a get request, we might need to convert the input to their right types
                        ScriptUtils.ClassAndValue classAndValue = new ScriptUtils.ClassAndValue();
                        if (!setter.getType().equals("String") && setterValue instanceof String) {
                            classAndValue = ScriptUtils.findTypeAndConvert(setter.getType(), (String) setterValue);
                        } else {
                            classAndValue.setValue(setterValue);
                            classAndValue.setClass(setterValue.getClass());
                        }

                        scriptInstance.getClass()
                                .getMethod(setter.getMethodName(), classAndValue.getTypeClass())
                                .invoke(scriptInstance, classAndValue.getValue());
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
        	// The script is probably not a Java script and we cannot retrieve its code using its class name
        	return context;
        }

        // Put getters' values to context
        if (script.getSourceTypeEnum() == ScriptSourceTypeEnum.JAVA) {
            for (Accessor getter : script.getGetters()) {
                try {
                    Object getterValue = engine.getClass()
                            .getMethod(getter.getMethodName())
                            .invoke(engine);
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

    /**
     * Construct classpath for script compilation
     *
     * @throws java.io.IOException
     */
    public void constructClassPath() throws IOException {

        if (classpath.length() == 0) {

            // Check if deploying an exploded archive or a compressed file
            String thisClassfile = this.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();

            File realFile = new File(thisClassfile);

            // Was deployed as exploded archive
            if (realFile.exists()) {
                File deploymentDir = realFile.getParentFile();
                for (File file : deploymentDir.listFiles()) {
                    if (file.getName().endsWith(".jar")) {
                        classpath += file.getCanonicalPath() + File.pathSeparator;
                    }
                }

                // War was deployed as compressed archive
            } else {

                org.jboss.vfs.VirtualFile vFile = org.jboss.vfs.VFS.getChild(thisClassfile);
                realFile = new File(org.jboss.vfs.VFSUtils.getPhysicalURI(vFile).getPath());

                File deploymentDir = realFile.getParentFile().getParentFile();

                Set<String> classPathEntries = new HashSet<>();

                for (File physicalLibDir : Objects.requireNonNull(deploymentDir.listFiles())) {
                    if (physicalLibDir.isDirectory()) {
                        for (File f : FileUtils.getFilesToProcess(physicalLibDir, "*", "jar")) {
                            classPathEntries.add(f.getCanonicalPath());
                        }
                    }
                }




                /* Fallback when thorntail is used */
                if (classPathEntries.isEmpty()) {
                    for (File physicalLibDir : Objects.requireNonNull(deploymentDir.listFiles())) {
                        if (physicalLibDir.isDirectory()) {
                            for (File subLib : Objects.requireNonNull(physicalLibDir.listFiles())) {
                                if (subLib.isDirectory()) {
                                    final List<String> jars = FileUtils.getFilesToProcess(subLib, "*", "jar")
                                            .stream()
                                            .map(this::getFilePath)
                                            .collect(Collectors.toList());
                                    classPathEntries.addAll(jars);
                                    if (subLib.getName().equals("classes")) {
                                        classPathEntries.add(subLib.getCanonicalPath());
                                    }
                                }
                            }
                        }
                    }
                } else {
                    // vfs used by thorntail
                    File vfsDir = deploymentDir.getParentFile().getParentFile();
                    for (File tempDir : Objects.requireNonNull(vfsDir.listFiles((dir, name) -> name.contains("temp")))) {
                        if (!tempDir.isDirectory()) {
                            continue;
                        }

                        for (File subTempDir : Objects.requireNonNull(tempDir.listFiles((dir, name) -> name.contains("temp")))) {
                            if (!subTempDir.isDirectory()) {
                                continue;
                            }

                            for (File warDir : Objects.requireNonNull(subTempDir.listFiles((dir, name) -> name.contains(".war")))) {
                                if (!warDir.isDirectory()) {
                                    continue;
                                }

                                for (File webInfDir : Objects.requireNonNull(warDir.listFiles((dir, name) -> name.equals("WEB-INF")))) {
                                    if (!webInfDir.isDirectory()) {
                                        continue;
                                    }

                                    for (File classesDir : Objects.requireNonNull(webInfDir.listFiles((dir, name) -> name.equals("classes")))) {
                                        if (classesDir.isDirectory()) {
                                            classPathEntries.add(classesDir.getCanonicalPath());
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                classpath = String.join(File.pathSeparator, classPathEntries);

            }
        }

    }

    private void checkEndpoints(CustomScript scriptInstance, List<Accessor> setters) throws BusinessException {

        // Check if endpoints parameters are not bound to deleted inputs
        final List<String> newInputs = setters
                .stream()
                .map(Accessor::getName)
                .collect(Collectors.toList());

        final List<String> currentInputs = scriptInstance.getInputs()
                .stream()
                .map(FunctionIO::getName)
                .collect(Collectors.toList());

        List<String> deletedProperties = new ArrayList<>(currentInputs);
        deletedProperties.removeAll(newInputs);

        final boolean hasEndpoint = deletedProperties.stream().anyMatch(o -> !endpointService.findByParameterName(scriptInstance.getCode(), o).isEmpty());

        if (hasEndpoint) {
            throw new BusinessException("An Endpoint is associated to one of those input : " + deletedProperties + " and therfore can't be deleted");
        }
    }

    private String getFilePath(File jar) {
        try {
            return jar.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Build the classpath and compile all scripts.
     *
     * @param scripts list of scripts
     */
    protected void compile(List<T> scripts) {
        try {

            constructClassPath();

            for (T script : scripts) {
                compileScript(script, false);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /*
     * Compile a script
     */
    public void refreshCompiledScript(String scriptCode) {

        T script = findByCode(scriptCode);
        if (script == null) {
            clearCompiledScripts(scriptCode);
        } else {
            compileScript(script, false);
        }
        // detach(script);
    }

    /**
     * Compile script, a and update script entity status with compilation errors. Successfully compiled script is added to a compiled script cache if active and not in test
     * compilation mode.
     *
     * @param script      Script entity to compile
     * @param testCompile Is it a compilation for testing purpose. Won't clear nor overwrite existing compiled script cache.
     */
    public void compileScript(T script, boolean testCompile) {

        List<ScriptInstanceError> scriptErrors = compileScript(script.getCode(), script.getSourceTypeEnum(), script.getScript(), script.isActive(), testCompile);

        script.setError(scriptErrors != null && !scriptErrors.isEmpty());
        script.setScriptErrors(scriptErrors);
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
		}catch(Exception e) {
			// Skip getter and setters parsing. We don't need to log errors as they will be logged later in code.
			return;
		}

		final ClassOrInterfaceDeclaration classOrInterfaceDeclaration = compilationUnit.getChildNodes()
		        .stream()
		        .filter(e -> e instanceof ClassOrInterfaceDeclaration)
		        .map(e -> (ClassOrInterfaceDeclaration) e)
		        .findFirst()
		        .get();

		final List<MethodDeclaration> methods = classOrInterfaceDeclaration.getMembers()
		        .stream()
		        .filter(e -> e instanceof MethodDeclaration)
		        .map(e -> (MethodDeclaration) e)
		        .collect(Collectors.toList());

		final List<Accessor> setters = methods.stream()
		        .filter(e -> e.getNameAsString().startsWith(SET))
		        .filter(e -> e.getModifiers().stream().anyMatch(modifier -> modifier.getKeyword().equals(Modifier.Keyword.PUBLIC)))
		        .filter(e -> e.getParameters().size() == 1)
		        .map(methodDeclaration -> {
		            Accessor setter = new Accessor();
		            String accessorFieldName = methodDeclaration.getNameAsString().substring(3);
		            setter.setName(Character.toLowerCase(accessorFieldName.charAt(0)) + accessorFieldName.substring(1));
		            setter.setType(methodDeclaration.getParameter(0).getTypeAsString());
		            setter.setMethodName(methodDeclaration.getNameAsString());
		            methodDeclaration.getComment()
		                    .ifPresent(comment -> comment.ifJavadocComment(javadocComment -> {
		                        javadocComment.parse()
		                                .getBlockTags()
		                                .stream()
		                                .filter(e -> e.getType() == JavadocBlockTag.Type.PARAM)
		                                .findFirst()
		                                .ifPresent(javadocBlockTag -> setter.setDescription(javadocBlockTag.getContent().toText()));
		                    }));
		            return setter;
		        }).collect(Collectors.toList());

		final List<Accessor> getters = methods.stream()
		        .filter(e -> e.getNameAsString().startsWith(GET) || e.getNameAsString().startsWith(IS))
		        .filter(e -> e.getModifiers().stream().anyMatch(modifier -> modifier.getKeyword().equals(Modifier.Keyword.PUBLIC)))
		        .filter(e -> e.getParameters().isEmpty())
		        .map(methodDeclaration -> {
		            Accessor getter = new Accessor();
		            String accessorFieldName;
		            if (methodDeclaration.getNameAsString().startsWith(GET)) {
		                accessorFieldName = methodDeclaration.getNameAsString().substring(3);
		            } else {
		                accessorFieldName = methodDeclaration.getNameAsString().substring(2);
		            }
		            getter.setName(Character.toLowerCase(accessorFieldName.charAt(0)) + accessorFieldName.substring(1));
		            getter.setMethodName(methodDeclaration.getNameAsString());
		            getter.setType(methodDeclaration.getTypeAsString());
		            methodDeclaration.getComment()
		                    .ifPresent(comment -> comment.ifJavadocComment(javadocComment -> {
		                        javadocComment.parse()
		                                .getBlockTags()
		                                .stream()
		                                .filter(e -> e.getType() == JavadocBlockTag.Type.RETURN)
		                                .findFirst()
		                                .ifPresent(javadocBlockTag -> getter.setDescription(javadocBlockTag.getContent().toText()));
		                    }));
		            return getter;
		        }).collect(Collectors.toList());

		checkEndpoints(script, setters);

		script.setGetters(getters);
		script.setSetters(setters);
    }

    /**
     * Compile script. DOES NOT update script entity status. Successfully compiled script is added to a compiled script cache if active and not in test compilation mode.
     *
     * @param scriptCode  Script entity code
     * @param sourceType  Source code language type
     * @param sourceCode  Source code
     * @param isActive    Is script active. It will compile it anyway. Will clear but not overwrite existing compiled script cache.
     * @param testCompile Is it a compilation for testing purpose. Won't clear nor overwrite existing compiled script cache.
     * @return A list of compilation errors if not compiled
     */
    private List<ScriptInstanceError> compileScript(String scriptCode, ScriptSourceTypeEnum sourceType, String sourceCode, boolean isActive, boolean testCompile) {
        if (sourceType == ScriptSourceTypeEnum.JAVA) {
            log.debug("Compile script {}", scriptCode);

            try {
                if (!testCompile) {
                    clearCompiledScripts(scriptCode);
                }

                Class<ScriptInterface> compiledScript = compileJavaSource(sourceCode);

                if (!testCompile && isActive) {

                    allScriptInterfaces.put(new CacheKeyStr(currentUser.getProviderCode(), scriptCode), () -> compiledScript.newInstance());
                    log.debug("Compiled script {} added to compiled interface map", scriptCode);
                }

                return null;

            } catch (CharSequenceCompilerException e) {
                log.error("Failed to compile script {}. Compilation errors:", scriptCode);

                List<ScriptInstanceError> scriptErrors = new ArrayList<>();

                List<Diagnostic<? extends JavaFileObject>> diagnosticList = e.getDiagnostics().getDiagnostics();
                for (Diagnostic<? extends JavaFileObject> diagnostic : diagnosticList) {
                    if ("ERROR".equals(diagnostic.getKind().name())) {
                        ScriptInstanceError scriptInstanceError = new ScriptInstanceError();
                        scriptInstanceError.setMessage(diagnostic.getMessage(Locale.getDefault()));
                        scriptInstanceError.setLineNumber(diagnostic.getLineNumber());
                        scriptInstanceError.setColumnNumber(diagnostic.getColumnNumber());
                        scriptInstanceError.setSourceFile(diagnostic.getSource().toString());
                        // scriptInstanceError.setScript(scriptInstance);
                        scriptErrors.add(scriptInstanceError);
                        // scriptInstanceErrorService.create(scriptInstanceError, scriptInstance.getAuditable().getCreator());
                        log.warn("{} script {} location {}:{}: {}", diagnostic.getKind().name(), scriptCode, diagnostic.getLineNumber(), diagnostic.getColumnNumber(),
                                diagnostic.getMessage(Locale.getDefault()));
                    }
                }
                return scriptErrors;

            } catch (Exception e) {
                log.error("Failed while compiling script", e);
                List<ScriptInstanceError> scriptErrors = new ArrayList<>();
                ScriptInstanceError scriptInstanceError = new ScriptInstanceError();
                scriptInstanceError.setMessage(e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName());
                scriptErrors.add(scriptInstanceError);

                return scriptErrors;
            }
        } else {
            ScriptInterface engine = new ES5ScriptEngine(sourceCode);
            allScriptInterfaces.put(new CacheKeyStr(currentUser.getProviderCode(), scriptCode), () -> engine);
            return null;
        }
    }


    /**
     * Compile java Source script
     *
     * @param javaSrc Java source to compile
     * @return Compiled class instance
     * @throws org.meveo.service.script.CharSequenceCompilerException char sequence compiler exception.
     */
    protected Class<ScriptInterface> compileJavaSource(String javaSrc) throws CharSequenceCompilerException {

        supplementClassPathWithMissingImports(javaSrc);

        String fullClassName = getFullClassname(javaSrc);

        log.trace("Compile JAVA script {} with classpath {}", fullClassName, classpath);

        compiler = new CharSequenceCompiler<ScriptInterface>(this.getClass().getClassLoader(), Arrays.asList("-cp", classpath));
        final DiagnosticCollector<JavaFileObject> errs = new DiagnosticCollector<JavaFileObject>();
        Class<ScriptInterface> compiledScript = compiler.compile(fullClassName, javaSrc, errs, ScriptInterface.class);
        return compiledScript;
    }

    /**
     * Supplement classpath with classes needed for the particular script compilation. Solves issue when classes server as jboss modules are referenced in script. E.g.
     * prg.slf4j.Logger
     *
     * @param javaSrc Java source to compile
     */
    @SuppressWarnings("rawtypes")
    private void supplementClassPathWithMissingImports(String javaSrc) {

        String regex = "import (.*?);";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(javaSrc);
        while (matcher.find()) {
            String className = matcher.group(1);
            try {
                if ((!className.startsWith("java") || className.startsWith("javax.persistence")) && !className.startsWith("org.meveo")) {
                    Class clazz = Class.forName(className);
                    try {
                        String location = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
                        if (location.startsWith("file:")) {
                            location = location.substring(5);
                        }
                        if (location.endsWith("!/")) {
                            location = location.substring(0, location.length() - 2);
                        }

                        if (!classpath.contains(location)) {
                            classpath += File.pathSeparator + location;
                        }

                    } catch (Exception e) {
                        log.warn("Failed to find location for class {}", className);
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to find location for class {}", className);
            }
        }

    }

    /**
     * Find the script class for a given script code
     *
     * @param scriptCode Script code
     * @return Script interface Class
     * @throws Exception
     */
    @Lock(LockType.READ)
    public ScriptInterface getScriptInterface(String scriptCode) throws Exception {
        ScriptInterfaceSupplier supplier = allScriptInterfaces.get(new CacheKeyStr(currentUser.getProviderCode(), scriptCode));

        if (supplier == null) {
            supplier = getScriptInterfaceWCompile(scriptCode);
        }

        return supplier.getScriptInterface();
    }

    /**
     * Compile the script class for a given script code if it is not compile yet. NOTE: method is executed synchronously due to WRITE lock. DO NOT CHANGE IT, so there would be only
     * one attempt to compile a new script class
     *
     * @param scriptCode Script code
     * @return Script interface Class
     * @throws InvalidScriptException   Were not able to instantiate or compile a script
     * @throws ElementNotFoundException Script not found
     */
    @Lock(LockType.WRITE)
    protected ScriptInterfaceSupplier getScriptInterfaceWCompile(String scriptCode) throws ElementNotFoundException, InvalidScriptException {
        ScriptInterfaceSupplier result = null;

        result = allScriptInterfaces.get(new CacheKeyStr(currentUser.getProviderCode(), scriptCode));

        if (result == null) {
            T script = findByCode(scriptCode);
            if (script == null) {
                log.debug("ScriptInstance with {} does not exist", scriptCode);
                throw new ElementNotFoundException(scriptCode, getEntityClass().getName());
            }
            compileScript(script, false);
            if (script.isError()) {
                log.debug("ScriptInstance {} failed to compile. Errors: {}", scriptCode, script.getScriptErrors());
                throw new InvalidScriptException(scriptCode, getEntityClass().getName());
            }
            result = allScriptInterfaces.get(new CacheKeyStr(currentUser.getProviderCode(), scriptCode));
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
     * @throws InvalidScriptException   Were not able to instantiate or compile a script
     * @throws ElementNotFoundException Script not found
     */
    @Lock(LockType.READ)
    public ScriptInterface getScriptInstance(String scriptCode) throws ElementNotFoundException, InvalidScriptException {
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
     * Gets a full classname of a script by combining a package (if applicable) and a classname
     *
     * @param script Java source code
     * @return Full classname
     */
    public static String getFullClassname(String script) {
        String packageName = getPackageName(script);
        String className = getClassName(script);
        return (packageName != null ? packageName.trim() + "." : "") + className;
    }

    /**
     * Remove compiled script, its logs and cached instances for given script code
     *
     * @param scriptCode Script code
     */
    public void clearCompiledScripts(String scriptCode) {
        super.clear(scriptCode);
        allScriptInterfaces.remove(new CacheKeyStr(currentUser.getProviderCode(), scriptCode));
    }

    public void clearCompiledScripts() {
        allScriptInterfaces.clear();
    }

    @Override
    public List<ExpectedOutput> compareResults(List<ExpectedOutput> expectedOutputs, Map<String, Object> results) {
        return null;
    }

}