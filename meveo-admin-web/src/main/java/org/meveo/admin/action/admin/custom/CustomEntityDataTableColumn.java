package org.meveo.admin.action.admin.custom;

public class CustomEntityDataTableColumn {
    private String header;
    private String property;
    public CustomEntityDataTableColumn(String header, String property) {
        this.header = header;
        this.property = property;
    }
    public String getHeader() {
        return header;
    }
    public String getProperty() {
        return property;
    } 
} 
