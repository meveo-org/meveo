package org.meveo.api.dto;

import org.meveo.model.customEntities.CustomEntityCategory;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "CustomEntityCategory")
@XmlAccessorType(XmlAccessType.FIELD)
public class CustomEntityCategoryDto extends BaseEntityDto {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = -6633504145323452803L;

    @XmlAttribute(required = true)
    private String code;

    @XmlAttribute(required = true)
    private String name;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static CustomEntityCategoryDto toDTO(CustomEntityCategory cec) {
        CustomEntityCategoryDto dto = new CustomEntityCategoryDto();
        dto.setCode(cec.getCode());
        dto.setName(cec.getName());
        return dto;
    }

    /**
     * Convert CustomEntityCategoryDto to a CustomEntityCategory instance. Note: does not convert custom entities that are part of DTO
     *
     * @param dto         CustomEntityCategoryDto object to convert
     * @param cecToUpdate CustomEntityCategory to update with values from dto, or if null create a new one
     * @return A new or updated CustomEntityCategory instance
     */
    public static CustomEntityCategory fromDTO(CustomEntityCategoryDto dto, CustomEntityCategory cecToUpdate) {
        CustomEntityCategory cec = new CustomEntityCategory();
        if (cecToUpdate != null) {
            cec = cecToUpdate;
        }
        cec.setCode(dto.getCode());
        cec.setName(dto.getName());
        return cec;
    }

    @Override
    public String toString() {
        return "CustomEntityCategoryDto [  code=" + code + ",name=" + name + "]";
    }
}

