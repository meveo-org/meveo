package org.meveo.api.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@ApiModel
public class CrtSearchEntityDTO {
	
	@ApiModelProperty("Code of crt")
	private String crtCode;
	
	@ApiModelProperty("Fields of the crt")
	private List<SearchTypeDTO.SearchField> crtFields;
	
	public String getCrtCode() {
		return crtCode;
	}
	public void setCrtCode(String crtCode) {
		this.crtCode = crtCode;
	}
	public List<SearchTypeDTO.SearchField> getCrtFields() {
		return crtFields;
	}
	public void setCrtFields(List<SearchTypeDTO.SearchField> crtFields) {
		this.crtFields = crtFields;
	}
	public void addCrtField(SearchTypeDTO.SearchField crtField) {
		if(crtFields==null){
			crtFields=new ArrayList<>();
		}
		crtFields.add(crtField);
	}
	
}
