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

import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.FunctionServiceLiteral;
import org.meveo.model.scripts.test.ExpectedOutput;

import javax.ejb.Lock;
import javax.ejb.LockType;
import javax.ejb.Singleton;
import javax.enterprise.inject.Any;
import javax.enterprise.inject.Default;
import javax.enterprise.inject.Instance;
import javax.inject.Inject;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author clement.bareth
 */
@Default
@Singleton
@Lock(LockType.READ)
public class ConcreteFunctionService extends FunctionService<Function, ScriptInterface> {
	
	@Inject @Any
    private Instance<FunctionService<?, ?>> fnServiceInst;

	@Override
	protected void afterUpdateOrCreate(Function executable) {}

	@Override
	protected void validate(Function executable) {}

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
	
	@Override
	public ScriptInterface getExecutionEngine(Function function, Map<String, Object> context) {
		FunctionService<?, ScriptInterface> functionService = getFunctionService(function);
		return functionService.getExecutionEngine(function.getCode(), context);
	}

	@SuppressWarnings("unchecked")
	public FunctionService<?, ScriptInterface> getFunctionService(String executableCode) {
		final Function function = findByCode(executableCode);
		String functionType = function.getFunctionType();
		FunctionServiceLiteral literal = new FunctionServiceLiteral(functionType);
		return (FunctionService<?, ScriptInterface>) fnServiceInst.select(literal).get();
	}
	
	@SuppressWarnings("unchecked")
	public FunctionService<?, ScriptInterface> getFunctionService(Function function) {
		String functionType = function.getFunctionType();
		FunctionServiceLiteral literal = new FunctionServiceLiteral(functionType);
		return (FunctionService<?, ScriptInterface>) fnServiceInst.select(literal).get();
	}

	@Override
	public List<ExpectedOutput> compareResults(List<ExpectedOutput> expectedOutputs, Map<String, Object> results) {
		return null;
	}

}
