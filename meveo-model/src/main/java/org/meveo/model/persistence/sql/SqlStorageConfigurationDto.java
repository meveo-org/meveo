package org.meveo.model.persistence.sql;

/**
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @version 6.6.0
 */
public class SqlStorageConfigurationDto {

	private boolean storeAsTable = false;
	private String sqlConfigurationCode;

	public SqlStorageConfigurationDto() {
		
	}
	
	public SqlStorageConfigurationDto(SQLStorageConfiguration e) {

		storeAsTable = e.isStoreAsTable();
		sqlConfigurationCode = e.getSqlConfigurationCode();
	}

	public boolean isStoreAsTable() {
		return storeAsTable;
	}

	public void setStoreAsTable(boolean storeAsTable) {
		this.storeAsTable = storeAsTable;
	}

	public String getSqlConfigurationCode() {
		return sqlConfigurationCode;
	}

	public void setSqlConfigurationCode(String sqlConfigurationCode) {
		this.sqlConfigurationCode = sqlConfigurationCode;
	}
}
