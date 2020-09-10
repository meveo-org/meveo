package org.meveo.api.dto.custom;

import java.util.ArrayList;
import java.util.List;

import org.meveo.api.dto.response.SearchResponse;
import org.meveo.model.customEntities.CustomEntityInstanceAudit;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.11.0
 */
public class CustomEntityInstanceAuditsResponseDto extends SearchResponse {

	private static final long serialVersionUID = 6463679858430836397L;
	private List<CustomEntityInstanceAudit> customEntityInstanceAuditdAudits = new ArrayList<CustomEntityInstanceAudit>();

	public List<CustomEntityInstanceAudit> getCustomEntityInstanceAuditdAudits() {
		return customEntityInstanceAuditdAudits;
	}

	public void setCustomEntityInstanceAuditdAudits(List<CustomEntityInstanceAudit> customEntityInstanceAuditdAudits) {
		this.customEntityInstanceAuditdAudits = customEntityInstanceAuditdAudits;
	}

}
