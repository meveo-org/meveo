package org.meveo.model.module;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

import org.meveo.model.scripts.ScriptInstance;

/**
 * This is the primary composite key of the module patch.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 * @see MeveoModulePatch
 */
@Embeddable
public class MeveoModulePatchId implements Serializable {

	private static final long serialVersionUID = 6526445886542706917L;

	/**
	 * The meveo module to which this patch key is linked.
	 */
	@NotNull
	@ManyToOne
	@JoinColumn(name = "meveo_module_id")
	private MeveoModule meveoModule;

	/**
	 * This is the module source version to which this patch can be applied.
	 */
	@NotNull
	@Column(name = "source_version", length = 25)
	@Pattern(regexp = MeveoModule.VERSION_PATTERN)
	private String sourceVersion;

	/**
	 * This is the target version to which this patch can update a module.
	 */
	@NotNull
	@Column(name = "target_version", length = 25)
	@Pattern(regexp = MeveoModule.VERSION_PATTERN)
	private String targetVersion;

	/**
	 * The script that will be executed when this patch is applied.
	 */
	@NotNull
	@ManyToOne
	@JoinColumn(name = "script_instance_id")
	private ScriptInstance scriptInstance;

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((meveoModule == null) ? 0 : meveoModule.hashCode());
		result = prime * result + ((scriptInstance == null) ? 0 : scriptInstance.hashCode());
		result = prime * result + ((sourceVersion == null) ? 0 : sourceVersion.hashCode());
		result = prime * result + ((targetVersion == null) ? 0 : targetVersion.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		MeveoModulePatchId other = (MeveoModulePatchId) obj;
		if (meveoModule == null) {
			if (other.meveoModule != null)
				return false;
		} else if (!meveoModule.equals(other.meveoModule))
			return false;
		if (scriptInstance == null) {
			if (other.scriptInstance != null)
				return false;
		} else if (!scriptInstance.equals(other.scriptInstance))
			return false;
		if (sourceVersion == null) {
			if (other.sourceVersion != null)
				return false;
		} else if (!sourceVersion.equals(other.sourceVersion))
			return false;
		if (targetVersion == null) {
			if (other.targetVersion != null)
				return false;
		} else if (!targetVersion.equals(other.targetVersion))
			return false;
		return true;
	}

	/**
	 * Retrieves the meveo module to which this patch is linked.
	 * 
	 * @return the meveo module
	 */
	public MeveoModule getMeveoModule() {
		return meveoModule;
	}

	/**
	 * Sets the meveo module to which this patch is link.
	 * 
	 * @param meveoModule the meveo module
	 */
	public void setMeveoModule(MeveoModule meveoModule) {
		this.meveoModule = meveoModule;
	}

	/**
	 * Retrieves the script that must be executed when this patch is applied.
	 * 
	 * @return the script instance
	 */
	public ScriptInstance getScriptInstance() {
		return scriptInstance;
	}

	/**
	 * Sets the script that will be executed when this patch is applied.
	 * 
	 * @param scriptInstance the script instance
	 */
	public void setScriptInstance(ScriptInstance scriptInstance) {
		this.scriptInstance = scriptInstance;
	}

	/**
	 * Retrieves the source version where this patch can be applied.
	 * 
	 * @return the source version
	 */
	public String getSourceVersion() {
		return sourceVersion;
	}

	/**
	 * Sets the source version where this patch can be applied
	 * 
	 * @param sourceVersion the source version
	 */
	public void setSourceVersion(String sourceVersion) {
		this.sourceVersion = sourceVersion;
	}

	/**
	 * Retrieves the target version up to where this patch can be applied.
	 * 
	 * @return the target version
	 */
	public String getTargetVersion() {
		return targetVersion;
	}

	/**
	 * Sets the target version up to where this patch can be applied.
	 * 
	 * @param targetVersion the target version
	 */
	public void setTargetVersion(String targetVersion) {
		this.targetVersion = targetVersion;
	}
}
