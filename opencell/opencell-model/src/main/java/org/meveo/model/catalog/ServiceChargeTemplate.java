package org.meveo.model.catalog;

import org.meveo.model.BaseEntity;

import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;
import java.util.List;

@MappedSuperclass
public abstract class ServiceChargeTemplate<T extends ChargeTemplate> extends BaseEntity {

	private static final long serialVersionUID = -1872859127097329926L;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "service_template_id")
	private ServiceTemplate serviceTemplate;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "charge_template_id")
	private T chargeTemplate;

	
	public ServiceTemplate getServiceTemplate() {
		return serviceTemplate;
	}

	public void setServiceTemplate(ServiceTemplate serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}

	public T getChargeTemplate() {
		return chargeTemplate;
	}

	public void setChargeTemplate(T chargeTemplate) {
		this.chargeTemplate = chargeTemplate;
	}

	public abstract List<WalletTemplate> getWalletTemplates();

	public abstract void setWalletTemplates(List<WalletTemplate> walletTemplates);
	

}
