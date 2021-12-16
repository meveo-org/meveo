/*
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.api.dto.payment;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;

import org.meveo.api.dto.BaseEntityDto;
import org.meveo.model.wf.WFAction;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Class WFActionDto.
 * 
 * @author anasseh
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @author Clément Bareth
 * @version 6.15
 */
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel("WFActionDto")
public class WFActionDto extends BaseEntityDto {

	/** The Constant serialVersionUID. */
	private static final long serialVersionUID = 8309866046667741458L;

	/** The uuid. */
	@XmlElement(required = false)
	@ApiModelProperty(required = true, value = "workflow's id")
	private String uuid;

	/** The action el. */
	@XmlElement(required = false)
	@ApiModelProperty(required = false, value = "expression to trigger when condition is true")
	@Deprecated
	private String actionEl;
	
	@XmlElement(required = true)
	@ApiModelProperty(required = true, value = "Script to trigger when condition is true")
	private String actionScript;
	
	@XmlElement(required = false)
	@ApiModelProperty(required = false, value = "Map representing the script parameter where the key is the paramer name and the value the el to evaluate")
    private Map<String, String> scriptParameters = new HashMap<>();

	/** The priority. */
	@XmlElement(required = false)
	@ApiModelProperty("order of this action")
	private Integer priority;

	/** The condition el. */
	@XmlElement(required = false)
	@ApiModelProperty("expression to evaluate to trigger an action")
	private String conditionEl;

	/**
	 * Instantiates a new WF action dto.
	 */
	public WFActionDto() {
	}

	/**
	 * Instantiates a new WF action dto.
	 *
	 * @param wfAction the WFAction entity
	 */
	public WFActionDto(WFAction wfAction) {
		this.uuid = wfAction.getUuid();
		this.actionEl = wfAction.getActionEl();
		this.priority = wfAction.getPriority();
		this.conditionEl = wfAction.getConditionEl();
		if(wfAction.getActionScript() != null) {
			this.actionScript = wfAction.getActionScript().getCode();
		}
		this.scriptParameters = wfAction.getScriptParameters();
	}
	
	/**
	 * @return the {@link #scriptParameters}
	 */
	public Map<String, String> getScriptParameters() {
		return scriptParameters;
	}

	/**
	 * @param scriptParameters the scriptParameters to set
	 */
	public void setScriptParameters(Map<String, String> scriptParameters) {
		this.scriptParameters = scriptParameters;
	}

	/**
	 * @return the {@link #serialversionuid}
	 */
	public static long getSerialversionuid() {
		return serialVersionUID;
	}

	/**
	 * @return the {@link #actionScript}
	 */
	public String getActionScript() {
		return actionScript;
	}

	/**
	 * @param actionScript the actionScript to set
	 */
	public void setActionScript(String actionScript) {
		this.actionScript = actionScript;
	}

	/**
	 * Gets the uuid.
	 *
	 * @return the uuid
	 */
	public String getUuid() {
		return uuid;
	}

	/**
	 * Sets the uuid.
	 *
	 * @param uuid the uuid to set
	 */
	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	/**
	 * Gets the action el.
	 *
	 * @return the actionEl
	 */
	public String getActionEl() {
		return actionEl;
	}

	/**
	 * Sets the action el.
	 *
	 * @param actionEl the actionEl to set
	 */
	public void setActionEl(String actionEl) {
		this.actionEl = actionEl;
	}

	/**
	 * Gets the priority.
	 *
	 * @return the priority
	 */
	public Integer getPriority() {
		return priority;
	}

	/**
	 * Sets the priority.
	 *
	 * @param priority the priority to set
	 */
	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	/**
	 * Gets the condition el.
	 *
	 * @return the conditionEl
	 */
	public String getConditionEl() {
		return conditionEl;
	}

	/**
	 * Sets the condition el.
	 *
	 * @param conditionEl the conditionEl to set
	 */
	public void setConditionEl(String conditionEl) {
		this.conditionEl = conditionEl;
	}

	@Override
	public String toString() {
		return "WFActionDto [actionEl=" + actionEl + ", priority=" + priority + ", conditionEl=" + conditionEl + "]";
	}

}
