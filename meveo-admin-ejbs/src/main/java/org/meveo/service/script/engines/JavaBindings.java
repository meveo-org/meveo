/**
 * 
 */
package org.meveo.service.script.engines;

import java.util.function.Function;

import javax.enterprise.inject.spi.CDI;

import org.meveo.service.script.ScriptInstanceService;
import org.meveo.service.script.ScriptInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class JavaBindings {
	
	public static final Function<String, ?> CDI_SUPPLIER;
	public static final Function<String, ScriptInterface> FUNCTION_SUPPLIER;
	
    private static Logger LOG = LoggerFactory.getLogger(JavaBindings.class);
	
	static {
		CDI_SUPPLIER = (name) -> {
        	try {
        		Class<?> beanClass = Class.forName(name);
            	return CDI.current().select(beanClass).get();
        	} catch (Exception e) {
        		LOG.error("Service not found", e);
        		return null;
        	}
        };
        
        FUNCTION_SUPPLIER = (name) -> {
        	ScriptInstanceService scriptService = CDI.current().select(ScriptInstanceService.class).get();
        	return scriptService.getExecutionEngine(name, null);
        };
	}

}
