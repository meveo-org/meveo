/**
 *
 */
package org.meveo.api.dto;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import org.meveo.model.admin.MvCredential;
import org.meveo.model.admin.MvCredential.AuthenticationType;

public class MvCredentialDto extends CFBusinessEntityDto {

	private static final long serialVersionUID = -7880581224489746541L;

	private String apiKey;

	private String headerValue;

	private String publicKey;

	private Instant tokenExpiry;

	private Instant lastConnection;

	private String token;

	private String privateKey;

	private String password;

	private String domainName;

	private AuthenticationType authenticationType;

	private String headerKey;

	private Long credit;

	private String refreshToken;

	private String status;

	private String username;

	private Map<String, String> extraParameters = new HashMap<>();

	public MvCredentialDto() {

	}

	public MvCredentialDto(MvCredential credential) {
		this.apiKey = credential.getApiKey();
		this.authenticationType = credential.getAuthenticationType();
		this.code = credential.getCode();
		this.credit = credential.getCredit();
		this.description = credential.getDescription();
		this.domainName = credential.getDomainName();
		this.headerKey = credential.getHeaderKey();
		this.headerValue = credential.getHeaderValue();
		this.lastConnection = credential.getLastConnection();
		this.password = credential.getPassword();
		this.privateKey = credential.getPrivateKey();
		this.publicKey = credential.getPublicKey();
		this.refreshToken = credential.getRefreshToken();
		this.status = credential.getStatus();
		this.token = credential.getToken();
		this.tokenExpiry = credential.getTokenExpiry();
		this.username = credential.getUsername();
		this.extraParameters = credential.getExtraParameters();
	}

	/**
	 * @return the {@link #apiKey}
	 */
	public String getApiKey() {
		return apiKey;
	}

	/**
	 * @param apiKey the apiKey to set
	 */
	public void setApiKey(String apiKey) {
		this.apiKey = apiKey;
	}

	/**
	 * @return the {@link #headerValue}
	 */
	public String getHeaderValue() {
		return headerValue;
	}

	/**
	 * @param headerValue the headerValue to set
	 */
	public void setHeaderValue(String headerValue) {
		this.headerValue = headerValue;
	}

	/**
	 * @return the {@link #publicKey}
	 */
	public String getPublicKey() {
		return publicKey;
	}

	/**
	 * @param publicKey the publicKey to set
	 */
	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	/**
	 * @return the {@link #tokenExpiry}
	 */
	public Instant getTokenExpiry() {
		return tokenExpiry;
	}

	/**
	 * @param tokenExpiry the tokenExpiry to set
	 */
	public void setTokenExpiry(Instant tokenExpiry) {
		this.tokenExpiry = tokenExpiry;
	}

	/**
	 * @return the {@link #lastConnection}
	 */
	public Instant getLastConnection() {
		return lastConnection;
	}

	/**
	 * @param lastConnection the lastConnection to set
	 */
	public void setLastConnection(Instant lastConnection) {
		this.lastConnection = lastConnection;
	}

	/**
	 * @return the {@link #token}
	 */
	public String getToken() {
		return token;
	}

	/**
	 * @param token the token to set
	 */
	public void setToken(String token) {
		this.token = token;
	}

	/**
	 * @return the {@link #privateKey}
	 */
	public String getPrivateKey() {
		return privateKey;
	}

	/**
	 * @param privateKey the privateKey to set
	 */
	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
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
	 * @return the {@link #domainName}
	 */
	public String getDomainName() {
		return domainName;
	}

	/**
	 * @param domainName the domainName to set
	 */
	public void setDomainName(String domainName) {
		this.domainName = domainName;
	}

	/**
	 * @return the {@link #authenticationType}
	 */
	public AuthenticationType getAuthenticationType() {
		return authenticationType;
	}

	/**
	 * @param authenticationType the authenticationType to set
	 */
	public void setAuthenticationType(AuthenticationType authenticationType) {
		this.authenticationType = authenticationType;
	}

	/**
	 * @return the {@link #headerKey}
	 */
	public String getHeaderKey() {
		return headerKey;
	}

	/**
	 * @param headerKey the headerKey to set
	 */
	public void setHeaderKey(String headerKey) {
		this.headerKey = headerKey;
	}

	/**
	 * @return the {@link #credit}
	 */
	public Long getCredit() {
		return credit;
	}

	/**
	 * @param credit the credit to set
	 */
	public void setCredit(Long credit) {
		this.credit = credit;
	}

	/**
	 * @return the {@link #refreshToken}
	 */
	public String getRefreshToken() {
		return refreshToken;
	}

	/**
	 * @param refreshToken the refreshToken to set
	 */
	public void setRefreshToken(String refreshToken) {
		this.refreshToken = refreshToken;
	}

	/**
	 * @return the {@link #status}
	 */
	public String getStatus() {
		return status;
	}

	/**
	 * @param status the status to set
	 */
	public void setStatus(String status) {
		this.status = status;
	}

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
	 * @return the {@link #extraParameters}
	 */
	public Map<String, String> getExtraParameters() {
		return extraParameters;
	}

	/**
	 * @param extraParameters the extraParameters to set
	 */
	public void setExtraParameters(Map<String, String> extraParameters) {
		this.extraParameters = extraParameters;
	}

}
