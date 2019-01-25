/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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

import java.util.HashMap;
import java.util.Map;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementNotFoundException;
import org.meveo.admin.exception.InvalidPermissionException;
import org.meveo.admin.exception.InvalidScriptException;
import org.meveo.model.IEntity;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.FunctionServiceLiteral;

/**
 * @param <Function> Type of function (service, script ...)
 * @param <E> Type of engine
 * @author clement.bareth
 */
@Default
@Singleton
@Lock(LockType.READ)
public class ConcreteFunctionService  extends FunctionService<Function, ScriptInterface> {
	
	private static Map<Class<? extends Function>, FunctionService<?,? extends ScriptInterface>> servicesMapping = new HashMap<>();
	private static Map<String, Class<? extends Function>> functionsClassesCache = new HashMap<>();

    @Inject @Any
    private Instance<FunctionService<?, ?>> fnServiceInst;
    
    /**
     * Execute an engine
     *
     * @param engine  Compiled script class
     * @param context Method context
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws BusinessException Any execution exception
     */
    protected Map<String, Object> execute(ScriptInterface engine, Map<String, Object> context)
            throws BusinessException {
        if (context == null) {
            context = new HashMap<String, Object>();
        }
        engine.init(context);
        engine.execute(context);
        engine.finalize(context);
        return buildResultMap(engine, context);
    }

    protected Map<String, Object> buildResultMap(ScriptInterface engine, Map<String, Object> context) {
		return context;
	}

	/**
     * Execute action on an entity.
     *
     * @param entity            Entity to execute action on
     * @param scriptCode        Script to execute, identified by a code
     * @param encodedParameters Additional parameters encoded in URL like style
     *                          param=value&amp;param=value
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws InvalidPermissionException Insufficient access to run the script
     * @throws ElementNotFoundException   Script not found
     * @throws BusinessException          Any execution exception
     */
    public Map<String, Object> execute(IEntity entity, String scriptCode, String encodedParameters) throws BusinessException {
        return execute(entity, scriptCode, CustomScriptService.parseParameters(encodedParameters));
    }

    /**
     * Execute action on an entity.
     *
     * @param scriptCode        Script to execute, identified by a code
     * @param encodedParameters Additional parameters encoded in URL like style
     *                          param=value&amp;param=value
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws InvalidPermissionException Insufficient access to run the script
     * @throws ElementNotFoundException   Script not found
     * @throws BusinessException          Any execution exception
     */
    public Map<String, Object> execute(String scriptCode, String encodedParameters) throws BusinessException {
        return execute(scriptCode, CustomScriptService.parseParameters(encodedParameters));
    }

    /**
     * Execute action on an entity.
     *
     * @param entity     Entity to execute action on
     * @param scriptCode Script to execute, identified by a code
     * @param context    Additional parameters
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws InvalidScriptException     Were not able to instantiate or compile a script
     * @throws ElementNotFoundException   Script not found
     * @throws InvalidPermissionException Insufficient access to run the script
     * @throws BusinessException          Any execution exception
     */
    public Map<String, Object> execute(IEntity entity, String scriptCode, Map<String, Object> context) throws BusinessException {
        if (context == null) {
            context = new HashMap<String, Object>();
        }
        context.put(Script.CONTEXT_ENTITY, entity);
        return execute(scriptCode, context);
    }

    /**
     * Execute action on an entity.
     *
     * @param code    Script to execute, identified by a code
     * @param context Method context
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws InvalidScriptException     Were not able to instantiate or compile a script
     * @throws ElementNotFoundException   Script not found
     * @throws InvalidPermissionException Insufficient access to run the script
     * @throws BusinessException          Any execution exception
     */
    public Map<String, Object> execute(String code, Map<String, Object> context) throws BusinessException {
    	ScriptInterface engine = getExecutionEngine(code, context);
        return execute(engine, context);
    }

	@Override
	protected void afterUpdateOrCreate(Function executable) {}

	@Override
	protected void validate(Function executable) throws BusinessException {}

	@Override
	protected String getCode(Function executable) {
		return executable.getCode();
	}
	
	/**
	 * Retrieve function class from its code and call corresponding service. When knowing type in advance, prefer use corresponding service.
	 */
	@Override
	public ScriptInterface getExecutionEngine(String executableCode, Map<String, Object> context) {
		FunctionService<?, ScriptInterface> functionService = getFunctionService(executableCode);
		return functionService.getExecutionEngine(executableCode, context);
	}

	@SuppressWarnings("unchecked")
	private FunctionService<?, ScriptInterface> getFunctionService(String executableCode) {
		Class<? extends Function> functionClass = functionsClassesCache.computeIfAbsent(executableCode, code -> this.findByCode(code).getClass() );
 		FunctionService<?,?> functionService = servicesMapping.computeIfAbsent(functionClass, clazz -> {
			FunctionServiceLiteral literal = new FunctionServiceLiteral(clazz);
			return fnServiceInst.select(literal).get();
		});
		return (FunctionService<?, ScriptInterface>) functionService;
	}
    
}
