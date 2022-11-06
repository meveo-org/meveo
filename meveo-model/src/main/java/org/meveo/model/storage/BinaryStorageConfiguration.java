package org.meveo.model.storage;

import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.meveo.model.BaseEntity;
import org.meveo.model.CustomFieldEntity;
import org.meveo.model.ExportIdentifier;
import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.admin.MvCredential;
import org.meveo.model.crm.custom.CustomFieldValues;
import org.meveo.model.persistence.DBStorageType;

/**
 * Configuration used to access a Binary storage repository.
 *
 * @author Edward P. Legaspi
 */
@ExportIdentifier({ "code" })
@Entity
@Table(name = "binary_storage_configuration", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "binary_storage_configuration_seq"), })
@CustomFieldEntity(cftCodePrefix = "FILE_SYSTEM")
public class BinaryStorageConfiguration extends BaseEntity implements IStorageConfiguration {

	private static final long serialVersionUID = -1378468359266231255L;

	/**
	 * Code of the configuration
	 */
	@Column(name = "code", length = 255)
	@Size(max = 255)
	private String code;

	/**
	 * Root path where the binary will be stored
	 */
	@Column(name = "root_path", length = 255, nullable = false, unique = true)
	@Size(max = 255)
	@NotNull
	private String rootPath;

	public String getCode() {
		return code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public String getRootPath() {
		return rootPath;
	}

	public void setRootPath(String rootPath) {
		this.rootPath = rootPath;
	}

	@Override
	public String getUuid() {
		return this.code;
	}

	@Override
	public String clearUuid() {
		return null;
	}

	@Override
	public ICustomFieldEntity[] getParentCFEntities() {
		return null;
	}

	@Override
	public CustomFieldValues getCfValues() {
		return null;
	}

	@Override
	public CustomFieldValues getCfValuesNullSafe() {
		return null;
	}

	@Override
	public void clearCfValues() {
		
	}

	@Override
	public MvCredential getCredential() {
		return null;
	}

	@Override
	public String getProtocol() {
		return null;
	}

	@Override
	public String getHostname() {
		return null;
	}

	@Override
	public Integer getPort() {
		return null;
	}

	@Override
	public String getConnectionUri() {
		return null;
	}

	@Override
	public DBStorageType getDbStorageType() {
		return DBStorageType.FILE_SYSTEM;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Objects.hash(code);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == null) {
			return false;
		}
		
		if (this == obj)
			return true;
		if (getClass() != obj.getClass())
			return false;
		BinaryStorageConfiguration other = (BinaryStorageConfiguration) obj;
		return Objects.equals(code, other.code);
	}
	
}
