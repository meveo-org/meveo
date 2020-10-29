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
	MANUAL("Manual"),
	
	/** A new transaction will be started */
	NEW("New transaction"),
	
	/** Caller transaction will be used */
	SAME("Same transaction"),
	
	/** Will fail if called in transactional context */
	NONE("No transaction");
	
	/** Label of the enum value*/
	private String label;

	/**
	 * Instantiates a new ScriptTransactionType
	 *
	 * @param label
	 */
	private ScriptTransactionType(String label) {
		this.label = label;
	}

	/**
	 * @return the {@link #label}
	 */
	public String getLabel() {
		return label;
	}
	
}
