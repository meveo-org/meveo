package org.meveo.service.script;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.EjbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by Hien Bach on 4/5/2019.
 */
public class ES5ScriptEngine implements ScriptInterface {
	
    private String script;
    private AtomicBoolean isInterrupted = new AtomicBoolean(false);
    public Map<String, Object> methodContext;
    private ScriptEngine jsEngine;
    private CompletableFuture<?> scriptExecution;
    
    private static Logger LOG = LoggerFactory.getLogger(ES5ScriptEngine.class);

    @Override
    public void init(Map<String, Object> methodContext) throws BusinessException {

    }

    @Override
    public void execute(Map<String, Object> methodContext) throws BusinessException {
    	this.methodContext = methodContext;
    	
        try {
        	scriptExecution = CompletableFuture.runAsync(() -> {
				try {
		            jsEngine = new ScriptEngineManager().getEngineByName("graal.js");
		            Bindings bindings = jsEngine.createBindings();
		            bindings.put("polyglot.js.allowAllAccess", true);
		            bindings.putAll(methodContext);
		            
		            bindings.put("methodContext", methodContext);
		            bindings.put("JAVA_CTX", new JavaCtx());
		            
		            ScriptContext scriptContext = new SimpleScriptContext();
		            scriptContext.setBindings(bindings, ScriptContext.GLOBAL_SCOPE);
		            jsEngine.setContext(scriptContext);
		            
					jsEngine.eval(script);
				} catch (ScriptException e) {
		        	LOG.error("Error executing script", e);
				}
			});
        	
        	scriptExecution.get();
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
			scriptExecution.get();
			
		} catch (Exception e) {
			LOG.error("Error cancelling script", e);
		}
		
        return methodContext;
    }

    public ES5ScriptEngine(String script) {
        this.script = script;
    }

    public class JavaCtx {
    	
    	public Object getServiceInterface(String name) {
    		return EjbUtils.getServiceInterface(name);
    	}
    	
    	public boolean isInterrupted() {
    		return isInterrupted.get();
    	}
    	
    	public Thread getCurrentThread() {
    		return Thread.currentThread();
    	}
    }
}