package org.meveo.service.script.engines;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

import javax.enterprise.inject.spi.CDI;

import org.apache.commons.io.IOUtils;
import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Value;
import org.meveo.admin.exception.BusinessException;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.service.config.impl.MavenConfigurationService;
import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.script.ScriptInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Hien Bach on 4/5/2019.
 */
public class ES5ScriptEngine implements ScriptInterface {
	
    private String script;
    private AtomicBoolean isInterrupted = new AtomicBoolean(false);
    public Map<String, Object> methodContext;
    private String code;
    
    private static Logger LOG = LoggerFactory.getLogger(ES5ScriptEngine.class);

    public ES5ScriptEngine(String script, String code) {
        this.script = script;
        this.code = code;
    }

    @Override
    public void init(Map<String, Object> methodContext) throws BusinessException {

    }

    @Override
    public void execute(Map<String, Object> methodContext) throws BusinessException {
    	this.methodContext = methodContext;
    	    	
        try {

			try (Context context = Context.newBuilder("js")
						  .allowHostClassLoading(true)
                          .allowAllAccess(true)
                          .allowPolyglotAccess(PolyglotAccess.ALL)
                          .build()) {
				 
	            var jsBindings = context.getBindings("js");
	            jsBindings.putMember("polyglot.js.allowAllAccess", true);
	            methodContext.forEach(jsBindings::putMember);
	            jsBindings.putMember("methodContext", methodContext);
	            
	            // Function bindings
	            Supplier<Boolean> isInterruptedSupplier = () -> isInterrupted.get();
	            jsBindings.putMember("isInterrupted", isInterruptedSupplier);
	            
	            jsBindings.putMember("requireService", JavaBindings.CDI_SUPPLIER);

	            jsBindings.putMember("requireFunction", JavaBindings.FUNCTION_SUPPLIER);
	            
	            Function<String, Value> npmRequire = (name) -> {
	            	String m2Dir = MavenConfigurationService.getM2Directory(null);
	            	File libFolder = Paths.get(m2Dir, "org", "webjars", "npm", name).toFile();
	            	if (!libFolder.exists() || !libFolder.isDirectory()) {
	            		return null;
	            	}
	            	
	            	File versionFolder = libFolder.listFiles()[0];
	            	File libFile = new File(versionFolder, name + "-" + versionFolder.getName() + ".jar");
	            	try (JarFile jarFile = new JarFile(libFile)) {
						
						String basePath = "META-INF/resources/webjars/" + name + "/" + versionFolder.getName() + "/";				
						ZipEntry packageJson = jarFile.getEntry(basePath + "package.json");					
						
						try (var is = jarFile.getInputStream(packageJson)) {
							var packageJsonMap = JacksonUtil.read(is, Map.class);
							String mainFilePath = (String) packageJsonMap.get("main");
							ZipEntry mainFile = jarFile.getEntry(basePath + mainFilePath);
							
							try (var libStream = jarFile.getInputStream(mainFile)) {
								String source = IOUtils.toString(libStream, "UTF-8");
								var returnValue = context.eval("js", source);
								
								return returnValue;
							}
						}
	            	} catch (IOException e) {
						LOG.error("Failed to read file", e);
					}
	            	
	            	return null;
	            };
	            jsBindings.putMember("require", npmRequire);
	            
	            Logger scriptLogger = LoggerFactory.getLogger(code);
	            jsBindings.putMember("log", scriptLogger);
	            
	            context.eval("js", script);
	            
			}
	
        } catch (Exception e) {
        	LOG.error("Error executing script", e);
        }
    }

    @Override
    public void finalize(Map<String, Object> methodContext) throws BusinessException {

    }

    @Override
    public Map<String, Object> cancel() {
		try {
			isInterrupted.set(true);
			
		} catch (Exception e) {
			LOG.error("Error cancelling script", e);
		}
		
        return methodContext;
    }
    
}