package org.meveo.admin.action.catalog;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.action.BaseBean;
import org.meveo.admin.action.admin.ViewBean;
import org.meveo.model.scripts.Function;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.script.ConcreteFunctionService;

@Named
@ViewScoped
@ViewBean
public class FunctionBean extends BaseBean<Function>{
	
	private static final long serialVersionUID = -460008353823901229L;
	
    @Inject
    private ConcreteFunctionService concreteFunctionService;
    
	public FunctionBean() {
		super(Function.class);
	}
	
	@Override
	protected IPersistenceService<Function> getPersistenceService() {
		return concreteFunctionService;
	}
	
}
