package org.meveo.api.dto;

import java.util.List;

import org.meveo.api.dto.neo4j.ResultTable;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import io.swagger.annotations.ApiModelProperty;

/**
 * Search table result object.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
public class SearchTableResultDTO {

	/**
	 * List of {@link ResultTable}
	 */
	@SerializedName("results")
	@Expose
	@ApiModelProperty("List of result table")
	private List<ResultTable> results = null;

	/**
	 * List of errors
	 */
	@SerializedName("errors")
	@Expose
	@ApiModelProperty("List of errors")
	private List<Object> errors = null;

	public List<ResultTable> getResults() {
		return results;
	}

	public void setResults(List<ResultTable> results) {
		this.results = results;
	}

	public List<Object> getErrors() {
		return errors;
	}

	public void setErrors(List<Object> errors) {
		this.errors = errors;
	}

}
