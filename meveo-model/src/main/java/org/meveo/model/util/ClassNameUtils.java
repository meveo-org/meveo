/**
 * 
 */
package org.meveo.model.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClassNameUtils {
	
	public static String getPackageName(String className) {
		Matcher matcher = Pattern.compile("(.*)\\.").matcher(className);
		if (matcher.find()) {
			return matcher.group(1);
		}
		return null;
	}
}
