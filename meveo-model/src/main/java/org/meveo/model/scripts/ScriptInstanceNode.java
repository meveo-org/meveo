package org.meveo.model.scripts;

public class ScriptInstanceNode {
    private String name;
    private String fullName;
    private Boolean error;
    private Long id;
    private String scriptType;

    public ScriptInstanceNode() {
    }

    public ScriptInstanceNode(String name, String scriptType) {
        this.name = name;
        this.scriptType = scriptType;
    }

    public ScriptInstanceNode(String name, String scriptType, String fullName, Boolean error, Long id) {
        this.name = name;
        this.scriptType = scriptType;
        this.fullName = fullName;
        this.error = error;
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean getError() {
        return error;
    }

    public void setError(Boolean error) {
        this.error = error;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getScriptType() {
        return scriptType;
    }

    public void setScriptType(String scriptType) {
        this.scriptType = scriptType;
    }
}
