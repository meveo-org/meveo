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

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementNotFoundException;
import org.meveo.admin.exception.InvalidPermissionException;
import org.meveo.admin.exception.InvalidScriptException;
import org.meveo.cache.CacheKeyStr;
import org.meveo.commons.utils.StringUtils;
import org.meveo.event.monitoring.ClusterEventDto.CrudActionEnum;
import org.meveo.event.monitoring.ClusterEventPublisher;
import org.meveo.model.IEntity;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.test.ExpectedOutput;
import org.meveo.service.base.BusinessService;

import javax.inject.Inject;
import java.util.*;

/**
 * @param <T> Type of function (service, script ...)
 * @param <E> Type of engine
 * @author clement.bareth
 */
public abstract class FunctionService<T extends Function, E extends ScriptInterface>
        extends BusinessService<T> {

    @Inject
    private ClusterEventPublisher clusterEventPublisher;

    private Map<CacheKeyStr, List<String>> allLogs = new HashMap<>();

    private Map<CacheKeyStr, E> cachedExecutionEngines = new HashMap<>();

    /**
     * Parse parameters encoded in URL like style param=value&amp;param=value.
     *
     * @param encodedParameters Parameters encoded in URL like style param=value&amp;param=value
     * @return A map of parameter keys and values
     */
    public static Map<String, Object> parseParameters(String encodedParameters) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        if (!StringUtils.isBlank(encodedParameters)) {
            StringTokenizer tokenizer = new StringTokenizer(encodedParameters, "&");
            while (tokenizer.hasMoreElements()) {
                String paramValue = tokenizer.nextToken();
                String[] paramValueSplit = paramValue.split("=");
                if (paramValueSplit.length == 2) {
                    parameters.put(paramValueSplit[0], paramValueSplit[1]);
                } else {
                    parameters.put(paramValueSplit[0], null);
                }
            }

        }
        return parameters;
    }

    private void validateAndSetCode(T executable) throws BusinessException {
        validate(executable);
        String code = getCode(executable);
        executable.setCode(code);
    }

    private void publish(T executable, CrudActionEnum action) {
        afterUpdateOrCreate(executable);
        clusterEventPublisher.publishEvent(executable, action);
    }

    /**
     * Clear from cache and logs the execution engine corresponding to the given code
     *
     * @param code Code of the execution engine
     */
    protected void clear(String code) {
        cachedExecutionEngines.remove(new CacheKeyStr(currentUser.getProviderCode(), code));
        allLogs.remove(new CacheKeyStr(currentUser.getProviderCode(), code));
    }

    /**
     * Action to execute after the creation or the update of the executable
     *
     * @param executable Function that was just created or updated
     */
    protected abstract void afterUpdateOrCreate(T executable);

    /**
     * Validates the executable
     *
     * @param executable Function to validate
     * @throws BusinessException if the executable is not valid
     */
    protected abstract void validate(T executable) throws BusinessException;

    /**
     * Build the code for the given executable
     *
     * @param executable Function to get code from
     * @return The code of the executable
     */
    protected abstract String getCode(T executable);

    @Override
    public void create(T executable) throws BusinessException {
        validateAndSetCode(executable);
        super.create(executable);
        publish(executable, CrudActionEnum.create);
    }

    @Override
    public T update(T executable) throws BusinessException {
        validateAndSetCode(executable);
        executable = super.update(executable);
        publish(executable, CrudActionEnum.update);
        return executable;
    }

    @Override
    public void remove(T executable) throws BusinessException {
        super.remove(executable);
        clusterEventPublisher.publishEvent(executable, CrudActionEnum.remove);
    }

    @Override
    public T enable(T executable) throws BusinessException {
        executable = super.enable(executable);
        clusterEventPublisher.publishEvent(executable, CrudActionEnum.enable);
        return executable;
    }

    @Override
    public T disable(T executable) throws BusinessException {
        executable = super.disable(executable);
        clusterEventPublisher.publishEvent(executable, CrudActionEnum.disable);
        return executable;
    }

    /**
     * Retrieve the execution engine corresponding to the given code
     *
     * @param executableCode Code of the execution engine to retrieve
     * @return An instance of an execution engine for the executable with code
     */
    public abstract E getExecutionEngine(String executableCode, Map<String, Object> context);
    
    public abstract E getExecutionEngine(T function, Map<String, Object> context);

    /**
     * Add a log line for a script
     *
     * @param message    message to be displayed
     * @param scriptCode code of script.
     */
    public void addLog(String message, String scriptCode) {
        if (!allLogs.containsKey(new CacheKeyStr(currentUser.getProviderCode(), scriptCode))) {
            allLogs.put(new CacheKeyStr(currentUser.getProviderCode(), scriptCode),
                    new ArrayList<String>());
        }
        allLogs.get(new CacheKeyStr(currentUser.getProviderCode(), scriptCode)).add(message);
    }

    /**
     * Get logs for script
     *
     * @param scriptCode code of script
     * @return list logs.
     */
    public List<String> getLogs(String scriptCode) {

        if (!allLogs.containsKey(new CacheKeyStr(currentUser.getProviderCode(), scriptCode))) {
            return new ArrayList<String>();
        }
        return allLogs.get(new CacheKeyStr(currentUser.getProviderCode(), scriptCode));
    }

    /**
     * Clear all logs for a script.
     *
     * @param scriptCode script's code
     */
    public void clearLogs(String scriptCode) {
        if (allLogs.containsKey(new CacheKeyStr(currentUser.getProviderCode(), scriptCode))) {
            allLogs.get(new CacheKeyStr(currentUser.getProviderCode(), scriptCode)).clear();
        }
    }
    
    /**
     * Execute an engine
     *
     * @param engine  Compiled script class
     * @param context Method context
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws BusinessException Any execution exception
     */
    public Map<String, Object> execute(E engine, Map<String, Object> context) throws BusinessException {
        if (context == null) {
            context = new HashMap<String, Object>();
        }
        engine.init(context);
        engine.execute(context);
        engine.finalize(context);
        return buildResultMap(engine, context);
    }

    protected Map<String, Object> buildResultMap(E engine, Map<String, Object> context) {
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
        E engine = getExecutionEngine(code, context);
        return execute(engine, context);
    }

    public abstract List<ExpectedOutput> compareResults(List<ExpectedOutput> expectedOutputs, Map<String, Object> results);
    
}
