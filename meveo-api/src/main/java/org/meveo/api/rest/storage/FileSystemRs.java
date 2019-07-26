package org.meveo.api.rest.storage;

/**
 * @author Edward P. Legaspi <czetsuya@gmail.com>
 */
public interface FileSystemRs {

	/**
	 * Retrieve a binary from a repository
	 * 
	 * @param repositoryCode storage
	 * @param cetCode        custom entity code
	 * @param uuid           entity id
	 * @param cftCode        custom field template code
	 */
	void findBinary(String repositoryCode, String cetCode, String uuid, String cftCode);
}
