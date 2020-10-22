package org.meveo.service.script;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import org.meveo.admin.exception.BusinessException;
import org.meveo.commons.utils.EjbUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.graalvm.polyglot.Context;

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
				try (Context context = Context.newBuilder("js")
	                          .allowAllAccess(true)
	                          .build()) {
					 
		            var jsBindings = context.getBindings("js");
		            jsBindings.putMember("polyglot.js.allowAllAccess", true);
		            methodContext.forEach(jsBindings::putMember);
		            jsBindings.putMember("methodContext", methodContext);
		            jsBindings.putMember("JAVA_CTX", new JavaCtx());
		            
		            context.eval("js", script);
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