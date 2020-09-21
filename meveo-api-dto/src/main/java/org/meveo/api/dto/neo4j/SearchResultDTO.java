package org.meveo.api.dto.neo4j;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SearchResultDTO {

    @JsonProperty("results")
    
    private List<Result> results = null;
    @JsonProperty("errors")
    
    private List<Object> errors = null;

    public List<Result> getResults() {
        return results;
    }

    public void setResults(List<Result> results) {
        this.results = results;
    }

    public List<Object> getErrors() {
        return errors;
    }

    public void setErrors(List<Object> errors) {
        this.errors = errors;
    }

}
