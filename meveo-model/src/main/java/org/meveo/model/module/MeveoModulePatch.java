package org.meveo.model.module;

import java.io.Serializable;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.meveo.model.customEntities.CustomEntityInstance;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.scripts.ScriptInstance;

/**
 * <p>
 * A patch is basically a script that is tied to a module of a given version
 * range. It can be apply before or after the module's update. For example,
 * before the update a cft must be added first to a cet. Or after update, insert
 * records into the cei.
 * </p>
 * <p>
 * For example patch can when applied can upgrade a module from version 1.0.0 to
 * 2.0.0.
 * </p>
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 * @see MeveoModule
 * @see ScriptInstance
 * @see CustomEntityTemplate
 * @see CustomEntityInstance
 */
@Entity
@Table(name = "meveo_module_patch", uniqueConstraints = @UniqueConstraint(columnNames = { "meveo_module_id", "script_instance_id", "source_version", "target_version" }))
public class MeveoModulePatch implements Comparable<MeveoModulePatch>, Serializable {

	private static final long serialVersionUID = 5944088652978890903L;

	@EmbeddedId
	private MeveoModulePatchId meveoModulePatchId = new MeveoModulePatchId();

	public void setMeveoModulePatchId(MeveoModule meveoModule, ScriptInstance scriptInstance, String sourceVersion, String targetVersion) {

		MeveoModulePatchId patchId = new MeveoModulePatchId();
		patchId.setMeveoModule(meveoModule);
		patchId.setScriptInstance(scriptInstance);
		patchId.setSourceVersion(sourceVersion);
		patchId.setTargetVersion(targetVersion);

		meveoModulePatchId = patchId;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((meveoModulePatchId == null) ? 0 : meveoModulePatchId.hashCode());
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
		MeveoModulePatch other = (MeveoModulePatch) obj;
		if (meveoModulePatchId == null) {
			if (other.meveoModulePatchId != null)
				return false;
		} else if (!meveoModulePatchId.equals(other.meveoModulePatchId))
			return false;
		return true;
	}

	/**
	 * Retrieves the source version from where this patch can be applied as integer.
	 * 
	 * @return source version in integer format
	 */
	public int getSourceVersionAsInt() {
		return Integer.parseInt(meveoModulePatchId.getSourceVersion().replace(".", ""));
	}

	/**
	 * Compares 2 patch for sorting.
	 */
	@Override
	public int compareTo(MeveoModulePatch o) {
		return o.getSourceVersionAsInt() - getSourceVersionAsInt();
	}

	/**
	 * Retrieves the composite primary key.
	 * 
	 * @return the primary key of this entity
	 */
	public MeveoModulePatchId getMeveoModulePatchId() {
		return meveoModulePatchId;
	}

	/**
	 * Sets the primary composite key of this entity
	 * 
	 * @param meveoModulePatchId the composite primary key
	 */
	public void setMeveoModulePatchId(MeveoModulePatchId meveoModulePatchId) {
		this.meveoModulePatchId = meveoModulePatchId;
	}
}
