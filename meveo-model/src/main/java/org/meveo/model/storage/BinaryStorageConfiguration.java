package org.meveo.model.storage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

import org.hibernate.annotations.GenericGenerator;
import org.meveo.model.BaseEntity;
import org.meveo.model.ExportIdentifier;

/**
 * Configuration used to access a Binary storage repository.
 *
 * @author Edward P. Legaspi
 */
@ExportIdentifier({ "code" })
@Entity
@Table(name = "binary_storage_configuration", uniqueConstraints = @UniqueConstraint(columnNames = { "code" }))
@GenericGenerator(name = "ID_GENERATOR", strategy = "org.hibernate.id.enhanced.SequenceStyleGenerator", parameters = { @org.hibernate.annotations.Parameter(name = "sequence_name", value = "binary_storage_configuration_seq"), })
public class BinaryStorageConfiguration extends BaseEntity {

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
}
