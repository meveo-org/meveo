package org.meveo.admin.exception;

public class IllegalTransitionException extends BusinessException {

	private static final long serialVersionUID = -646961659473697584L;

	private String from;
	private String to;
	private String field;
	
	/**
	 * Instantiates a new IllegalTransitionException
	 *
	 * @param from the initial state
	 * @param to the final state
	 * @param field the concerned field
	 */
	public IllegalTransitionException(String from, String to, String field) {
		super(String.format("Illegal transition for % from % to %", field, from, to));
		this.from = from;
		this.to = to;
		this.field = field;
	}

	/**
	 * @return the {@link #from}
	 */
	public String getFrom() {
		return from;
	}

	/**
	 * @return the {@link #to}
	 */
	public String getTo() {
		return to;
	}

	/**
	 * @return the {@link #field}
	 */
	public String getField() {
		return field;
	}
	
}
