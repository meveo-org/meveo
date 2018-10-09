package org.meveo.api.dto.technicalservice;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RelationDescriptionDto extends InputOutputDescriptionDto {

    @JsonProperty(required = true)
    private String source;
    @JsonProperty(required = true)
    private String target;

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
        return source + "-" + target;
    }

    @Override
    public void setName(String name) {
    }

}
