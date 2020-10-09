
package org.meveo.api.dto.neo4j;

import com.fasterxml.jackson.annotation.JsonProperty;

public class Properties_ {

    @JsonProperty("updateDate")
    
    private Integer internalUpdateDate;
    
    @JsonProperty("sourceName")
    
    private String sourceName;
    
    @JsonProperty("sourceCode")
    
    private String sourceCode;
    
    @JsonProperty("internal_identifier")
    
    private String internal_identifier;

    public Integer getInternalUpdateDate() {
        return internalUpdateDate;
    }

    public void setInternalUpdateDate(Integer internalUpdateDate) {
        this.internalUpdateDate = internalUpdateDate;
    }

	public String getInternal_identifier() {
		return internal_identifier;
	}

	public void setInternal_identifier(String internal_identifier) {
		this.internal_identifier = internal_identifier;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public String getSourceCode() {
		return sourceCode;
	}

	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}


    
    

}
