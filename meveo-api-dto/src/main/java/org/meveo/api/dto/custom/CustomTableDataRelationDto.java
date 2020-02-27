package org.meveo.api.dto.custom;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents data in custom table for a relationship - custom entity data
 * stored in a separate table
 * 
 * @author Clément Bareth
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "CustomTableData")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel("CustomTableDataRelationDto")
public class CustomTableDataRelationDto extends CustomTableDataDto {

	private static final long serialVersionUID = -4151576536343447637L;

	/**
	 * A list of values with field name as map's key and field value as map's value
	 * and pointing on source and target entities UUIDs
	 */
	@XmlElementWrapper(name = "records")
	@XmlElement(name = "record")
	@JsonProperty("records")
	@ApiModelProperty("A list of values with field name as map's key and field value as map's value and pointing on source and target entities UUIDs")
	private List<CustomTableRelationRecordDto> records;

	public List<CustomTableRelationRecordDto> getRecords() {
		return records;
	}

	public void setRecords(List<CustomTableRelationRecordDto> records) {
		this.records = records;
	}

}
