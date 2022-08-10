package org.meveo.api.rest;

import javax.ws.rs.core.MultivaluedMap;

import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.plugins.providers.multipart.InputPart;

public class RestUtils {
	
	/**
	 * Extract the filename of an multipart input part
	 * 
	 * @param inputPart InputPart holding the file
	 * @return the filename if Content-Disposition header exists, or null.
	 */
	public static String getFileName(InputPart inputPart) {
		MultivaluedMap<String, String> headers = inputPart.getHeaders();
		String disposition = headers.get("Content-Disposition").get(0);

		if (StringUtils.isEmpty(disposition)) {
			return null;
		}

		return disposition.replaceFirst("(?i)^.*filename=\"?([^\"]+)\"?.*$", "$1");
	}

}
