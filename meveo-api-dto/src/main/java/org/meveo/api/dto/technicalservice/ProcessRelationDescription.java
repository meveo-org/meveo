package org.meveo.api.dto.technicalservice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ProcessRelationDescription extends InputOutputDescription {

    public static final String RELATION_DESCRIPTION = "RelationDescription";

    @JsonProperty(required = true)
    private String source;
    @JsonProperty(required = true)
    private String target;
    
    private String name;

    /**
     * Source entity instance name of the relation
     */
    public String getSource() {
        return source;
    }

    /**
     * @param source Source entity instance name of the relation
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * Target entity instance name of the relation
     */
    public String getTarget() {
        return target;
    }

    /**
     * @param target Target entity instance name of the relation
     */
    public void setTarget(String target) {
        this.target = target;
    }

    @Override
    public String getName() {
        return name == null ? source + "-" + target : this.name;
    }

    @Override
    public void setName(String name) {
    	this.name = name;
    }

}
