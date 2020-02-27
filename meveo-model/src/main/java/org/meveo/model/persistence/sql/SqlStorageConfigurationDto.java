package org.meveo.model.persistence.sql;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.6.0
 */
public class SqlStorageConfigurationDto {

	private boolean storeAsTable = false;

	public SqlStorageConfigurationDto() {
		
	}
	
	public SqlStorageConfigurationDto(SQLStorageConfiguration e) {

		storeAsTable = e.isStoreAsTable();
	}

	public boolean isStoreAsTable() {
		return storeAsTable;
	}

	public void setStoreAsTable(boolean storeAsTable) {
		this.storeAsTable = storeAsTable;
	}
}
