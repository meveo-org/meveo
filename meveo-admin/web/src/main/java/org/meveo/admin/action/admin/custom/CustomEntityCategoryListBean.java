package org.meveo.admin.action.admin.custom;

import java.util.List;

import javax.enterprise.context.ConversationScoped;
import javax.inject.Named;

import org.meveo.model.customEntities.CustomEntityCategory;

/**
 * List bean controller for {@linkplain CustomEntityCategory}.
 * 
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.9.0
 */
@Named
@ConversationScoped
public class CustomEntityCategoryListBean extends CustomEntityCategoryBean {

	private static final long serialVersionUID = 8964268530669089018L;

	@Override
	public List<CustomEntityCategory> getCustomEntityCategories() {
		List<CustomEntityCategory> customEntityCategories = super.getCustomEntityCategories();
		if (!customEntityCategories.isEmpty()) {
			customEntityCategories.sort((c1, c2) -> {
				return c1.getCode().compareToIgnoreCase(c2.getCode());
			});
		}
		return customEntityCategories;
	}

}
