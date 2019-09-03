package org.meveo.service.storage;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.meveo.model.crm.CustomFieldTemplate;

public class BinaryStorageUtils {

	public static final String INDEX = "index";
	
	private static final Pattern indexPattern = Pattern.compile("(?:\\?|&)" + INDEX + "=(\\d+)(?:&|$)");
	
	public static final boolean filePathContainsEL(CustomFieldTemplate cft) {
		String filePath = cft.getFilePath();
		return filePath != null && (filePath.contains("#{") || filePath.contains("${")) && filePath.contains("}");
	}
	
	public static Integer getIndexFromURI(String uri) {
		Matcher uriMatcher = indexPattern.matcher(uri);
		boolean found = uriMatcher.find();
		
		if(found) {
			return Integer.parseInt(uriMatcher.group(1));
		}
		
		return null;
	}
	
	
	

}
