/**
 * 
 */
package org.meveo.model.storage;

import org.meveo.model.ICustomFieldEntity;
import org.meveo.model.admin.MvCredential;
import org.meveo.model.persistence.DBStorageType;

public interface IStorageConfiguration extends ICustomFieldEntity {

	MvCredential getCredential();

	String getProtocol();
	
	String getHostname();

	Integer getPort();

	String getConnectionUri();
	
	DBStorageType getDbStorageType();
	
	String getCode();
	
}
