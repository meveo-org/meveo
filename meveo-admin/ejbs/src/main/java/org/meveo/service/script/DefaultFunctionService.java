/**
 * 
 */
package org.meveo.service.script;

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.FunctionServiceFor;
import org.meveo.model.scripts.test.ExpectedOutput;

/**
 * Default implementation for FunctionService
 * 
 * @author clement.bareth
 * @since 6.9.0
 * @version 6.9.0
 */
@FunctionServiceFor(Function.UNKNOWN)
@Stateless
public class DefaultFunctionService extends FunctionService<Function, ScriptInterface> {

	@Override
	protected void afterUpdateOrCreate(Function executable) {
	}

	@Override
	protected void validate(Function executable) throws BusinessException {
	}

	@Override
	protected String getCode(Function executable) {
		return executable.getCode();
	}

	@Override
	public ScriptInterface getExecutionEngine(String executableCode, Map<String, Object> context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public ScriptInterface getExecutionEngine(Function function, Map<String, Object> context) {
		throw new UnsupportedOperationException();
	}

	@Override
	public List<ExpectedOutput> compareResults(List<ExpectedOutput> expectedOutputs, Map<String, Object> results) {
		return null;
	}

}
