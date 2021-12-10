package org.meveo.model.scripts;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Type;
import org.meveo.commons.utils.XStreamCDATAConverter;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.persistence.JsonTypes;

import com.thoughtworks.xstream.annotations.XStreamConverter;

/**
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 **/
@ExportIdentifier({ "code" })
@MappedSuperclass
public abstract class CustomScript extends Function {

	private static final long serialVersionUID = 8176170199770220430L;

	/** Script type */
	public static final String TYPE = "Script";

	@Column(name = "script", nullable = false, columnDefinition = "TEXT")
	@XStreamConverter(XStreamCDATAConverter.class)
	private String script;

	@Enumerated(EnumType.STRING)
	@Column(name = "src_type")
	private ScriptSourceTypeEnum sourceTypeEnum = ScriptSourceTypeEnum.JAVA;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "tx_type")
	private ScriptTransactionType transactionType = ScriptTransactionType.SAME;

	@Transient
	private List<ScriptInstanceError> scriptErrors = new ArrayList<>();

	@Type(type = "numeric_boolean")
	@Column(name = "is_error")
	private Boolean error = false;

	/**
	 * Setters defined for the script
	 */
	@Type(type = JsonTypes.JSON_LIST)
	@Column(name = "setters", columnDefinition = "text")
	private List<Accessor> setters;

	/**
	 * Getters defined for the script
	 */
	@Type(type = JsonTypes.JSON_LIST)
	@Column(name = "getters", columnDefinition = "text")
	private List<Accessor> getters;

	@Fetch(FetchMode.JOIN)
	@ElementCollection(fetch = FetchType.LAZY)
	@JoinTable(name = "meveo_script_inputs", joinColumns = @JoinColumn(name = "meveo_script_instance_id"))
	@Column(name = "script_input")
	private Set<String> scriptInputs;

	@Fetch(FetchMode.JOIN)
	@ElementCollection(fetch = FetchType.LAZY)
	@JoinTable(name = "meveo_script_outputs", joinColumns = @JoinColumn(name = "meveo_script_instance_id"))
	@Column(name = "script_output")
	private Set<String> scriptOutputs;
	
	/**
	 * @return the {@link #transactionType}
	 */
	public ScriptTransactionType getTransactionType() {
		return transactionType;
	}

	/**
	 * @param transactionType the transactionType to set
	 */
	public void setTransactionType(ScriptTransactionType transactionType) {
		this.transactionType = transactionType;
	}

	/**
	 * @return the script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * @param script The script to set
	 */
	public void setScript(String script) {
		this.script = script;
	}

	public List<Accessor> getSettersNullSafe() {
		if (setters == null) {
			setters = new ArrayList<Accessor>();
		}

		return getSetters();
	}

	public List<Accessor> getSetters() {
		return setters;
	}

	public void setSetters(List<Accessor> setters) {
		this.setters = setters;
	}

	public List<Accessor> getGettersNullSafe() {
		if (getters == null) {
			getters = new ArrayList<>();
		}
		return getGetters();
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
		List<FunctionIO> inputs = Optional.ofNullable(setters).orElse(List.of()).stream().map(s -> {
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
		List<FunctionIO> outputs = Optional.ofNullable(getters).orElse(List.of()).stream().map(s -> {
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

	public Set<String> getScriptInputsNullSafe() {
		if (scriptInputs == null) {
			scriptInputs = new HashSet<String>();
		}
		return getScriptInputs();
	}

	public Set<String> getScriptInputs() {
		return scriptInputs;
	}

	public void setScriptInputs(Set<String> scriptInputs) {
		this.scriptInputs = scriptInputs;
	}

	public Set<String> getScriptOutputsNullSafe() {
		if (scriptOutputs == null) {
			scriptOutputs = new HashSet<String>();
		}
		return getScriptOutputs();
	}

	public Set<String> getScriptOutputs() {
		return scriptOutputs;
	}

	public void setScriptOutputs(Set<String> scriptOutputs) {
		this.scriptOutputs = scriptOutputs;
	}
}