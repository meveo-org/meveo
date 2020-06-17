package org.meveo.api.dto.module;

import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.model.BusinessEntity;
import org.meveo.model.IEntity;

import io.swagger.annotations.ApiModel;

/**
 * The Class BaseDataModelDto.
 * 
 * @author andrius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@ApiModel("BaseDataModelDto")
abstract class BaseDataModelDto extends BusinessEntityDto implements IEntity<Long> {

	/**
	 * Instantiates a new base data model dto.
	 */
	public BaseDataModelDto() {
		super();
	}

	/**
	 * Instantiates a new base data model dto.
	 *
	 * @param businessEntity the businessEntity
	 */
	public BaseDataModelDto(BusinessEntity businessEntity) {
		super(businessEntity);
	}

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 1L;

}