package org.meveo.api.dto.technicalservice;


import javax.ws.rs.QueryParam;

public class TechnicalServiceFilters {


    @QueryParam("name")
    private String name;

    @QueryParam("likeName")
    private String likeName;

    public String getName() {
        return name;
    }

    public String getLikeName() {
        return likeName;
    }
}
