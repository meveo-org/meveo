/**
 * 
 */
package org.meveo.api.dto.module;

import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.model.module.MeveoModuleProviderType;

public class MeveoModuleProviderDto extends BusinessEntityDto {

	private String username;
	private String password;
	private String accessToken;
	private String gpgKey;
	private String sshKey;
	private MeveoModuleProviderType providerType;
	/**
	 * @return the {@link #username}
	 */
	public String getUsername() {
		return username;
	}
	/**
	 * @param username the username to set
	 */
	public void setUsername(String username) {
		this.username = username;
	}
	/**
	 * @return the {@link #password}
	 */
	public String getPassword() {
		return password;
	}
	/**
	 * @param password the password to set
	 */
	public void setPassword(String password) {
		this.password = password;
	}
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
	 * @return the {@link #gpgKey}
	 */
	public String getGpgKey() {
		return gpgKey;
	}
	/**
	 * @param gpgKey the gpgKey to set
	 */
	public void setGpgKey(String gpgKey) {
		this.gpgKey = gpgKey;
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
}
