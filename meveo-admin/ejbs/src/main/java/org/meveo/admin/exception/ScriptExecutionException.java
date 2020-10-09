package org.meveo.admin.exception;

/** Exception thrown when the execution of a script fails 
 * The original error is available in the cause of the new exception*/ 
public class ScriptExecutionException extends BusinessException {
	/** */
	private static final long serialVersionUID = 1L;
	
	/**
	 * @param Name of the script
	 * @param method executed
	 * @param e exception
	 */
	public ScriptExecutionException(String script, Throwable e) {
		this(script, null, e);
	}
	/**
	 * @param Name of the script
	 * @param method executed
	 * @param e exception
	 */
	public ScriptExecutionException(String script, String method, Throwable e) {
		super(e.getClass().getSimpleName() + " in script " + script + (method!= null ? "#"+method : "") + getLineInformation(script, e) + " : " + e.getMessage(), e);
	} 

	private static String getLineInformation(String script, Throwable e) {
		String firstError = "";
		try {
			firstError =  " (thrown at " + e.getStackTrace()[0].toString() + ")";
			if (script == null || script.equals(e.getStackTrace()[0].getClassName())) {
				return firstError;
			} else {
				for (StackTraceElement stack : e.getStackTrace()) {
					if (script.equals(stack.getClassName())) {
						return " (stacked at " + stack.toString() + ")";
					}
				}
			}
		} catch (Throwable e1) {
		}
		return firstError;
		

	}

}
