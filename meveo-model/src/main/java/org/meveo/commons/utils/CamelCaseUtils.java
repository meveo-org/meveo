/**
 * 
 */
package org.meveo.commons.utils;

import java.util.regex.Pattern;

public class CamelCaseUtils {
	
	private static Pattern SEPARATORS = Pattern.compile("[_.\\-\\s]+");
    
	public static String camelCase(String input, boolean preserveUpper) {
		input = input.trim();
		
		String result = "";
		for (int i = 0; i < input.length(); i++) {
			char currentChar = input.charAt(i);
			boolean toLowerCase = true;
			
			if (i + 1 < input.length()) {
				char nextChar = input.charAt(i + 1);
				if (SEPARATORS.matcher(String.valueOf(nextChar)).matches()) {
					// Set current char to lower is next char is a separator
					toLowerCase = true;
				} else if (Character.isUpperCase(currentChar) && !Character.isUpperCase(nextChar)) {
					// Leave current char as upper if next character is lower
					toLowerCase = !preserveUpper;
				}
			}
			
			if (i > 0) {
				String previousChar = String.valueOf(input.charAt(i - 1));
				// Set current char to upper if previous character was a separator
				if (SEPARATORS.matcher(previousChar).matches()) {
					toLowerCase = false;
				}
			} else {
				// Always set first char to lower
				toLowerCase = true;
			}
			
			// Do not write separator characters
			if (!SEPARATORS.matcher(String.valueOf(currentChar)).matches()) {
				if (toLowerCase) {
					result += Character.toLowerCase(currentChar);
				} else {
					result += Character.toUpperCase(currentChar); 
				}
			}
		}
		
		return result;
	}
	
}
