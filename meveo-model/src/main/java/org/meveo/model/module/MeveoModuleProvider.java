/**
 * 
 */
package org.meveo.model.module;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.meveo.model.BusinessEntity;

@Entity(name =  "MeveoModuleProvider")
@Table(name = "meveo_module_provider")
public class MeveoModuleProvider extends BusinessEntity {

	private static final long serialVersionUID = -1063431329539010077L;
	
	@Column(name = "provider_name")
	private String providerName;
	
	@Column(name = "provider_url")
	private String providerUrl;
	
	@Column(name = "access_token")
	private String accessToken;
	
	@Column(name = "ssh_key")
	private String sshKey;
	
	@Enumerated(EnumType.STRING)
	@Column(name = "provider_type")
	private MeveoModuleProviderType providerType;
	
	/**
	 * @return the {@link #accessToken}
	 */
	public String getAccessToken() {
		return accessToken;
	}
	/**
	 * @param accessToken the accessToken to set
	 */
	public void setAccessToken(String accessToken) {
		this.accessToken = accessToken;
	}
	/**
	 * @return the {@link #sshKey}
	 */
	public String getSshKey() {
		return sshKey;
	}
	/**
	 * @param sshKey the sshKey to set
	 */
	public void setSshKey(String sshKey) {
		this.sshKey = sshKey;
	}
	/**
	 * @return the {@link #providerType}
	 */
	public MeveoModuleProviderType getProviderType() {
		return providerType;
	}
	/**
	 * @param providerType the providerType to set
	 */
	public void setProviderType(MeveoModuleProviderType providerType) {
		this.providerType = providerType;
	}
	/**
	 * @return the {@link #providerName}
	 */
	public String getProviderName() {
		return providerName;
	}
	/**
	 * @param providerName the providerName to set
	 */
	public void setProviderName(String providerName) {
		this.providerName = providerName;
	}
	/**
	 * @return the {@link #providerUrl}
	 */
	public String getProviderUrl() {
		return providerUrl;
	}
	/**
	 * @param providerUrl the providerUrl to set
	 */
	public void setProviderUrl(String providerUrl) {
		this.providerUrl = providerUrl;
	}
}
