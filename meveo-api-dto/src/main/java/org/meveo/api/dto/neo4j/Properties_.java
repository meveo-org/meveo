
package org.meveo.api.dto.neo4j;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

public class Properties_ {

    @SerializedName("updateDate")
    @Expose
    private Integer internalUpdateDate;
    
    @SerializedName("sourceName")
    @Expose
    private String sourceName;
    
    @SerializedName("sourceCode")
    @Expose
    private String sourceCode;
    
    @SerializedName("internal_identifier")
    @Expose
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
