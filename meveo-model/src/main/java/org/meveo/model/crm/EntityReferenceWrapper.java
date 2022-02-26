package org.meveo.model.crm;

import java.io.Serializable;
import java.lang.reflect.Method;

import org.meveo.commons.utils.ReflectionUtils;
import org.meveo.model.BusinessEntity;
import org.meveo.model.customEntities.CustomEntityInstance;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Represents a custom field value type - reference to an Meveo entity identified by a classname and code. In case a class is a generic Custom Entity Template a classnameCode is
 * required to identify a concrete custom entity template by its code
 * 
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.8.0
 **/
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class EntityReferenceWrapper implements Serializable {

    private static final long serialVersionUID = -4756870628233941711L;

    /**
     * Classname of an entity
     */
    private String classname;

    /**
     * Custom entity template code - applicable and required when reference is to Custom Entity Template type
     */
    private String classnameCode;

    /**
     * Entity code
     */
    private String code;
    
    private String uuid;
    
    private Long id;
    
    private String repository = "default";

    public EntityReferenceWrapper() {
    	
    }

    public EntityReferenceWrapper(BusinessEntity entity) {
        super();
        if (entity == null) {
            return;
        }
        classname = ReflectionUtils.getCleanClassName(entity.getClass().getName());
        if (entity instanceof CustomEntityInstance) {
            classnameCode = ((CustomEntityInstance) entity).getCetCode();
        }
        code = entity.getCode();
        id = entity.getId();
        
        // Try to retrieve uuid of entity
        try {
	        Method getUuid = entity.getClass().getMethod("getUuid");
	        uuid = (String) getUuid.invoke(entity);
        } catch(Exception ignored) {
        	//NOOP
        }
        
        if(id == null && uuid == null) {
        	uuid = code;
        }
    }

    public EntityReferenceWrapper(String classname, String classnameCode, String code, Long id) {
        this.classname = classname;
        this.classnameCode = classnameCode;
        this.code = code;
        this.id = id;
    }

    @Override
	public String toString() {
		return "EntityReferenceWrapper [classname=" + classname + ", classnameCode=" + classnameCode + ", code=" + code + ", uuid=" + uuid + ", id=" + id + "]";
	}
  
    public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getClassname() {
        return classname;
    }

    public void setClassname(String classname) {
        this.classname = classname;
    }

    public String getClassnameCode() {
        return classnameCode;
    }

    public void setClassnameCode(String classnameCode) {
        this.classnameCode = classnameCode;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public boolean isEmpty() {
        return code == null;
    }

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/**
	 * @return the {@link #repository}
	 */
	public String getRepository() {
		return repository;
	}

	/**
	 * @param repository the repository to set
	 */
	public void setRepository(String repository) {
		this.repository = repository;
	}
}