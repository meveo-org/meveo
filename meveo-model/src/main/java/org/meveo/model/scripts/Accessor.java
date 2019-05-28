package org.meveo.model.scripts;

import java.io.Serializable;

public class Accessor implements Serializable {

    /**
     * Name of the property
     */
    private String name;

    /**
     * Type of the property
     */
    private String type;

    /**
     * Description of the property
     */
    private String description;

    /**
     * Name of the getter or setter
     */
    private String methodName;
    
    public String getMethodName() {
		return methodName;
	}

	public void setMethodName(String methodName) {
		this.methodName = methodName;
	}

	public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
