package org.meveo.admin.action.admin.custom;

import java.util.List;

import javax.annotation.PostConstruct;
import javax.faces.view.ViewScoped;
import javax.inject.Named;

import org.meveo.admin.exception.BusinessException;
import org.meveo.elresolver.ELException;
import org.meveo.model.customEntities.CustomEntityCategory;
import org.meveo.service.custom.CustomEntityCategoryService;
import org.primefaces.model.TreeNode;

@Named
@ViewScoped
public class CustomEntityCategoryBean extends BackingCustomBean<CustomEntityCategory> {

	private static final long serialVersionUID = -8940088434700385379L;

	private List<CustomEntityCategory> customEntityCategories;

	public CustomEntityCategoryBean() {
		super(CustomEntityCategory.class);
		entityClass = CustomEntityCategory.class;
	}

	@Override
	public String saveOrUpdate(boolean killConversation) throws BusinessException, ELException {
		super.saveOrUpdate(killConversation);
		return getListViewName();
	}

	@PostConstruct
	public void init() {
		customEntityCategories = customEntityCategoryService.list();
	}

	@Override
	protected CustomEntityCategoryService getPersistenceService() {
		return customEntityCategoryService;
	}

	@Override
	public void refreshFields() {
		// TODO Auto-generated method stub
	}

	@Override
	public void newTab() {
		// TODO Auto-generated method stub
	}

	@Override
	public void newFieldGroup(TreeNode parentNode) {
		// TODO Auto-generated method stub
	}

	@Override
	public void saveUpdateFieldGrouping() {
		// TODO Auto-generated method stub
	}

	@Override
	public void cancelFieldGrouping() {
		// TODO Auto-generated method stub
	}

	public List<CustomEntityCategory> getCustomEntityCategories() {
		return customEntityCategories;
	}
	
}
