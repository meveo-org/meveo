package org.meveo.service.script;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import java.util.function.Supplier;

import javax.enterprise.inject.spi.CDI;

import org.graalvm.polyglot.Context;
import org.graalvm.polyglot.PolyglotAccess;
import org.graalvm.polyglot.Value;
import org.meveo.admin.exception.BusinessException;
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
	            
	            Function<String, ?> cdiSupplier = (name) -> {
	            	try {
	            		Class<?> beanClass = Class.forName(name);
		            	return CDI.current().select(beanClass).get();
	            	} catch (Exception e) {
	            		LOG.error("Service not found", e);
	            		return null;
	            	}
	            };
	            jsBindings.putMember("requireService", cdiSupplier);
	            
	            Function<String, ScriptInterface> functionSupplier = (name) -> {
	            	ScriptInstanceService scriptService = CDI.current().select(ScriptInstanceService.class).get();
	            	return scriptService.getExecutionEngine(name, methodContext);
	            };
	            jsBindings.putMember("requireFunction", functionSupplier);
	            
	            Function<String, Value> npmRequire = (name) -> {
	            	//TODO: load the corresponding library
	            	return context.eval("js", null);
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