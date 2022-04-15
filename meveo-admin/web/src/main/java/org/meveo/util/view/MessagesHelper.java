/**
 * 
 */
package org.meveo.util.view;
import org.jboss.seam.international.status.Messages;

public class MessagesHelper {
	
	/**
	 * Display the error on screen and returns null
	 * 
	 * @param messages the messages bean
	 * @param e the exception that occured
	 * @return null
	 */
	public static String error(Messages messages, Exception e) {
		String message = e.getLocalizedMessage();
		if (e.getCause() != null) {
			message += " : " + e.getCause().getLocalizedMessage();
		}
		messages.error(message);
		return null;
	}
}
