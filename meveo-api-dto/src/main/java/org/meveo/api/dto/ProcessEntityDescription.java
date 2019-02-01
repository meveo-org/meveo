package org.meveo.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.meveo.api.dto.technicalservice.InputOutputDescription;
import org.meveo.interfaces.technicalservice.description.EntityDescription;

import javax.validation.constraints.NotNull;

public class ProcessEntityDescription extends InputOutputDescription  {

    public static final String ENTITY_DESCRIPTION = "EntityDescription";

    @JsonProperty(required = true)
    @NotNull
    private String name;

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String name) {
        this.name = name;
    }

    public String getDescriptionType(){
        return ENTITY_DESCRIPTION;
    }

}
