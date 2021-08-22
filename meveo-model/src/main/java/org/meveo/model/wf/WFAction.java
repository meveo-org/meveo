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
package org.meveo.model.wf;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.annotations.Parameter;
import org.hibernate.annotations.Type;
import org.meveo.model.EnableEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.persistence.JsonTypes;
import org.meveo.model.scripts.ScriptInstance;

@Entity
@ExportIdentifier({ "uuid" })
@Table(name = "wf_action", uniqueConstraints = @UniqueConstraint(columnNames = { "uuid" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = {
        @Parameter(name = "sequence_name", value = "wf_action_seq"), })
@NamedQueries({ @NamedQuery(name = "WFAction.listByTransition", query = "SELECT wfa FROM WFAction wfa where  wfa.wfTransition=:wfTransition order by priority ASC") })
public class WFAction extends EnableEntity {

    private static final long serialVersionUID = 1L;

    @Column(name = "uuid", nullable = false, updatable = false, length = 60)
    @Size(max = 60)
    @NotNull
    private String uuid = UUID.randomUUID().toString();

    @Deprecated
    @Column(name = "action_el", length = 2000)
    @Size(max = 2000)
    private String actionEl;
    
    //TODO : Make this field mandatory 
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "action_script")
    private ScriptInstance actionScript;
    
    /**
     * Map representing the script parameter where the key is the paramer name
     * and the value the el to evaluate
     */
    @Column(name = "script_parameters", columnDefinition = "text")
    @Type(type = JsonTypes.JSON)
    private Map<String, String> scriptParameters = new HashMap<>();

    @Column(name = "priority")
    private int priority;

    @Column(name = "condition_el", length = 2000)
    @Size(max = 2000)
    private String conditionEl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "wf_transition_id")
    private WFTransition wfTransition;
    
    /**
	 * @return the {@link #scriptParameters}
	 */
	public Map<String, String> getScriptParameters() {
		if(scriptParameters == null) {
			this.scriptParameters = new HashMap<>();
		}
		return scriptParameters;
	}

	/**
	 * @param scriptParameters the scriptParameters to set
	 */
	public void setScriptParameters(Map<String, String> scriptParameters) {
		this.scriptParameters = scriptParameters;
	}

	/**
	 * @return the {@link #actionScript}
	 */
	public ScriptInstance getActionScript() {
		return actionScript;
	}

	/**
	 * @param actionScript the actionScript to set
	 */
	public void setActionScript(ScriptInstance actionScript) {
		this.actionScript = actionScript;
	}

	public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the actionEl
     */
    public String getActionEl() {
        return actionEl;
    }

    /**
     * @param actionEl the actionEl to set
     */
    public void setActionEl(String actionEl) {
        this.actionEl = actionEl;
    }

    /**
     * @return the priority
     */
    public int getPriority() {
        return priority;
    }

    /**
     * @param priority the priority to set
     */
    public void setPriority(int priority) {
        this.priority = priority;
    }

    /**
     * @return the wfTransition
     */
    public WFTransition getWfTransition() {
        return wfTransition;
    }

    /**
     * @param wfTransition the wfTransition to set
     */
    public void setWfTransition(WFTransition wfTransition) {
        this.wfTransition = wfTransition;
    }

    public String getConditionEl() {
        return conditionEl;
    }

    public void setConditionEl(String conditionEl) {
        this.conditionEl = conditionEl;
    }

    public String getUserGroupCode() {
        if (!StringUtils.isBlank(actionEl) && actionEl.indexOf(",") >= 0) {
            int startIndexCode = actionEl.indexOf(",") + 2;
            int endIndexCode = actionEl.length() - 3;
            String userGroupCode = actionEl.substring(startIndexCode, endIndexCode);
            return userGroupCode;
        }
        return null;
    }

    @Override
    public int hashCode() {
        return 961 + ("WfAction" + id).hashCode();
    }

    @Override
    public boolean equals(Object obj) {

        if (this == obj) {
            return true;
        } else if (obj == null) {
            return false;
        } else if (!(obj instanceof WFAction)) {
            return false;
        }

        WFAction other = (WFAction) obj;
        if (getId() == null) {
            if (other.getId() != null) {
                return false;
            }
        } else if (!getId().equals(other.getId())) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("WFAction [actionEl=%s, conditionEl=%s]", actionEl, conditionEl);
    }

    public String clearUuid() {
        String oldUuid = uuid;
        uuid = UUID.randomUUID().toString();
        return oldUuid;
    }

}