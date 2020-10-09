package org.meveo.api.dto;

import java.util.List;

import org.meveo.api.dto.neo4j.ResultTable;

import com.fasterxml.jackson.annotation.JsonProperty;

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
	@JsonProperty("results")
	
	@ApiModelProperty("List of result table")
	private List<ResultTable> results = null;

	/**
	 * List of errors
	 */
	@JsonProperty("errors")
	
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
