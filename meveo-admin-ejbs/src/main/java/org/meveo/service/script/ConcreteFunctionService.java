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

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import javax.transaction.Transactional;

import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.exception.ElementNotFoundException;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.FunctionIO;
import org.meveo.model.scripts.FunctionServiceLiteral;
import org.meveo.model.scripts.test.ExpectedOutput;

/**
 * @author clement.bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.9.0
 */
@Default
public class ConcreteFunctionService extends FunctionService<Function, ScriptInterface> implements Serializable {
	
	private static final long serialVersionUID = 6805848221588048249L;
	
	@Inject @Any
    private Instance<FunctionService<?, ?>> fnServiceInst;
	
	@Override
	public void afterUpdateOrCreate(Function executable) {}

	@Override
	protected void validate(Function executable) {}

	@Override
	protected String getCode(Function executable) {
		return executable.getCode();
	}
	
	/**
	 * @return the codes of all the {@link Function} in database
	 */
	public List<String> getCodes() {
		return this.getEntityManager().createQuery("SELECT code FROM Function", String.class)
			.getResultList();
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FunctionIO> getInputs(Function function) throws BusinessException {
		FunctionService<Function, ScriptInterface> functionService = (FunctionService<Function, ScriptInterface>) getFunctionService(function.getCode());
		return functionService.getInputs(function);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<FunctionIO> getOutputs(Function function) throws BusinessException {
		FunctionService<Function, ScriptInterface> functionService = (FunctionService<Function, ScriptInterface>) getFunctionService(function.getCode());
		return functionService.getOutputs(function);
	}

	/**
	 * Retrieve function class from its code and call corresponding service. When knowing type in advance, prefer use corresponding service.
	 */
	@Override
	public ScriptInterface getExecutionEngine(String executableCode, Map<String, Object> context) throws BusinessException{
		FunctionService<?, ScriptInterface> functionService = getFunctionService(executableCode);
		return functionService.getExecutionEngine(executableCode, context);
	}
	
	@Override
	public ScriptInterface getExecutionEngine(Function function, Map<String, Object> context) throws BusinessException {
		FunctionService<?, ScriptInterface> functionService = getFunctionService(function);
		return functionService.getExecutionEngine(function.getCode(), context);
	}
	
	@Override
	public Map<String, Object> execute(String code, Map<String, Object> context) throws BusinessException {
		FunctionService<?, ScriptInterface> functionService = getFunctionService(code);
		return functionService.execute(code, context);
	}

	@SuppressWarnings("unchecked")
	public FunctionService<?, ScriptInterface> getFunctionService(Long functionId) {
		
		Function function = findById(functionId);
		String functionType = function.getFunctionType();
		FunctionServiceLiteral literal = new FunctionServiceLiteral(functionType);
		return (FunctionService<?, ScriptInterface>) fnServiceInst.select(literal).get();
	}

	@SuppressWarnings("unchecked")
	public <T extends Function> FunctionService<T, ScriptInterface> getFunctionService(String executableCode) throws ElementNotFoundException {
		
		final Function function = findByCode(executableCode);
        if(function == null) {
    		throw new ElementNotFoundException( executableCode, "Function");
    	}  
		String functionType = function.getFunctionType();
		FunctionServiceLiteral literal = new FunctionServiceLiteral(functionType);
		return (FunctionService<T, ScriptInterface>) fnServiceInst.select(literal).get();
	}
	
	@SuppressWarnings("unchecked")
	public <T extends Function> FunctionService<T, ScriptInterface> getFunctionService(Function function) {
		String functionType = function.getFunctionType();
		FunctionServiceLiteral literal = new FunctionServiceLiteral(functionType);
		return (FunctionService<T, ScriptInterface>) fnServiceInst.select(literal).get();
	}

	@Override
	public List<ExpectedOutput> compareResults(List<ExpectedOutput> expectedOutputs, Map<String, Object> results) {
		return null;
	}
	
	@Transactional
	@Override
	public long count() {
		return super.count();
	}
	
	@Transactional
	@Override
	public long count(PaginationConfiguration config) {
		return super.count(config);
	}
	
	@Transactional
	@Override
	public List<Function> list(PaginationConfiguration config) {
		return super.list(config);
	}

}
