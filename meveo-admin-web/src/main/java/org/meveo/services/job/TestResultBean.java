/**
 * 
 */
package org.meveo.services.job;

import java.util.List;

import javax.ejb.EJB;
import javax.inject.Inject;
import javax.inject.Named;
import javax.persistence.NoResultException;

import org.meveo.admin.action.BaseBean;
import org.meveo.admin.util.pagination.PaginationConfiguration;
import org.meveo.model.scripts.FunctionCategory;
import org.meveo.model.tests.TestResultDto;
import org.meveo.service.base.local.IPersistenceService;
import org.meveo.service.script.DefaultFunctionService;
import org.meveo.service.script.test.TestResultService;
import org.omnifaces.cdi.Param;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 * 
 * @author clement.bareth
 * @since 6.10.0 
 * @version 6.10.0
 */
@Named
public class TestResultBean extends BaseBean<TestResultDto> {

	private static final long serialVersionUID = 3229574090795083013L;

	@Inject
	private transient TestResultService trs;
	
	@EJB
	private DefaultFunctionService dfs;
	
	@Inject @Param(name = "category")
	private String categoryCode;
	
	private FunctionCategory category;
	
	public TestResultBean() {
		super(TestResultDto.class);
		
		this.getFilters().put("active", true);
		this.getFilters().put("history", "1");
	}

	public String getCategoryDescription() {
		if(categoryCode == null) {
			category = null;
		}
		
		if(categoryCode != null) {
			if(category == null) {
				try {
					category = dfs.findCategory(categoryCode);
				} catch (NoResultException e) {
					category = null;
				}
			}
		}
		
		if(category != null) {
			return category.getDescription();
		}
		
		return null;
	}
	
	public List<String> getCategoriesCodes() {
		return dfs.getCategoriesCodes();
	}
	
	@Override
	public void init() {
		super.init();
	}

	@Override
	public void search() {
		this.getFilters().put("category", categoryCode);
		super.search();
	}
	
	@Override
	public LazyDataModel<TestResultDto> getLazyDataModel() {
		this.getFilters().put("category", categoryCode);
		
		LazyDataModel<TestResultDto> lazyDataModel = super.getLazyDataModel();
		return lazyDataModel;
	}

	public String getCategory() {
		return categoryCode;
	}

	public void setCategory(String category) {
		this.categoryCode = category;
	}

	@Override
	protected IPersistenceService<TestResultDto> getPersistenceService() {
		return trs;
	}

	@Override
	protected String getDefaultSort() {
		return "nbKo";
	}

	@Override
	protected SortOrder getDefaultSortOrder() {
		return SortOrder.DESCENDING;
	}

}