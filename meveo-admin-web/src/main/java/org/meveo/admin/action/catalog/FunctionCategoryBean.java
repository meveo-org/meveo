/**
 * 
 */
package org.meveo.admin.action.catalog;

import javax.faces.view.ViewScoped;
import javax.inject.Inject;
import javax.inject.Named;
import javax.transaction.Transactional;

import org.meveo.admin.action.admin.custom.BackingCustomBean;
import org.meveo.admin.exception.BusinessException;
import org.meveo.admin.web.interceptor.ActionMethod;
import org.meveo.elresolver.ELException;
import org.meveo.model.scripts.FunctionCategory;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.script.FunctionCategoryService;
import org.primefaces.model.TreeNode;

/**
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
@Named
@ViewScoped
public class FunctionCategoryBean extends BackingCustomBean<FunctionCategory> {
	
	private static final long serialVersionUID = 310189328131223269L;
	
	@Inject
	private transient FunctionCategoryService fnCatService;

	/**
	 * Instantiates a new FunctionCategoryBean
	 */
	protected FunctionCategoryBean() {
		super(FunctionCategory.class);
	}

	@Override
	public void refreshFields() {
	}

	@Override
	public void newTab() {
	}

	@Override
	public void newFieldGroup(TreeNode parentNode) {
	}

	@Override
	public void saveUpdateFieldGrouping() {
	}

	@Override
	public void cancelFieldGrouping() {
	}

	@Override
	protected IPersistenceService<FunctionCategory> getPersistenceService() {
		return fnCatService;
	}

	@Override
	protected String getListViewName() {
		return "functionCategories";
	}

	@Override
	public String back() {
		return getListViewName();
	}
	
	@Override
	@ActionMethod
	public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {
		super.saveOrUpdate(killConversation);
		return getListViewName();
	}
	

}
