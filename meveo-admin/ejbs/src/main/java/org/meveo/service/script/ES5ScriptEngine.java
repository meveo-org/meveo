package org.meveo.service.script;

import java.util.Map;

import org.meveo.admin.exception.BusinessException;

import com.eclipsesource.v8.V8;

/**
 * Created by Hien Bach on 4/5/2019.
 */
public class ES5ScriptEngine implements ScriptInterface {
    private String script;

    @Override
    public void init(Map<String, Object> methodContext) throws BusinessException {

    }

    @Override
    public void execute(Map<String, Object> methodContext) throws BusinessException {
    	// Initialize runtime
        V8 runtime = V8.createV8Runtime();
        methodContext.forEach((k, v) -> {
        	if (v instanceof String) {
        		runtime.add(k, (String) v);
        	} else if (v instanceof Integer) {
        		runtime.add(k, (int) v);
        	} else if (v instanceof Double) {
        		runtime.add(k, (double) v);
        	} else if (v instanceof Boolean) {
        		runtime.add(k, (boolean) v);
        	}
        });
        
        // Execute and retrieve results
        runtime.executeScript(script);
        final String[] keys = runtime.getKeys();
        for (String key : keys) {
            methodContext.put(key, runtime.get(key));
        }
    }

    @Override
    public void finalize(Map<String, Object> methodContext) throws BusinessException {

    }

    @Override
    public Map<String, Object> cancel() {
        return null;
    }

    public ES5ScriptEngine(String script) {
        this.script = script;
    }
}