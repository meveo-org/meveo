package org.meveo.api.storage;

import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.model.storage.DataSeparationTypeEnum;
import org.meveo.model.storage.Repository;

/**
 * @author Edward P. Legaspi
 */
public class RepositoryDto extends BusinessEntityDto {

	private static final long serialVersionUID = 57566827213462626L;

	private String parentCode;
	private String binaryStorageConfigurationCode;
	private String neo4jConfigurationCode;
	private DataSeparationTypeEnum dataSeparationType;

	public RepositoryDto() {

	}

	public RepositoryDto(Repository e) {
		if (e.getParentRepository() != null) {
			parentCode = e.getParentRepository().getCode();
		}
		if (e.getBinaryStorageConfiguration() != null) {
			binaryStorageConfigurationCode = e.getBinaryStorageConfiguration().getCode();
		}
		if (e.getNeo4jConfiguration() != null) {
			neo4jConfigurationCode = e.getNeo4jConfiguration().getCode();
		}
		dataSeparationType = e.getDataSeparationType();
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
}