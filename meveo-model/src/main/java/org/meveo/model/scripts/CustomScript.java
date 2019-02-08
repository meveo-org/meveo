package org.meveo.model.scripts;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.hibernate.annotations.Type;
import org.meveo.commons.utils.XStreamCDATAConverter;
import org.meveo.model.ExportIdentifier;

@ExportIdentifier({ "code"})
@MappedSuperclass
public abstract class CustomScript extends Function {

    private static final long serialVersionUID = 8176170199770220430L;
    public static final String TYPE = "Script";

    @Column(name = "script", nullable = false, columnDefinition = "TEXT")
    @NotNull
    @XStreamConverter(XStreamCDATAConverter.class)
    private String script;

    @Enumerated(EnumType.STRING)
    @Column(name = "src_type")
    private ScriptSourceTypeEnum sourceTypeEnum = ScriptSourceTypeEnum.JAVA;

    @Transient
    private List<ScriptInstanceError> scriptErrors = new ArrayList<>();

    @Type(type="numeric_boolean")
    @Column(name = "is_error")
    private Boolean error = false;

    /**
     * Setters defined for the script
     */
    @Type(type = "jsonList")
    @Column(name = "setters", columnDefinition = "text")
    private List<Accessor> setters = new ArrayList<>();

    /**
     * Getters defined for the script
     */
    @Type(type = "jsonList")
    @Column(name = "getters", columnDefinition = "text")
    private List<Accessor> getters = new ArrayList<>();

    /**
     * @return the script
     */
    public String getScript() {
        return script;
    }

    /**
     * @param script the script to set
     */
    public void setScript(String script) {
        this.script = script;
    }

    public List<Accessor> getSetters() {
        return setters != null ? setters : new ArrayList<>();
    }

    public void setSetters(List<Accessor> setters) {
        this.setters = setters;
    }

    public List<Accessor> getGetters() {
        return getters;
    }

    public void setGetters(List<Accessor> getters) {
        this.getters = getters;
    }

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

	@Override
	public List<FunctionIO> getInputs() {
		if(setters == null) {
			return new ArrayList<>();
		}
		return setters.stream().map(s -> {
					FunctionIO inp = new FunctionIO();
					inp.setDescription(s.getDescription());
					inp.setName(s.getName());
					inp.setType(s.getType());
					return inp;
				}).collect(Collectors.toList());
	}

    @Override
    public List<FunctionIO> getOutputs() {
        if(setters == null) {
            return new ArrayList<>();
        }
        return getters.stream().map(s -> {
            FunctionIO inp = new FunctionIO();
            inp.setDescription(s.getDescription());
            inp.setName(s.getName());
            inp.setType(s.getType());
            return inp;
        }).collect(Collectors.toList());
    }

    @Override
	public boolean hasInputs() {
        return setters != null && !setters.isEmpty();
    }

    @Override
    public boolean hasOutputs() {
        return getters != null && !getters.isEmpty();
    }

    @Override
    public String getFunctionType() {
        return TYPE;
    }
}