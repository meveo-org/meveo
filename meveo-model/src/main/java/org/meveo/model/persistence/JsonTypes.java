/**
 * 
 */
package org.meveo.model.persistence;

/**
 * Enum representing the different JSON type of storage for use in SQL by hibernate
 * 
 * @author clement.bareth
 * @since 6.15.0
 * @version 6.15.0
 */
public final class JsonTypes {
	
    /**
     * JSON Base object serialisation, such as map or a POJO
     */
	public static final String JSON = "json";
	
	/**
	 * Binary serialization
	 */
	public static final String JSON_B = "jsonb";
	
	/**
	 * Json list serialization
	 */
	public static final String JSON_LIST = "jsonList";
	
	/**
	 * JSON set serialization
	 */
	public static final String JSON_SET = "jsonSet";
	
}
