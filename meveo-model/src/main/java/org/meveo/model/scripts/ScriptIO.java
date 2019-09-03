package org.meveo.model.scripts;

public class ScriptIO {
    private String name;

    private boolean editable = true;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }


}
