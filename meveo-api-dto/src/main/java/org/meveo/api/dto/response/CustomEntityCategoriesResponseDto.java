package org.meveo.api.dto.response;

import org.meveo.api.dto.CustomEntityCategoryDto;

import java.util.List;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @lastModifiedVersion 6.4.0
 */
public class CustomEntityCategoriesResponseDto extends SearchResponse {

	private static final long serialVersionUID = 467985294297950021L;

	private List<CustomEntityCategoryDto> customEntityCategories;

	public List<CustomEntityCategoryDto> getCustomEntityCategories() {
		return customEntityCategories;
	}

	public void setCustomEntityCategories(List<CustomEntityCategoryDto> customEntityCategories) {
		this.customEntityCategories = customEntityCategories;
	}
}
