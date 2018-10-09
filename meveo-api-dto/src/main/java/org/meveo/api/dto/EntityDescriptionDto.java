package org.meveo.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.meveo.api.dto.technicalservice.InputOutputDescriptionDto;

import javax.validation.constraints.NotNull;

public class EntityDescriptionDto extends InputOutputDescriptionDto {

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

}
