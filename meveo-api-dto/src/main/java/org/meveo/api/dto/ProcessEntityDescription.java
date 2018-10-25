package org.meveo.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.meveo.api.dto.technicalservice.InputOutputDescription;

import javax.validation.constraints.NotNull;

public class ProcessEntityDescription extends InputOutputDescription {

    public static final String ENTITY_DESCRIPTION = "EntityDescription";

    @JsonProperty(required = true)
    @NotNull
    private String name;

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescriptionType(){
        return ENTITY_DESCRIPTION;
    }

}
