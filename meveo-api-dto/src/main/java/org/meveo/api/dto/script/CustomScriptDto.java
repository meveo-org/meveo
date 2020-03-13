package org.meveo.api.dto.script;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.apache.commons.lang3.StringUtils;
import org.meveo.api.dto.function.FunctionDto;
import org.meveo.model.scripts.CustomScript;
import org.meveo.model.scripts.ScriptSourceTypeEnum;

/**
 * The Class CustomScriptDto.
 *
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @lastModifiedVersion 6.5.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
public abstract class CustomScriptDto extends FunctionDto {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = -977313726064562882L;

	/** The type. */
	@XmlElement
	private ScriptSourceTypeEnum type = ScriptSourceTypeEnum.JAVA;

	/** The script. */
	@XmlElement(required = true)
	private String script;

	/**
	 * Instantiates a new custom script dto.
	 */
	public CustomScriptDto() {

	}

	/**
	 * Instantiates a new custom script dto.
	 *
	 * @param code        the code
	 * @param description the description
	 * @param type        the type
	 * @param script      the script
	 */
	public CustomScriptDto(String code, String description, ScriptSourceTypeEnum type, String script) {

		this.type = type;
		this.script = script;
	}

	public CustomScriptDto(CustomScript e, String script) {

		super(e);
		this.type = e.getSourceTypeEnum();
		this.script = script;
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
	public boolean isCodeOnly() {
		return StringUtils.isBlank(script);
	}

	@Override
	public String toString() {
		return "CustomScriptDto [code=" + getCode() + ", description=" + getDescription() + ", type=" + type + ", script=" + script + "]";
	}
}