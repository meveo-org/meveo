package org.meveo.api.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SearchTypeDTO {
	
	private String cetCode;
	private String cetLabel;
	private String pictoCode;
	private List<SearchField> cetFields;
	private List<CrtSearchEntityDTO> crtFields;
	
	
	public class SearchField{
		private String code;
		private String label;
		private String type;
		
		
		public SearchField(String code,String label,
				String type) {
			super();
			this.code = code;
			this.label = label;
			this.type = type;
		}
		public String getCode() {
			return code;
		}
		public void setCode(String code) {
			this.code = code;
		}
		
		public String getLabel() {
			return label;
		}
		public void setLabel(String label) {
			this.label = label;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}
		
		
	}


	public String getCetCode() {
		return cetCode;
	}


	public void setCetCode(String cetCode) {
		this.cetCode = cetCode;
	}


	public String getCetLabel() {
		return cetLabel;
	}


	public void setCetLabel(String cetLabel) {
		this.cetLabel = cetLabel;
	}


	public List<SearchField> getCetFields() {
		return cetFields;
	}

	public void addCetField(SearchField cetField) {
		if(cetFields==null){
			cetFields=new ArrayList<>();
		}
		cetFields.add(cetField);
	}
	public void setCetFields(List<SearchField> cetFields) {
		this.cetFields = cetFields;
	}


	
	public String getPictoCode() {
		return pictoCode;
	}


	public void setPictoCode(String pictoCode) {
		this.pictoCode = pictoCode;
	}


	public List<CrtSearchEntityDTO> getCrtFields() {
		return crtFields;
	}


	public void setCrtFields(List<CrtSearchEntityDTO> crtFields) {
		this.crtFields = crtFields;
	}


	public void addCrtField(CrtSearchEntityDTO crtField) {
		if(crtFields==null){
			crtFields=new ArrayList<>();
		}
		crtFields.add(crtField);
	}
	
	

}
