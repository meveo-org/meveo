/**
 * 
 */
package org.meveo.services.job;

import javax.inject.Inject;
import javax.inject.Named;

import org.meveo.admin.action.BaseBean;
import org.meveo.model.tests.CategoryTest;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.script.test.CategoryTestService;

/**
 * 
 * @author clement.bareth
 * @since 6.10.0
 * @version 6.10.0
 */
@Named
public class CategoryTestBean extends BaseBean<CategoryTest> {
	
	private static final long serialVersionUID = 8178024503938807904L;
	
	@Inject
	private transient CategoryTestService cts;

	public CategoryTestBean() {
		super(CategoryTest.class);
	}
	

	@Override
	protected IPersistenceService<CategoryTest> getPersistenceService() {
		return cts;
	}

	@Override
	public String getEditViewName() {
		return "jobInstanceDetail";
	}

	@Override
	public CategoryTest getInstance() throws InstantiationException, IllegalAccessException {
		return new CategoryTest(null, 0, 0);
	}
		

}