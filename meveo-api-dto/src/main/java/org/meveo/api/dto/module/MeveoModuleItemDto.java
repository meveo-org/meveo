package org.meveo.api.dto.module;

import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.CustomEntityCategoryDto;
import org.meveo.api.dto.CustomEntityTemplateDto;

import io.swagger.annotations.ApiModel;

/**
 * An item can be a class, script, custom field, notification, etc inside meveo.
 *
 * @author Clément Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
/**
 * 
 * @author clement.bareth
 * @since 
 * @version
 */
@ApiModel("MeveoModuleItemDto")
public class MeveoModuleItemDto extends BaseEntityDto implements Comparable<MeveoModuleItemDto> {

	private static final long serialVersionUID = -5514899106616353330L;

	private String dtoClassName;
	private Object dtoData;

	public MeveoModuleItemDto() {
		super();
	}

	public MeveoModuleItemDto(String dtoClassName, Object dtoData) {
		super();
		this.dtoClassName = dtoClassName;
		this.dtoData = dtoData;
	}

	public String getDtoClassName() {
		return dtoClassName;
	}

	public void setDtoClassName(String dtoClassName) {
		this.dtoClassName = dtoClassName;
	}

	public Object getDtoData() {
		return dtoData;
	}

	public void setDtoData(Object dtoData) {
		this.dtoData = dtoData;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((dtoClassName == null) ? 0 : dtoClassName.hashCode());
		result = prime * result + ((dtoData == null) ? 0 : dtoData.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MeveoModuleItemDto other = (MeveoModuleItemDto) obj;
		if (dtoClassName == null) {
			if (other.dtoClassName != null)
				return false;
		} else if (!dtoClassName.equals(other.dtoClassName))
			return false;
		if (dtoData == null) {
			if (other.dtoData != null)
				return false;
		} else if (!dtoData.equals(other.dtoData))
			return false;
		return true;
	}

	@Override
	public int compareTo(MeveoModuleItemDto o) {
		// Categories should be created before entity templates
    	if(this.dtoClassName.equals(CustomEntityTemplateDto.class.getName())) {
    		if(o.dtoClassName.equals(CustomEntityCategoryDto.class.getName())) {
    			return 1;
    		}
    	} else if(this.dtoClassName.equals(CustomEntityCategoryDto.class.getName())) {
      		if(o.dtoClassName.equals(CustomEntityTemplateDto.class.getName())) {
    			return -1;
    		}
    	}
    	
		return 0;
	}

	@Override
	public String toString() {
		return "MeveoModuleItemDto [dtoClassName=" + dtoClassName+"]";
	}

}
