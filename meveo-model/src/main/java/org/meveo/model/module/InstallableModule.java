/**
 * 
 */
package org.meveo.model.module;

import java.io.Serializable;
import java.util.Map;

public class InstallableModule implements Serializable {

	private static final long serialVersionUID = -5806525342000440081L;

	private String code;
	
	private String version;
	
	private String description;
	
	private String url;
	
	private String commitSha;
	
	private String localRepositorySha;
	
	private String providerCode;
	
	/**
	 * @return the {@link #code}
	 */
	public String getCode() {
		return code;
	}
	/**
	 * @param code the code to set
	 */
	public void setCode(String code) {
		this.code = code;
	}
	/**
	 * @return the {@link #version}
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * @param version the version to set
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	/**
	 * @return the {@link #alreadyInstalled}
	 */
	public boolean isAlreadyInstalled() {
		return localRepositorySha != null;
	}
	
	public boolean hasUpdate() {
		if (localRepositorySha != null && commitSha != null) {
			return !localRepositorySha.equals(commitSha);
		}
		return false;
	}
	/**
	 * @return the {@link #url}
	 */
	public String getUrl() {
		return url;
	}
	/**
	 * @param url the url to set
	 */
	public void setUrl(String url) {
		this.url = url;
	}
	
	/**
	 * @param provider the provider to set
	 */
	public void setProvider(MeveoModuleProvider provider) {
		this.providerCode = provider.getCode();
	}
	
	public String getProviderCode() {
		return providerCode;
	}
	
	/**
	 * @return the {@link #description}
	 */
	public String getDescription() {
		return description;
	}
	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}
	/**
	 * @return the {@link #commitSha}
	 */
	public String getCommitSha() {
		return commitSha;
	}
	/**
	 * @param commitSha the commitSha to set
	 */
	public void setCommitSha(String commitSha) {
		this.commitSha = commitSha;
	}
	
	
	/**
	 * @return the {@link #localRepositorySha}
	 */
	public String getLocalRepositorySha() {
		return localRepositorySha;
	}
	
	/**
	 * @param localRepositorySha the localRepositorySha to set
	 */
	public void setLocalRepositorySha(String localRepositorySha) {
		this.localRepositorySha = localRepositorySha;
	}
	/**
	 * Fill version, code and description fields
	 * 
	 * @param moduleDescriptor Parsed module.json file
	 */
	public void completeFromModuleDescriptor(Map<String, Object> moduleDescriptor) {
		this.version = (String) moduleDescriptor.get("currentVersion");
		this.code = (String) moduleDescriptor.get("code");
		this.description = (String) moduleDescriptor.get("description");
	}
	
	
}
