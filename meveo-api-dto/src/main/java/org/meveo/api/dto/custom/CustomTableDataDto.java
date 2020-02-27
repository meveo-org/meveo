package org.meveo.api.dto.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.api.dto.BaseEntityDto;
import org.meveo.model.sql.SqlConfiguration;

import com.fasterxml.jackson.annotation.JsonProperty;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Represents data in custom table - custom entity data stored in a separate
 * table
 * 
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "CustomTableData")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel("CustomTableDataDto")
public class CustomTableDataDto extends BaseEntityDto {

	private static final long serialVersionUID = -1209601309024979414L;

	/**
	 * The {@link SqlConfiguration} code.
	 */
	@ApiModelProperty("Code of SqlConnection")
	private String sqlConnectionCode;

	/**
	 * Custom table/custom entity (or relation) template code
	 */
	@XmlAttribute(required = true)
	@ApiModelProperty("Custom table/custom entity (or relation) template code")
	private String customTableCode;

	/**
	 * Should data be overwritten (deleted all data first) instead of appended to
	 * existing values. Defaults to false if omitted.
	 */
	@XmlAttribute
	@ApiModelProperty("Should data be overwritten (deleted all data first) instead of appended to existing values. Defaults to false if omitted.")
	private Boolean overwrite;

	/**
	 * A list of values with field name as map's key and field value as map's value
	 */
	@XmlElementWrapper(name = "records")
	@XmlElement(name = "record")
	@JsonProperty("records")
	@ApiModelProperty("A list of values with field name as map's key and field value as map's value")
	private List<CustomTableRecordDto> values;

	/**
	 * @return Custom table/custom entity template code
	 */
	public String getCustomTableCode() {
		return customTableCode;
	}

	/**
	 * @param customTableCode Custom table/custom entity template code
	 */
	public void setCustomTableCode(String customTableCode) {
		this.customTableCode = customTableCode;
	}

	/**
	 * @return Should data be overwritten (deleted all data first) instead of
	 *         appended to existing values. Defaults to false if null.
	 */
	public Boolean getOverwrite() {
		return overwrite;
	}

	/**
	 * @param overrwrite Should data be overwritten (deleted all data first) instead
	 *                   of appended to existing values.
	 */
	public void setOverwrite(Boolean overrwrite) {
		this.overwrite = overrwrite;
	}

	/**
	 * @return A list of values with field name as map's key and field value as
	 *         map's value
	 */
	public List<CustomTableRecordDto> getValues() {
		return values;
	}

	/**
	 * @param values A list of values with field name as map's key and field value
	 *               as map's value
	 */
	public void setValues(List<CustomTableRecordDto> values) {
		this.values = values;
	}

	public void setValuesFromListofMap(List<Map<String, Object>> list) {

		if (list == null) {
			return;

		}
		values = new ArrayList<>();

		for (Map<String, Object> item : list) {
			values.add(new CustomTableRecordDto(item));
		}
	}

	public String getSqlConnectionCode() {
		return sqlConnectionCode;
	}

	public void setSqlConnectionCode(String sqlConnectionCode) {
		this.sqlConnectionCode = sqlConnectionCode;
	}
}