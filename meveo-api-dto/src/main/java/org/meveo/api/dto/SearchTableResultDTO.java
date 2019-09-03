package org.meveo.api.dto;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import org.meveo.api.dto.neo4j.ResultTable;

import java.util.List;

public class SearchTableResultDTO {

    @SerializedName("results")
    @Expose
    private List<ResultTable> results = null;
    @SerializedName("errors")
    @Expose
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
