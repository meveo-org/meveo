package org.meveo.api.dto;

import org.meveo.model.scripts.FileDependency;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "FileDependency")
@XmlAccessorType(XmlAccessType.FIELD)
public class FileDependencyDto extends BaseEntityDto{

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The path. */
    @XmlAttribute()
    private String path;

    /**
     * Instantiates a new file dependency dto.
     */
    public FileDependencyDto() {

    }

    public FileDependencyDto(FileDependency fileDependency) {
        this.setPath(fileDependency.getPath());
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}
