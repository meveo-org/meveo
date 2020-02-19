package org.meveo.model.crm.custom;

import java.lang.reflect.Type;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since
 * @version
 */
public class DbField {

	private String name;
	private Type type;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}
}
