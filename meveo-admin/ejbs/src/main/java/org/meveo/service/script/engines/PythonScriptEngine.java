/**
 * 
 */
package org.meveo.service.script.engines;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.meveo.admin.exception.BusinessException;
import org.meveo.service.script.ScriptInterface;
import org.python.core.PyCode;
import org.python.core.PyObject;
import org.python.util.PythonInterpreter;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
public class PythonScriptEngine implements ScriptInterface {
	
    private String script;
    private AtomicBoolean isInterrupted = new AtomicBoolean(false);
    public Map<String, Object> methodContext;
    private String code;
	
    public PythonScriptEngine(String script, String code) {
        this.script = script;
        this.code = code;
        

        
    }

	@Override
	public void init(Map<String, Object> methodContext) throws BusinessException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void execute(Map<String, Object> methodContext) throws BusinessException {
        try (PythonInterpreter interp = new PythonInterpreter()) {
        	System.setProperty("python.import.site", "false");
        	methodContext.forEach(interp::set);
        	interp.set("methodContext", methodContext);
        	interp.set("requireService", JavaBindings.CDI_SUPPLIER);
        	interp.set("requireFunction", JavaBindings.FUNCTION_SUPPLIER);
        	interp.set("log", LoggerFactory.getLogger(code));
        	interp.set("require", null); //TODO: python module imports
        	
        	interp.eval(script);
        }
        
	}

	@Override
	public void finalize(Map<String, Object> methodContext) throws BusinessException {
		// TODO Auto-generated method stub
		
	}
	
	

}
