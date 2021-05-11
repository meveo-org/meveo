package org.meveo.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

import org.meveo.model.persistence.JacksonUtil;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;

import io.swagger.annotations.ApiModel;

/**
 * Represents a single CF simple value inside a more complex CF value (list, map, matrix).
 *
 * @author Andrius Karpavicius
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlRootElement(name = "CustomFieldValue")
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class CustomFieldValueDto implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6551785257592739335L;

    @XmlAttribute
    private Date dateValue;
    
    @XmlAttribute
    private Double doubleValue;
    
    @XmlAttribute
    private Long longValue;
    
    @XmlAttribute
    private String stringValue;
    
    @XmlAttribute
    private EntityReferenceDto entityReferenceValue;
    
    @XmlAttribute
    private CustomEntityInstanceDto childEntityValue;
    
    @XmlAttribute
    private Object objectValue;

    /**
     * Instantiates a new custom field value dto.
     */
    public CustomFieldValueDto() {
    }

    /**
     * From DTO.
     *
     * @return the object
     */
    private Object fromDTO() {

        if (getValue() instanceof EntityReferenceDto) {
            return ((EntityReferenceDto) getValue()).fromDTO();
        } else {
            return getValue();
        }
    }

    /**
     * From DTO.
     *
     * @param listValue the list value
     * @return the list
     */
    public static List<Object> fromDTO(List<CustomFieldValueDto> listValue) {
        List<Object> values = new ArrayList<Object>();
        for (CustomFieldValueDto valueDto : listValue) {
            values.add(valueDto.fromDTO());
        }
        return values;
    }

    /**
     * From DTO.
     *
     * @param mapValue the map value
     * @return the linked hash map
     */
    public static LinkedHashMap<String, Object> fromDTO(Map<String, CustomFieldValueDto> mapValue) {
        LinkedHashMap<String, Object> values = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, CustomFieldValueDto> valueDto : mapValue.entrySet()) {
            values.put(valueDto.getKey(), valueDto.getValue().fromDTO());
        }
        return values;
    }

    /**
     * Instantiates a new custom field value dto.
     *
     * @param object the CustomFieldValue
     */
    public CustomFieldValueDto(Object object) {
    	setValue(object);
    }

    /**
     * Check if value is empty.
     *
     * @return True if value is empty
     */
    public boolean isEmpty() {
        if (getValue() == null) {
            return true;
        }
        if (getValue() instanceof EntityReferenceDto) {
            return ((EntityReferenceDto) getValue()).isEmpty();
        } else if (getValue() instanceof String) {
            return ((String) getValue()).length() == 0;
        }
        return false;
    }

    /**
     * Gets the value.
     *
     * @return the value
     */
    @JsonIgnore // Don't use this for jackson serialization
    public Object getValue() {
    	return Stream.of(dateValue, longValue, doubleValue, stringValue, entityReferenceValue, childEntityValue, objectValue)
    		.filter(Objects::nonNull)
    		.findFirst()
    		.orElse(null);
    }
    
    @Override
    public String toString() {
        return String.format("CustomFieldValueDto [value=%s]", getValue());
    }

	/**
	 * @return the {@link #dateValue}
	 */
	public Date getDateValue() {
		return dateValue;
	}

	/**
	 * @param dateValue the dateValue to set
	 */
	public void setDateValue(Date dateValue) {
		this.dateValue = dateValue;
	}

	/**
	 * @return the {@link #doubleValue}
	 */
	public Double getDoubleValue() {
		return doubleValue;
	}

	/**
	 * @param doubleValue the doubleValue to set
	 */
	public void setDoubleValue(Double doubleValue) {
		this.doubleValue = doubleValue;
	}

	/**
	 * @return the {@link #longValue}
	 */
	public Long getLongValue() {
		return longValue;
	}

	/**
	 * @param longValue the longValue to set
	 */
	public void setLongValue(Long longValue) {
		this.longValue = longValue;
	}

	/**
	 * @return the {@link #stringValue}
	 */
	public String getStringValue() {
		return stringValue;
	}

	/**
	 * @param stringValue the stringValue to set
	 */
	public void setStringValue(String stringValue) {
		this.stringValue = stringValue;
	}

	/**
	 * @return the {@link #entityReferenceValue}
	 */
	public EntityReferenceDto getEntityReferenceValue() {
		return entityReferenceValue;
	}

	/**
	 * @param entityReferenceValue the entityReferenceValue to set
	 */
	public void setEntityReferenceValue(EntityReferenceDto entityReferenceValue) {
		this.entityReferenceValue = entityReferenceValue;
	}

	/**
	 * @return the {@link #childEntityValue}
	 */
	public CustomEntityInstanceDto getChildEntityValue() {
		return childEntityValue;
	}

	/**
	 * @param childEntityValue the childEntityValue to set
	 */
	public void setChildEntityValue(CustomEntityInstanceDto childEntityValue) {
		this.childEntityValue = childEntityValue;
	}

	/**
	 * @return the {@link #objectValue}
	 */
	public Object getObjectValue() {
		return objectValue;
	}

	/**
	 * @param objectValue the objectValue to set
	 */
	public void setObjectValue(Object objectValue) {
		this.objectValue = objectValue;
	}
	
	@JsonSetter("value")
	public void setValue(Object object) {
		if(object instanceof String) {
    		this.stringValue = (String) object;
    	} else if (object instanceof Date) {
    		this.dateValue = (Date) object;
    	} else if(object instanceof Long) {
    		this.longValue = (Long) object;
    	} else if(object instanceof Double) {
    		this.doubleValue = (Double) object;
    	} else if(object instanceof EntityReferenceDto) {
    		this.entityReferenceValue = (EntityReferenceDto) object;
    	} else if(object instanceof CustomEntityInstanceDto) {
    		this.childEntityValue = (CustomEntityInstanceDto) object;
    	} else if(object instanceof Map) {
    		Map map = (Map) object;
    		if(map.containsKey("customFields")) {
    			this.childEntityValue = JacksonUtil.convert(map, CustomEntityInstanceDto.class);
    		} else if(map.containsKey("classnameCode")) {
    			this.entityReferenceValue = JacksonUtil.convert(map, EntityReferenceDto.class);
    		} else {
    			this.objectValue = object;
    		}
    	} else {
    		this.objectValue = object;
    	}
	}
	
    
}