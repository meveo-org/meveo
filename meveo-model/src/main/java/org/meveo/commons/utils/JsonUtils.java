package org.meveo.commons.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

/**
 * Json utility functions.
 *
 * @author Edward P. Legaspi | <czetsuya@gmail.com>
 * @since 6.9.0
 * @version 6.9.0
 */
public class JsonUtils {

	private static Gson gson = null;

	private JsonUtils() {

	}

	static {
		GsonBuilder builder = new GsonBuilder();
		builder.setVersion(1.0);
		gson = builder.registerTypeAdapter(java.util.Date.class, new MillisDate()).disableHtmlEscaping().create();
	}

	public static String toJson(Object object, boolean prettyPrint) {
		if (object == null) {
			return "";
		}
		if (!prettyPrint) {
			return gson.toJson(object);
		} else {
			GsonBuilder builder = new GsonBuilder();
			builder.setVersion(1.0);
			Gson gsonPP = builder.registerTypeAdapter(java.util.Date.class, new MillisDate()).setPrettyPrinting().disableHtmlEscaping().create();
			return gsonPP.toJson(object);
		}
	}

	public static <T> T toObject(String jsonString, Class<T> clazz) {
		if (jsonString == null) {
			return null;
		}
		return gson.fromJson(jsonString, clazz);
	}

	public static String beautifyString(String uglyJsonString) {

		Gson gson = new GsonBuilder().setPrettyPrinting().create();
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(uglyJsonString);

		return gson.toJson(je);
	}
}