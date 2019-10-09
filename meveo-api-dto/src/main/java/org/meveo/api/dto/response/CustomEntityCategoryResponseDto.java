package org.meveo.api.dto.response;

import org.meveo.api.dto.CustomEntityCategoryDto;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 */
public class CustomEntityCategoryResponseDto extends BaseResponse {

	private static final long serialVersionUID = 7280065145323467467L;

	private CustomEntityCategoryDto customEntityCategory;

	public CustomEntityCategoryDto getCustomEntityCategory() {
		return customEntityCategory;
	}

	public void setCustomEntityCategory(CustomEntityCategoryDto customEntityCategory) {
		this.customEntityCategory = customEntityCategory;
	}
}
