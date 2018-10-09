package org.meveo.model.scripts;

import org.hibernate.annotations.Type;
import org.meveo.model.ExportIdentifier;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@ExportIdentifier({ "code"})
@MappedSuperclass
public abstract class CustomScript extends Executable {

    private static final long serialVersionUID = 8176170199770220430L;

    @Enumerated(EnumType.STRING)
    @Column(name = "src_type")
    private ScriptSourceTypeEnum sourceTypeEnum = ScriptSourceTypeEnum.JAVA;

    @Transient
    private List<ScriptInstanceError> scriptErrors = new ArrayList<ScriptInstanceError>();

    @Type(type="numeric_boolean")
    @Column(name = "is_error")
    private Boolean error = false;

    /**
     * @return Script language
     */
    public ScriptSourceTypeEnum getSourceTypeEnum() {
        return sourceTypeEnum;
    }

    /**
     * @param sourceTypeEnum Script language
     */
    public void setSourceTypeEnum(ScriptSourceTypeEnum sourceTypeEnum) {
        this.sourceTypeEnum = sourceTypeEnum;
    }

    /**
     * @return the Script errors
     */
    public List<ScriptInstanceError> getScriptErrors() {
        return scriptErrors;
    }

    /**
     * @param scriptErrors Script errors to set
     */
    public void setScriptErrors(List<ScriptInstanceError> scriptErrors) {
        this.scriptErrors = scriptErrors;
    }

    /**
     * @return the error
     */
    public Boolean isError() {
        return error;
    }

    /**
     * @return the error
     */
    public Boolean getError() {
        return error;
    }

    /**
     * @param error the error to set
     */
    public void setError(Boolean error) {
        this.error = error;
    }
}