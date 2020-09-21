/**
 * 
 */
package org.meveo.service.script.cache;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;
import javax.ejb.AsyncResult;
import javax.ejb.Asynchronous;
import javax.ejb.ConcurrencyManagement;
import javax.ejb.ConcurrencyManagementType;
import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;
import javax.inject.Provider;
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaFileObject;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.DependencyFilterUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.cache.CacheKeyStr;
import org.meveo.commons.utils.FileUtils;
import org.meveo.commons.utils.MeveoFileUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.customEntities.CETConstants;
import org.meveo.model.customEntities.CustomEntityCategory;
import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.customEntities.CustomModelObject;
import org.meveo.model.customEntities.CustomRelationshipTemplate;
import org.meveo.model.customEntities.CustomTableRecord;
import org.meveo.model.customEntities.GraphQLQueryField;
import org.meveo.model.customEntities.Mutation;
import org.meveo.model.git.GitRepository;
import org.meveo.model.scripts.CustomScript;
import org.meveo.model.scripts.MavenDependency;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.scripts.ScriptInstanceError;
import org.meveo.model.scripts.ScriptSourceTypeEnum;
import org.meveo.security.CurrentUser;
import org.meveo.security.MeveoUser;
import org.meveo.service.config.impl.MavenConfigurationService;
import org.meveo.service.custom.CustomEntityTemplateCompiler;
import org.meveo.service.git.GitHelper;
import org.meveo.service.git.MeveoRepository;
import org.meveo.service.script.CharSequenceCompiler;
import org.meveo.service.script.CharSequenceCompilerException;
import org.meveo.service.script.CustomScriptService;
import org.meveo.service.script.ES5ScriptEngine;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.script.ScriptInterface;
import org.meveo.service.script.ScriptInterfaceSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author clement.bareth
 * @since 6.11.0
 * @version 6.11.0
 */
@Singleton
@ConcurrencyManagement(ConcurrencyManagementType.BEAN)
@Lock(LockType.READ)
public class ScriptInstancesCache {
	
	/**
	 * Class path used to compile the java scripts
	 */
    public static final AtomicReference<String> CLASSPATH_REFERENCE = new AtomicReference<>("");
    
    private static final ConcurrentHashMap<CacheKeyStr, ScriptInterfaceSupplier> ALL_SCRIPT_INTERFACES = new ConcurrentHashMap<>();
    private static Logger staticLogger = LoggerFactory.getLogger(CustomScriptService.class);
    
    private static final Map<String, Future<?>> compilations = new ConcurrentHashMap<>();

    /**
     * Construct classpath for script compilation
     * @throws IOException if we fail to retrieve one of the jar file path
     */
    public static void constructClassPath() throws IOException {
        if (CLASSPATH_REFERENCE.get().length() == 0) {
            synchronized (CLASSPATH_REFERENCE) {
                if (CLASSPATH_REFERENCE.get().length() == 0) {
                    String classpath = CLASSPATH_REFERENCE.get();

                    // Check if deploying an exploded archive or a compressed file
                    String thisClassfile = new Object() {}.getClass().getProtectionDomain().getCodeSource().getLocation().getFile();

                    // Handle wildfly modules
                    
                    
                    File realFile = new File(thisClassfile);

                    // Was deployed as exploded archive
                    if (realFile.exists()) {
                        File deploymentDir = realFile.getParentFile();
                        System.out.println("Deployment dir: " + deploymentDir.getAbsolutePath());
                        File[] files = deploymentDir.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.getName().endsWith(".jar")) {
                                    classpath += file.getCanonicalPath() + File.pathSeparator;
                                }
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
                                            		.map(item->getFilePath(item))
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
                    
                    CLASSPATH_REFERENCE.set(classpath);
                    
                	String cpt = System.getProperty("java.class.path");
                	File wildflyFolder = new File(cpt).getParentFile();
                	File moduleFolder = new File(wildflyFolder, "modules/system/layers/base");
                	addJarsToClassPath(moduleFolder);
                }
            }
       }
            
        System.out.println("Final classpath : " + CLASSPATH_REFERENCE.get());
    }
    
    private static void addJarsToClassPath(File directory) {
        for (File file : directory.listFiles()) {
            if (file.getName().endsWith(".jar")) {
				try {
					String canonicalPath = file.getCanonicalPath();
					String classPath = CLASSPATH_REFERENCE.get();
					classPath += canonicalPath + File.pathSeparator;
					CLASSPATH_REFERENCE.set(classPath);
					
				} catch (IOException e) {
					staticLogger.warn("Can't add jar file to class path", e);
				}
				
            } else if(file.isDirectory()) {
            	addJarsToClassPath(file);
            }
        }
    }
    
    private static String getFilePath(File jar) {
        try {
            return jar.getCanonicalPath();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Inject
    @CurrentUser
    protected Provider<MeveoUser> currentUser;
    
    @Inject
    private CustomEntityTemplateCompiler cetCompiler;
    
    private Map<String, List<File>> compiledCustomEntities = new ConcurrentHashMap<>();

    private RepositorySystem defaultRepositorySystem;
    
    private RepositorySystemSession defaultRepositorySystemSession;
    
    @Inject
    private Logger log;
    
    @Inject
    private MavenConfigurationService mavenConfigurationService;
    
    @Inject
    @MeveoRepository
    private GitRepository meveoRepository;
    
    @Inject
    private Provider<ScriptInstanceService> scriptInstanceService;
    
    /**
     * Add a jar file to application class path
     * 
     * @param location location of the jar file
     */
    public void addLibrary(String location) {
        File file = new File(location);
        
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        
        if(systemClassLoader instanceof URLClassLoader) {
			URLClassLoader classLoader = (URLClassLoader) systemClassLoader;
	        Method method;
	
	        try {
	            URL url = file.toURI().toURL();
	            method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
	            method.setAccessible(true);
	            method.invoke(classLoader, url);
	
	        } catch (Exception e) {
	            throw new RuntimeException(e);
	        }
	        
        } else {
	        try {
	        	Method method = systemClassLoader.getClass().getDeclaredMethod("appendToClassPathForInstrumentation", String.class);
	            method.setAccessible(true);
	            method.invoke(systemClassLoader, file.getAbsolutePath());
	
	        } catch (Exception e) {
	        	log.warn("Libray {} not added to classpath", location);
	        	log.warn("Can't access system class loader class, please restart JVM with the following options : --add-opens java.base/jdk.internal.loader=ALL-UNNAMED --add-exports=java.base/jdk.internal.loader=ALL-UNNAMED");
	        }
        }
    }
    
    /**
     * Clear script cache
     */
    public void clearCompiledScripts() {
        ALL_SCRIPT_INTERFACES.clear();
    }
    
    /**
     * Remove compiled script, its logs and cached instances for given script code
     *
     * @param scriptCode Script code
     */
    public void clearCompiledScripts(String scriptCode) {
        ALL_SCRIPT_INTERFACES.remove(new CacheKeyStr(currentUser.get().getProviderCode(), scriptCode));
    }
    
    /**
     * Build the classpath and compile all scripts.
     *
     * @param scripts list of scripts
     */
    public <T extends CustomScript> void compile(List<T> scripts) {
        try {

            constructClassPath();

            for (T script : scripts) {
                compileScript(script, false);
            }
        } catch (Exception e) {
            log.error("", e);
        }
    }

    /**
     * Compile java Source script
     *
     * @param javaSrc Java source to compile
     * @return Compiled class instance
     * @throws CharSequenceCompilerException char sequence compiler exception.
     */
    public Class<ScriptInterface> compileJavaSource(String javaSrc) throws CharSequenceCompilerException {

        List<File> fileList = supplementClassPathWithMissingImports(javaSrc);

        String fullClassName = CustomScriptService.getFullClassname(javaSrc);

        String classPath = CLASSPATH_REFERENCE.get();

        log.trace("Compile JAVA script {} with classpath {}", fullClassName, classPath);

        CharSequenceCompiler<ScriptInterface> compiler = new CharSequenceCompiler<>(this.getClass().getClassLoader(), Arrays.asList("-cp", classPath));
        final DiagnosticCollector<JavaFileObject> errs = new DiagnosticCollector<>();
        return compiler.compile(fullClassName, javaSrc, errs, fileList, ScriptInterface.class);
    }
    
	/**
     * Compile script, a and update script entity status with compilation errors.
     * Successfully compiled script is added to a compiled script cache if active
     * and not in test compilation mode.
     *
     * @param script      Script entity to compile
     * @param testCompile Is it a compilation for testing purpose. Won't clear nor
     *                    overwrite existing compiled script cache.
	 * @throws InterruptedException 
     */
    @TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
    public void compileScript(CustomScript script, boolean testCompile) {
    	log.debug("Compiling script {}", script.getCode()); //FIXME remove
    	
    	/* if(!testCompile) {
			boolean compilationWasNotStarted = true;

    		try {
    			Future<?> compilation = compilations.computeIfAbsent(script.getCode(), k -> compileAsync(script));
    			
    	    	log.debug("Retrieving compilation " + compilations); //FIXME remove

//    			if(compilation != null) {
//    				log.debug("Compilation for script {} already running, waiting for end of compilation", script.getCode());
//    			} else {
//    				compilationWasNotStarted = true;
//    				//compilation = 	
//				}
    			
    			compilation.get();
			} catch (InterruptedException | ExecutionException e) {
				throw new RuntimeException(e);
			} finally {
				if(compilationWasNotStarted) {
					CompletableFuture.runAsync(() -> {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {}
						compilations.remove(script.getCode());
					});
				}
			}
    		
    	} else { */
//    		final String source;
//            source = script.getScript();
//	        addScriptDependencies(script);
//	        List<ScriptInstanceError> scriptErrors = compileScript(script.getCode(), script.getSourceTypeEnum(), source, script.isActive(), true);
//	        script.setError(scriptErrors != null && !scriptErrors.isEmpty());
//	        script.setScriptErrors(scriptErrors);
    	//}
    	
    	final String source;
        if (testCompile || !findScriptFile(script).exists()) {
            source = script.getScript();
        } else {
            source = readScriptFile(script);
        }
        addScriptDependencies(script);
        List<ScriptInstanceError> scriptErrors = compileScript(script.getCode(), script.getSourceTypeEnum(), source, script.isActive(), false);
        script.setError(scriptErrors != null && !scriptErrors.isEmpty());
        script.setScriptErrors(scriptErrors);
    }
    
    @Asynchronous
    public Future<CustomScript> compileAsync(CustomScript script) {
    	final String source;
        if (!findScriptFile(script).exists()) {
            source = script.getScript();
        } else {
            source = readScriptFile(script);
        }
        addScriptDependencies(script);
        List<ScriptInstanceError> scriptErrors = compileScript(script.getCode(), script.getSourceTypeEnum(), source, script.isActive(), false);
        script.setError(scriptErrors != null && !scriptErrors.isEmpty());
        script.setScriptErrors(scriptErrors);
        return new AsyncResult<>(script);
    }
	
    /**
     * @param scriptInstance the script
     * @return the physical file reflecting the script 
     */
    public File findScriptFile(CustomScript scriptInstance) {
        final File repositoryDir = GitHelper.getRepositoryDir(currentUser.get(), meveoRepository.getCode() + "/src/main/java/");
        final File scriptDir = new File(repositoryDir, "/scripts");
        if (!scriptDir.exists()) {
            scriptDir.mkdirs();
        }

        String extension = scriptInstance.getSourceTypeEnum() == ScriptSourceTypeEnum.ES5 ? ".js" : ".java";
        String path = scriptInstance.getCode().replaceAll("\\.", "/");
        return new File(scriptDir, path + extension);
    }

    /**
     * @return the script cache
     */
    public Map<CacheKeyStr, ScriptInterfaceSupplier> getScriptCache() {
    	return ALL_SCRIPT_INTERFACES;
    }
    
    /**
     * Find the script class for a given script code
     *
     * @param scriptCode Script code
     * @return Script interface Class
     */
    public ScriptInterfaceSupplier getScriptInterfaceSupplier(String scriptCode) {
        return ALL_SCRIPT_INTERFACES.get(new CacheKeyStr(currentUser.get().getProviderCode(), scriptCode));
    }
	
    /**
     * @param script the script
     * @return the content as string of the physical script file
     */
    public String readScriptFile(CustomScript script) {
        File scriptFile = findScriptFile(script);

        try {
            return MeveoFileUtils.readString(scriptFile.getAbsolutePath());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private void addScriptDependencies(CustomScript script) {

		if (script instanceof ScriptInstance) {
			ScriptInstance scriptInstance = (ScriptInstance) script;

			if (scriptInstance.getMavenDependencies() != null && scriptInstance.getMavenDependencies().size() > 0) {
				Set<String> mavenDependencies = getMavenDependencies(scriptInstance.getMavenDependenciesNullSafe());

				synchronized (CLASSPATH_REFERENCE) {
					mavenDependencies.stream().forEach(location -> {
						if (!StringUtils.isBlank(location) && !CLASSPATH_REFERENCE.get().contains(location)) {
							addLibrary(location);
							CLASSPATH_REFERENCE.set(CLASSPATH_REFERENCE.get() + File.pathSeparator + location);
						}
					});
				}
			}
		}
    }
    
    /**
     * Compile script. DOES NOT update script entity status. Successfully compiled
     * script is added to a compiled script cache if active and not in test
     * compilation mode.
     *
     * @param scriptCode  Script entity code
     * @param sourceType  Source code language type
     * @param sourceCode  Source code
     * @param isActive    Is script active. It will compile it anyway. Will clear
     *                    but not overwrite existing compiled script cache.
     * @param testCompile Is it a compilation for testing purpose. Won't clear nor
     *                    overwrite existing compiled script cache.
     * @return A list of compilation errors if not compiled
     */
    private List<ScriptInstanceError> compileScript(String scriptCode, ScriptSourceTypeEnum sourceType, String sourceCode, boolean isActive, boolean testCompile) {
        if (sourceType == ScriptSourceTypeEnum.JAVA) {
            log.debug("Compile script {}", scriptCode);

            try {
                if (!testCompile) {
                    clearCompiledScripts(scriptCode);
                }

                Class<ScriptInterface> compiledScriptClass = compileJavaSource(sourceCode);
                
                if (!testCompile && isActive) {

                    ALL_SCRIPT_INTERFACES.put(new CacheKeyStr(currentUser.get().getProviderCode(), scriptCode), () -> compiledScriptClass.getDeclaredConstructor().newInstance());
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
                        // scriptInstanceErrorService.create(scriptInstanceError,
                        // scriptInstance.getAuditable().getCreator());
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
            ALL_SCRIPT_INTERFACES.put(new CacheKeyStr(currentUser.get().getProviderCode(), scriptCode), () -> engine);
            return null;
        }
    }
    
    private Set<String> getMavenDependencies(Set<MavenDependency> mavenDependencies) {

		Set<String> result = new HashSet<>();
		List<String> repos = mavenConfigurationService.getMavenRepositories();
		String m2FolderPath = mavenConfigurationService.getM2FolderPath();

		if (!StringUtils.isBlank(m2FolderPath)) {
			String repoId = RandomStringUtils.randomAlphabetic(5);
			List<RemoteRepository> remoteRepositories = repos.stream()
					.map(e -> new RemoteRepository.Builder(repoId, "default", e)
					.build())
					.collect(Collectors.toList());

			log.debug("found {} repositories", remoteRepositories.size());

			if (mavenDependencies != null && !mavenDependencies.isEmpty()) {
				Set<ArtifactResult> artifacts = mavenDependencies.stream().map(e -> {
					DefaultArtifact rootArtifact = new DefaultArtifact(e.getGroupId(), e.getArtifactId(), e.getClassifier(), "jar", e.getVersion());
					DependencyFilter classpathFlter = DependencyFilterUtils.classpathFilter(JavaScopes.COMPILE);
					Dependency root = new Dependency(rootArtifact, JavaScopes.COMPILE);

					try {
						// check if artifact exists in local repository or in maven central
						CollectRequest collectRequestLocal = new CollectRequest();
						collectRequestLocal.setRoot(root);

						DependencyRequest theDependencyRequest = new DependencyRequest(collectRequestLocal, classpathFlter);
						DependencyResult dependencyResult = defaultRepositorySystem.resolveDependencies(defaultRepositorySystemSession, theDependencyRequest);
						
						return dependencyResult.getArtifactResults();

					} catch (DependencyResolutionException e1) {
						
						// If local repository resolution failed, try with remote
						log.info("Local dependencies resolution failed ({}), trying with remote resolution", e1.getMessage());
						try {
							CollectRequest collectRequestRemote = new CollectRequest();
							collectRequestRemote.setRoot(root);
							remoteRepositories.forEach(collectRequestRemote::addRepository);
							
							DependencyRequest theDependencyRequest = new DependencyRequest(collectRequestRemote, classpathFlter);
							DependencyResult dependencyResult = defaultRepositorySystem.resolveDependencies(defaultRepositorySystemSession, theDependencyRequest);
							return dependencyResult.getArtifactResults();
							
						} catch (DependencyResolutionException e2) {
							log.error("Fail downloading dependencies {}", e1);
							return null; // TODO handle it
						}

					}
				}).filter(Objects::nonNull)
				.flatMap(Collection::stream)
				.collect(Collectors.toSet());

				log.debug("found {} artifacts", artifacts.size());

				result = artifacts.stream().filter(Objects::nonNull).map(e -> {
					return e.getArtifact().getFile().getPath();
				}).collect(Collectors.toSet());
			}
		}

		return result;
	}
    
    @PostConstruct
    private void init() {
        if(mavenConfigurationService.getM2FolderPath() != null) {
            defaultRepositorySystem = mavenConfigurationService.newRepositorySystem();
            defaultRepositorySystemSession = mavenConfigurationService.newRepositorySystemSession(defaultRepositorySystem);
        }
    }

    /**
     * Recursively read the dependency of CET in a script.
     * 
     * @param pattern import patter
     * @param className class name of the cet
     * @return a list of CET java class file names.
     * @throws BusinessException if a dependency file can't be retrieved
     */
    private List<File> parseImportCustomEntity(Pattern pattern, String className) throws BusinessException {
    	// Skip if class is a meveo-model class
    	// We must do that because generated entities have the same package than these classes
    	List<Class<?>> modelClasses = List.of(
			CustomEntityTemplate.class,
			CustomEntityInstance.class,
			CustomEntityCategory.class,
			CETConstants.class,
			CustomModelObject.class,
			CustomRelationshipTemplate.class,
			CustomTableRecord.class,
			GraphQLQueryField.class,
			Mutation.class
		);
    	
    	boolean isModelClass = modelClasses.stream()
    			.map(Class::getName)
    			.anyMatch(className::equals);
    	
    	if(isModelClass) {
    		return new ArrayList<>();
    	}
    	
    	List<File> files = new ArrayList<>();

    	try {
            if (className.startsWith("org.meveo.model.customEntities")) {
                String fileName = className.split("\\.")[4];
                File file = cetCompiler.getCETSourceFile(fileName);
                String content = org.apache.commons.io.FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                Matcher matcher2 = pattern.matcher(content);
                while (matcher2.find()) {
                    String name = matcher2.group(1);
                    if (name.startsWith("org.meveo.model.customEntities") && !name.equals(className)) {
                    	var depFiles = compiledCustomEntities.computeIfAbsent(name, k -> {
							try {
								return parseImportCustomEntity(pattern, name);
							} catch (Exception e) {
								log.error("Failed to recursively parse dependencies for class {}", name, e);
								return List.of();
							}
						});
						files.addAll(depFiles);
                    }
                }
                
                files.add(file);
                
            } else {
                String name = className.replace('.', '/');
                File file = new File(GitHelper.getRepositoryDir(currentUser.get(), meveoRepository.getCode()).getAbsolutePath() + "/src/main/java/", "scripts/" + name + ".java");
                if (file.exists()) {
                    ScriptInstance scriptInstance = scriptInstanceService.get().findByCode(className);
                    populateImportScriptInstance(scriptInstance, files);
                    files.add(file);
                }
            }
            
        } catch (IOException e) {
        	log.warn("Miss matcher when loading custom entities {}", e.getMessage());
        }
    	
    	return files;
    }
    
    /**
     * Populate import script instance.
     *
     * @param scriptInstance script instance
     * @param files list of files
     */
    private void populateImportScriptInstance(ScriptInstance scriptInstance, List<File> files) {
        try {
            if (scriptInstance != null) {
                String javaSource = scriptInstance.getScript();
                String regex = "import (.*?);";
                Pattern pattern = Pattern.compile(regex);
                Matcher matcher = pattern.matcher(javaSource);
                while (matcher.find()) {
                    String className = matcher.group(1);
                    if (className.startsWith("org.meveo.model.customEntities")) {
                        String fileName = className.split("\\.")[4];
                        File file = new File(GitHelper.getRepositoryDir(currentUser.get(), meveoRepository.getCode()).getAbsolutePath() + "/src/main/java/","custom" + File.separator + "entities" + File.separator + fileName + ".java");
                        String content = org.apache.commons.io.FileUtils.readFileToString(file, StandardCharsets.UTF_8);
                        matcher = pattern.matcher(content);
                        while (matcher.find()) {
                            String name = matcher.group(1);
                            if (name.startsWith("org.meveo.model.customEntities")) {
                                String cetName = name.split("\\.")[4];
                                File cetFile = cetCompiler.getCETSourceFile(cetName);
                                files.add(cetFile);
                                continue;
                            }
                        }
                        files.add(file);
                        continue;
                    }
                }
                if (scriptInstance.getImportScriptInstances() != null && CollectionUtils.isNotEmpty(scriptInstance.getImportScriptInstances())) {
                    for (ScriptInstance instance : scriptInstance.getImportScriptInstancesNullSafe()) {
                        String path = instance.getCode().replace('.', '/');
                        File fileImport = new File(GitHelper.getRepositoryDir(currentUser.get(), meveoRepository.getCode()).getAbsolutePath() + "/src/main/java/", "scripts" + File.separator + path + ".java");
                        if (fileImport.exists()) {
                            files.add(fileImport);
                        }
                        populateImportScriptInstance(instance, files);
                    }
                }
            }
        } catch (Exception e) {}
    }
    
	/**
     * Supplement classpath with classes needed for the particular script
     * compilation. Solves issue when classes server as jboss modules are referenced
     * in script. E.g. prg.slf4j.Logger
     *
     * @param javaSrc Java source to compile
     */
    @SuppressWarnings("rawtypes")
    private List<File> supplementClassPathWithMissingImports(String javaSrc) {
        Set<File> files = new HashSet<>();

        String regex = "import (.*?);";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(javaSrc);
        while (matcher.find()) {
            String className = matcher.group(1);
            try {
				files.addAll(parseImportCustomEntity(pattern, className));
			} catch (BusinessException e1) {
				log.error("Failed to parse custom entities references", e1);
			}
            
            try {
                if ((!className.startsWith("java") || className.startsWith("javax.persistence")) && !className.startsWith("org.meveo")) {
                	Class clazz;
                	
                	try {
                		ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	                    clazz = classLoader.loadClass(className);
	                    
                	} catch(ClassNotFoundException e) {
                		clazz = Class.forName(className);
                	}
                	
                    try {
                        String location = clazz.getProtectionDomain().getCodeSource().getLocation().getFile();
                        if (location.startsWith("file:")) {
                            location = location.substring(5);
                        }
                        
                        if (location.endsWith("!/")) {
                            location = location.substring(0, location.length() - 2);
                        }

                        if (!CLASSPATH_REFERENCE.get().contains(location)) {
                            synchronized (CLASSPATH_REFERENCE) {
                                if (!CLASSPATH_REFERENCE.get().contains(location)) {
                                    CLASSPATH_REFERENCE.set(CLASSPATH_REFERENCE.get() + File.pathSeparator + location);
                                }
                            }
                        }

                    } catch (Exception e) {
                        log.warn("Failed to find location for class {} with error {}", className, e.getMessage());
                    }
                }
                
            } catch (Exception e) {
                log.warn("Failed to find location for class {} with error {}", className, e.getMessage());
            }
        }
        
        return new ArrayList<>(files);
    }

	
}
