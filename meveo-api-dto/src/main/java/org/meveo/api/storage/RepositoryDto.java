package org.meveo.api.storage;

import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.model.storage.DataSeparationTypeEnum;
import org.meveo.model.storage.Repository;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 */
public class RepositoryDto extends BusinessEntityDto {

	private static final long serialVersionUID = 57566827213462626L;

	private String parentCode;
	private String binaryStorageConfigurationCode;
	private String neo4jConfigurationCode;
	private String sqlConfigurationCode;
	private DataSeparationTypeEnum dataSeparationType;
	private String path;
	private Boolean forceDelete;

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
}