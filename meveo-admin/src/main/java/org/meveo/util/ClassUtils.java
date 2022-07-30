package org.meveo.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Utility class for Class.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.8.0
 * @version 6.8.0
 */
public class ClassUtils {

	private ClassUtils() {

	}

	/**
	 * Java native data types.
	 */
	private static final List<String> primitiveDataType = new ArrayList<>();
	private static final List<String> primitiveWrapperDataType = new ArrayList<>();

	static {
		primitiveDataType.add("byte");
		primitiveDataType.add("short");
		primitiveDataType.add("int");
		primitiveDataType.add("long");
		primitiveDataType.add("float");
		primitiveDataType.add("double");
		primitiveDataType.add("char");
		primitiveDataType.add("boolean");
	}

	static {
		primitiveWrapperDataType.add("Boolean");
		primitiveWrapperDataType.add("Byte");
		primitiveWrapperDataType.add("Character");
		primitiveWrapperDataType.add("Short");
		primitiveWrapperDataType.add("Integer");
		primitiveWrapperDataType.add("Long");
		primitiveWrapperDataType.add("Double");
		primitiveWrapperDataType.add("Float");
		primitiveWrapperDataType.add("Void");
	}

	public static boolean isPrimitiveOrWrapperType(String key) {
		return primitiveDataType.contains(key) || primitiveWrapperDataType.contains(key);
	}
}
