/**
 * 
 */
package org.meveo.model.technicalservice.wsendpoint;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.persistence.Column;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Type;
import org.meveo.model.BusinessEntity;
import org.meveo.model.scripts.Function;

@MappedSuperclass
public abstract class Websocket extends BusinessEntity {

	private static final long serialVersionUID = -8448819177071169076L;

	/** Whether websocket endpoint is accessible without logging */
	@Column(name = "secured", nullable = false)
	@Type(type = "numeric_boolean")
	private boolean secured = true;

	/**
	 * Technical service associated to the endpoint
	 */
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_id", updatable = false, nullable = false)
	private Function service;

	public void setCode(String code){
		Matcher matcher = WSEndpoint.basePathPattern.matcher(code);
		if(matcher.matches()) {
			this.code = code;
		} else {
			throw new RuntimeException("invalid code");
		}
	}

	public Function getService() {
		return service;
	}

	public void setService(Function service) {
		this.service = service;
	}

	public boolean isSecured() {
		return secured;
	}

	public void setSecured(boolean secured) {
		this.secured = secured;
	}
	
}
