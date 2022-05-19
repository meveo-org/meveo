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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;

import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementNotFoundException;
import org.meveo.admin.exception.InvalidPermissionException;
import org.meveo.admin.exception.InvalidScriptException;
import org.meveo.admin.exception.ScriptExecutionException;
import org.meveo.cache.CacheKeyStr;
import org.meveo.commons.utils.StringUtils;
import org.meveo.event.monitoring.ClusterEventDto.CrudActionEnum;
import org.meveo.event.monitoring.ClusterEventPublisher;
import org.meveo.model.IEntity;
import org.meveo.model.jobs.JobCategoryEnum;
import org.meveo.model.jobs.JobInstance;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.FunctionIO;
import org.meveo.model.scripts.FunctionUtils;
import org.meveo.model.scripts.Sample;
import org.meveo.model.scripts.test.ExpectedOutput;
import org.meveo.model.storage.Repository;
import org.meveo.service.base.BusinessService;
import org.meveo.service.job.JobInstanceService;

/**
 * @param <T> Type of function (service, script ...)
 * @param <E> Type of engine
 * @author clement.bareth
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.11
 */
public abstract class FunctionService<T extends Function, E extends ScriptInterface>
        extends BusinessService<T> {

    private static final Map<CacheKeyStr, List<String>> ALL_LOGS = new ConcurrentHashMap<>();

    public static final String FUNCTION_TEST_JOB = "FunctionTestJob";

    @Inject
    private ClusterEventPublisher clusterEventPublisher;

    @Inject
    private JobInstanceService jobInstanceService;
    
    /**
     * Parse parameters encoded in URL like style param=value&amp;param=value.
     *
     * @param encodedParameters Parameters encoded in URL like style param=value&amp;param=value
     * @return A map of parameter keys and values
     */
    public static Map<String, Object> parseParameters(String encodedParameters) {
        Map<String, Object> parameters = new HashMap<>();
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

    private void publish(T executable, CrudActionEnum action) throws BusinessException {
    	
        clusterEventPublisher.publishEvent(executable, action);
    }

    /**
     * Clear from logs the execution engine corresponding to the given code
     *
     * @param code Code of the execution engine
     */
    protected void clear(String code) {
        ALL_LOGS.remove(new CacheKeyStr(currentUser.getProviderCode(), code));
    }

    /**
     * Action to execute after the creation or the update of the executable
     *
     * @param executable Function that was just created or updated
     */
    @Override
    public void afterUpdateOrCreate(T executable) throws BusinessException {
    	super.afterUpdateOrCreate(executable);
    }

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

    /**
     * Retrieve the function's inputs
     * 
     * @param function the function
     * @return inputs of the function
     */
    public List<FunctionIO> getInputs(T function) throws BusinessException {
    	return function.getInputs();
    }
    
    /**
     * Retrieve the function's outputs
     * 
     * @param function the function
     * @return outputs of the function
     */
    public List<FunctionIO> getOutputs(T function) throws BusinessException {
    	return function.getOutputs();
    }
    
    @Override
    public void create(T executable) throws BusinessException {
        beforeUpdateOrCreate(executable);
    	validateAndSetCode(executable);
        super.create(executable);
        publish(executable, CrudActionEnum.create);
    }

    @Override
    public T update(T executable) throws BusinessException {
        
        beforeUpdateOrCreate(executable);
    	validateAndSetCode(executable);
        executable = super.update(executable);        
        publish(executable, CrudActionEnum.update);
        
        return executable;
    }
    

    @Override
	public T updateNoMerge(T entity) throws BusinessException {
    	validateAndSetCode(entity);
        beforeUpdateOrCreate(entity);
        super.updateNoMerge(entity);       
        publish(entity, CrudActionEnum.update);
        return entity;
	}

	@Override
    public void remove(T executable) throws BusinessException {
        // First remove test jobs
        final List<JobInstance> jobsToRemove = jobInstanceService.findJobsByTypeAndParameters(FUNCTION_TEST_JOB, executable.getCode(), JobCategoryEnum.TEST);
        for(JobInstance jobToRemove : jobsToRemove){
            jobInstanceService.remove(jobToRemove);
        }

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
	public void postDisable(T entity) {
		super.postDisable(entity);
        clusterEventPublisher.publishEvent(entity, CrudActionEnum.disable);
	}

	/**
     * Retrieve the execution engine corresponding to the given code
     *
     * @param executableCode Code of the execution engine to retrieve
     * @return An instance of an execution engine for the executable with code
     */
    public abstract E getExecutionEngine(String executableCode, Map<String, Object> context) throws BusinessException;
    
    public abstract E getExecutionEngine(T function, Map<String, Object> context) throws BusinessException;

    /**
     * Add a log line for a script
     *
     * @param message    message to be displayed
     * @param scriptCode code of script.
     */
    public void addLog(String message, String scriptCode) {
        if (!ALL_LOGS.containsKey(new CacheKeyStr(currentUser.getProviderCode(), scriptCode))) {
            ALL_LOGS.put(new CacheKeyStr(currentUser.getProviderCode(), scriptCode),
                    new ArrayList<>());
        }
        ALL_LOGS.get(new CacheKeyStr(currentUser.getProviderCode(), scriptCode)).add(message);
    }

    /**
     * Get logs for script
     *
     * @param scriptCode code of script
     * @return list logs.
     */
    public List<String> getLogs(String scriptCode) {

        if (!ALL_LOGS.containsKey(new CacheKeyStr(currentUser.getProviderCode(), scriptCode))) {
            return new ArrayList<>();
        }
        return ALL_LOGS.get(new CacheKeyStr(currentUser.getProviderCode(), scriptCode));
    }

    /**
     * Clear all logs for a script.
     *
     * @param scriptCode script's code
     */
    public void clearLogs(String scriptCode) {
        if (ALL_LOGS.containsKey(new CacheKeyStr(currentUser.getProviderCode(), scriptCode))) {
            ALL_LOGS.get(new CacheKeyStr(currentUser.getProviderCode(), scriptCode)).clear();
        }
    }
    
    public Map<String, Object> execute(E engine, Map<String, Object> context) throws BusinessException {
    	return execute(engine, context, true);
    }
    
    /**
     * Execute an engine
     *
     * @param engine  Compiled script class
     * @param context Method context
     * @param withInitFinalize whether to execute init and finalize methods
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws BusinessException Any execution exception
     */
    public Map<String, Object> execute(E engine, Map<String, Object> context, boolean withInitFinalize) throws BusinessException {
        if (context == null) {
            context = new HashMap<>();
        }
        try {
			if (withInitFinalize) engine.init(context);
		} catch (Throwable e) {
			throw new ScriptExecutionException(engine.getClass().getName(), "init", e);
		}
        
        executeEngine(engine, context);

        try {
			if (withInitFinalize) engine.finalize(context);
		} catch (Throwable e) {
			throw new ScriptExecutionException(engine.getClass().getName(), "finalize", e);
		}

        try {
        	return buildResultMap(engine, context);
		} catch (Throwable e) {
			throw new ScriptExecutionException(engine.getClass().getName(), "buildResultMap", e);
		}
        
    }
    
    protected void executeEngine(E engine, Map<String, Object> context) throws ScriptExecutionException{
        try {
			engine.execute(context);
		} catch (Throwable e) {
			throw new ScriptExecutionException(engine.getClass().getName(), "execute", e);
		}
    }
    
    protected Map<String, Object> buildResultMap(E engine, Map<String, Object> context) {
		return context;
	}

	/**
     * Execute action on an entity.
     *
     * @param entity            Entity to execute action on
     * @param repository Repository containing the entity
     * @param scriptCode        Script to execute, identified by a code
     * @param encodedParameters Additional parameters encoded in URL like style
     *                          param=value&amp;param=value
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws InvalidPermissionException Insufficient access to run the script
     * @throws ElementNotFoundException   Script not found
     * @throws BusinessException          Any execution exception
     */
    public Map<String, Object> execute(IEntity<?> entity, Repository repository, String scriptCode, String encodedParameters) throws BusinessException {
        return execute(entity, repository, scriptCode, CustomScriptService.parseParameters(encodedParameters));
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
     * @param repository Repository containing the entity
     * @param scriptCode Script to execute, identified by a code
     * @param context    Additional parameters
     * @return Context parameters. Will not be null even if "context" parameter is null.
     * @throws InvalidScriptException     Were not able to instantiate or compile a script
     * @throws ElementNotFoundException   Script not found
     * @throws InvalidPermissionException Insufficient access to run the script
     * @throws BusinessException          Any execution exception
     */
    public Map<String, Object> execute(IEntity<?> entity, Repository repository, String scriptCode, Map<String, Object> context) throws BusinessException {
        if (context == null) {
            context = new HashMap<>();
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
    @TransactionAttribute(TransactionAttributeType.NEVER)
    public Map<String, Object> execute(String code, Map<String, Object> context) throws BusinessException {
        E engine = getExecutionEngine(code, context);
        return execute(engine, context);
    }
    
    @TransactionAttribute(TransactionAttributeType.NEVER)
   	public Map<String, Object> postCommit(String code, Map<String, Object> context) throws BusinessException {

   		E engine = getExecutionEngine(code, context);

   		if (context == null) {
   			context = new HashMap<>();
   		}

   		engine.postCommit(context);

   		return buildResultMap(engine, context);
   	}

   	@TransactionAttribute(TransactionAttributeType.NEVER)
   	public Map<String, Object> postRollback(String code, Map<String, Object> context) throws BusinessException {

   		E engine = getExecutionEngine(code, context);

   		if (context == null) {
   			context = new HashMap<>();
   		}

   		engine.postRollback(context);

   		return buildResultMap(engine, context);
   	}

    public abstract List<ExpectedOutput> compareResults(List<ExpectedOutput> expectedOutputs, Map<String, Object> results);

	public List<Sample> getSamples(String functioncode) {
        T f = findByCode(functioncode);
        if (f != null) {
            return getSamples(f);
        }

        return new ArrayList<>();
    }

	public List<Sample> getSamples(T script) {
        try {
            if (script.getGenerateOutputs()) {
                for (Sample sample : script.getSamples()) {
                    if (sample.getOutputs() == null) {
                        HashMap<String, Object> copyOfInput = new HashMap<>(sample.getInputs());
                        Map<String, Object> output = execute(script.getCode(), copyOfInput);
                        // Keep only keys that were modified
                        new HashMap<>(output).forEach((s, o) -> {
                            if (sample.getInputs().get(s) == o) {
                                output.remove(s);
                            }
                        });
                        sample.setOutputs(output);
                    }
                }

                this.update(script);
            }

        } catch (Exception e) {
            log.warn("Cannot generate outputs of script {}", script.getCode(), e);
        }

        return script.getSamples();
    }
	
	/**
	 * Updates the test suite for the function with the given code
	 * 
	 * @param code      function code
	 * @param testSuite test suite content
	 */
	public void updateTestSuite(String code, String testSuite) {
		final String finalTestSuite = FunctionUtils.replaceWithCorrectCode(testSuite, code);
		final String updateQuery = "UPDATE Function SET testSuite = :testSuite "
								 + "WHERE code = :code";
		
		getEntityManager().createQuery(updateQuery)
			.setParameter("testSuite", finalTestSuite)
			.setParameter("code", code)
			.executeUpdate();	
	}
	
    public Class<ScriptInterface> compileScript(T script, boolean testCompile) {
    	return null;
	}
    
    public void setParameters(T script, E scriptInstance, Map<String, Object> context) {
    	// NOOP
    }
}
