package org.meveo.api.storage;

import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.model.storage.BinaryStorageConfiguration;
import org.meveo.model.storage.DataSeparationTypeEnum;
import org.meveo.model.storage.Repository;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * A repository is a data storage. Supported storages are database and local
 * folder. If storage is a database, it will load the respective database
 * configuration. For local folder it uses the configuration in
 * {@link BinaryStorageConfiguration} on where to save a file.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.12
 */
@ApiModel
public class RepositoryDto extends BusinessEntityDto {

	private static final long serialVersionUID = 57566827213462626L;

	private String sqlConfigurationCode;

	/**
	 * Code of the parent
	 */
	@ApiModelProperty("Code of the parent")
	private String parentCode;

	/**
	 * Code of the binary storage configuration
	 */
	@ApiModelProperty("Code of the binary storage configuration")
	private String binaryStorageConfigurationCode;

	/**
	 * Code of the neo4j configuration
	 */
	@ApiModelProperty("Code of the neo4j configuration")
	private String neo4jConfigurationCode;

	/**
	 * Data separation type
	 */
	@ApiModelProperty("Data separation type")
	private DataSeparationTypeEnum dataSeparationType;

	/**
	 * Physical path
	 */
	@ApiModelProperty("Physical ath")
	private String path;

	/**
	 * Whether to delete the children of the repository
	 */
	@ApiModelProperty("Whether to delete the children of the repository")
	private Boolean forceDelete;

	@ApiModelProperty("User hierarchy level")
	private String userHierarchyLevelCode;

	public RepositoryDto() {

	}

	public RepositoryDto(Repository e) {
		super(e);
		if (e.getParentRepository() != null) {
			parentCode = e.getParentRepository().getCode();
		}
		if (e.getBinaryStorageConfiguration() != null) {
			binaryStorageConfigurationCode = e.getBinaryStorageConfiguration().getCode();
		}
		if (e.getNeo4jConfiguration() != null) {
			neo4jConfigurationCode = e.getNeo4jConfiguration().getCode();
		}
		if (e.getSqlConfiguration() != null) {
			sqlConfigurationCode = e.getSqlConfiguration().getCode();
		}
		if(e.getUserHierarchyLevel() != null && e.getUserHierarchyLevel().getCode() != null) {
			userHierarchyLevelCode = e.getUserHierarchyLevel().getCode();
		}
		dataSeparationType = e.getDataSeparationType();
		path = e.getPath();
	}

	public String getParentCode() {
		return parentCode;
	}

	public void setParentCode(String parentCode) {
		this.parentCode = parentCode;
	}

	public String getBinaryStorageConfigurationCode() {
		return binaryStorageConfigurationCode;
	}

	public void setBinaryStorageConfigurationCode(String binaryStorageConfigurationCode) {
		this.binaryStorageConfigurationCode = binaryStorageConfigurationCode;
	}

	public String getNeo4jConfigurationCode() {
		return neo4jConfigurationCode;
	}

	public void setNeo4jConfigurationCode(String neo4jConfigurationCode) {
		this.neo4jConfigurationCode = neo4jConfigurationCode;
	}

	public DataSeparationTypeEnum getDataSeparationType() {
		return dataSeparationType;
	}

	public void setDataSeparationType(DataSeparationTypeEnum dataSeparationType) {
		this.dataSeparationType = dataSeparationType;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Boolean getForceDelete() {
		return forceDelete;
	}

	public void setForceDelete(Boolean forceDelete) {
		this.forceDelete = forceDelete;
	}

	public String getSqlConfigurationCode() {
		return sqlConfigurationCode;
	}

	public void setSqlConfigurationCode(String sqlConfigurationCode) {
		this.sqlConfigurationCode = sqlConfigurationCode;
	}

	public String getUserHierarchyLevelCode() {
		return userHierarchyLevelCode;
	}

	public void setUserHierarchyLevelCode(String userHierarchyLevelCode) {
		this.userHierarchyLevelCode = userHierarchyLevelCode;
	}
}