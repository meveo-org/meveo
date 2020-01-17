package org.meveo.api.dto;

import java.util.ArrayList;
import java.util.List;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The type of search when searching custom entity.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@ApiModel
public class SearchTypeDTO {

	/**
	 * The cet code
	 */
	@ApiModelProperty("The cet code")
	private String cetCode;

	/**
	 * The cet label
	 */
	@ApiModelProperty("The cet label")
	private String cetLabel;

	/**
	 * The picto code
	 */
	@ApiModelProperty("The picto code")
	private String pictoCode;

	/**
	 * List of cet fields
	 */
	@ApiModelProperty("List of cet fields")
	private List<SearchField> cetFields;

	/**
	 * List of search entities
	 */
	@ApiModelProperty("List of search entities")
	private List<CrtSearchEntityDTO> crtFields;

	/**
	 * The field being search when searching a custom entity.
	 * 
	 * @author Edward P. Legaspi | czetsuya@gmail.com
	 * @version 6.7.0
	 */
	public class SearchField {

		/**
		 * Code of search field
		 */
		@ApiModelProperty("Code of search field")
		private String code;

		/**
		 * Label of search field
		 */
		@ApiModelProperty("Label of search field")
		private String label;

		/**
		 * Type of search field
		 */
		@ApiModelProperty("Type of search field")
		private String type;

		public SearchField(String code, String label, String type) {
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
		if (cetFields == null) {
			cetFields = new ArrayList<>();
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
		if (crtFields == null) {
			crtFields = new ArrayList<>();
		}
		crtFields.add(crtField);
	}

}
