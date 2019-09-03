package org.meveo.api.dto;

import java.util.ArrayList;
import java.util.List;

public class CrtSearchEntityDTO {
	
	private String crtCode;
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
