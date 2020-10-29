package org.meveo.api.dto.script;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.StringUtils;
import org.meveo.api.dto.function.FunctionDto;
import org.meveo.model.scripts.CustomScript;
import org.meveo.model.scripts.ScriptSourceTypeEnum;
import org.meveo.model.scripts.ScriptTransactionType;

import com.fasterxml.jackson.annotation.JsonIgnore;

/**
 * The Class CustomScriptDto.
 *
 * @author Clément Bareth | clement.bareth@gmail.com
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.12.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class CustomScriptDto extends FunctionDto {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -977313726064562882L;

	/** The type. */
	@XmlElement
	private ScriptSourceTypeEnum type = ScriptSourceTypeEnum.JAVA;
	
	/** The transaction management for executing the script */
	private ScriptTransactionType transactionType = ScriptTransactionType.SAME;

	/** The script. */
	@XmlElement(required = true)
	private String script;

	/**
	 * Instantiates a new custom script dto.
	 */
	public CustomScriptDto() {

	}

	public CustomScriptDto(CustomScript e, String script) {
		super(e);
		this.type = e.getSourceTypeEnum();
		this.script = script;
        this.transactionType = e.getTransactionType();
	}

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
	 * Gets the type.
	 *
	 * @return the type
	 */
	public ScriptSourceTypeEnum getType() {
		return type;
	}

	/**
	 * Sets the type.
	 *
	 * @param type the new type
	 */
	public void setType(ScriptSourceTypeEnum type) {
		this.type = type;
	}

	/**
	 * Gets the script.
	 *
	 * @return the script
	 */
	public String getScript() {
		return script;
	}

	/**
	 * Sets the script.
	 *
	 * @param script the new script
	 */
	public void setScript(String script) {
		this.script = script;
	}

	/**
	 * Checks if is code only.
	 *
	 * @return true, if is code only
	 */
	@JsonIgnore
	public boolean isCodeOnly() {
		return StringUtils.isBlank(script);
	}

	@Override
	public String toString() {
		return "CustomScriptDto [code=" + getCode() + ", description=" + getDescription() + ", type=" + type + ", script=" + script + "]";
	}
}