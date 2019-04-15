package org.meveo.service.script;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import org.meveo.admin.exception.BusinessException;
import org.meveo.service.script.ScriptInterface;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

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
        V8 runtime = V8.createV8Runtime();
        Object o = runtime.executeScript(script);
        if(o instanceof V8Object){
            V8Object v = (V8Object) o;
            final String[] keys = v.getKeys();
            for (String key : keys) {
                methodContext.put(key, v.get(key));
            }
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
