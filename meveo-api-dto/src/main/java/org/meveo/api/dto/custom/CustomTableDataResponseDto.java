package org.meveo.api.dto.custom;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.response.SearchResponse;

import io.swagger.annotations.ApiModelProperty;

/**
 * Represents search within custom table results. Wrapper to custom table
 * information.
 *
 * @see CustomTableDataDto
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "CustomTableDataResponse")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomTableDataResponseDto extends SearchResponse {

	private static final long serialVersionUID = -2044679362279307652L;

	/**
	 * Search within custom table results
	 */
	@XmlElement(name = "tableData")
	@ApiModelProperty("Search within custom table results")
	private CustomTableDataDto customTableData = new CustomTableDataDto();

	/**
	 * @return Search within custom table results
	 */
	public CustomTableDataDto getCustomTableData() {
		return customTableData;
	}

	/**
	 * @param customTableData Search within custom table results
	 */
	public void setCustomTableData(CustomTableDataDto customTableData) {
		this.customTableData = customTableData;
	}

	@Override
	public String toString() {
		return "CustomTableDataResponseDto [customTableData=" + customTableData + " " + super.toString() + "]";
	}

}