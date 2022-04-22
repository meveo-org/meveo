/**
 * 
 */
package org.meveo.service.script;

import java.util.List;
import java.util.Map;

import javax.ejb.Stateless;

import org.meveo.admin.exception.BusinessException;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.FunctionCategory;
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
		
	public FunctionCategory findCategory(String code) {
		return getEntityManager().createQuery("FROM FunctionCategory WHERE code = :code", FunctionCategory.class)
				.setParameter("code", code)
				.getSingleResult();
	}
	
	public List<String> getCategoriesCodes() {
		return getEntityManager().createQuery("SELECT DISTINCT(code) FROM FunctionCategory", String.class)
				.getResultList();
	}

	@Override
	public void afterUpdateOrCreate(Function executable) {
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
