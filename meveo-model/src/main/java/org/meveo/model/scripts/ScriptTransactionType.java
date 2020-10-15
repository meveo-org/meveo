/**
 * 
 */
package org.meveo.model.scripts;

/**
 * Enum representing the transaction management for the script execution
 * 
 * @author clement.bareth
 * @since 6.12.0
 * @version 6.12.0
 */
public enum ScriptTransactionType {
	
	/** UserTransaction will be injected in script context */
	MANUAL,
	
	/** A new transaction will be started */
	NEW,
	
	/** Caller transaction will be used */
	SAME,
	
	/** Will fail if called in transactional context */
	NONE
}
