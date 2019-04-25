package org.meveo.model.scripts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.persistence.*;
import javax.validation.constraints.NotNull;

import com.thoughtworks.xstream.annotations.XStreamConverter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
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

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name="meveo_script_inputs", joinColumns=@JoinColumn(name="meveo_script_instance_id"))
    @Column(name="script_input")
    private Set<String> scriptInputs = new HashSet<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @JoinTable(name="meveo_script_outputs", joinColumns=@JoinColumn(name="meveo_script_instance_id"))
    @Column(name="script_output")
    private Set<String> scriptOutputs = new HashSet<>();

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
        List<FunctionIO> inputs = setters.stream().map(s -> {
            FunctionIO inp = new FunctionIO();
            inp.setDescription(s.getDescription());
            inp.setName(s.getName());
            inp.setType(s.getType());
            return inp;
        }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(scriptInputs)) {
            inputs.addAll(scriptInputs.stream().map(s -> {
                FunctionIO inp = new FunctionIO();
                inp.setDescription(s);
                inp.setName(s);
                inp.setType(StringUtils.EMPTY);
                return inp;
            }).collect(Collectors.toList()));
        }
        return inputs;
    }

    @Override
    public List<FunctionIO> getOutputs() {
        if(setters == null) {
            return new ArrayList<>();
        }
        List<FunctionIO> outputs = getters.stream().map(s -> {
            FunctionIO inp = new FunctionIO();
            inp.setDescription(s.getDescription());
            inp.setName(s.getName());
            inp.setType(s.getType());
            return inp;
        }).collect(Collectors.toList());
        if (CollectionUtils.isNotEmpty(scriptOutputs)) {
            outputs.addAll(scriptOutputs.stream().map(s -> {
                FunctionIO inp = new FunctionIO();
                inp.setDescription(s);
                inp.setName(s);
                inp.setType(StringUtils.EMPTY);
                return inp;
            }).collect(Collectors.toList()));
        }
        return outputs;
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

    public Set<String> getScriptInputs() {
        return scriptInputs;
    }

    public void setScriptInputs(Set<String> scriptInputs) {
        this.scriptInputs = scriptInputs;
    }

    public Set<String> getScriptOutputs() {
        return scriptOutputs;
    }

    public void setScriptOutputs(Set<String> scriptOutputs) {
        this.scriptOutputs = scriptOutputs;
    }
}