package org.meveo.api.dto;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * The Class PermissionsDto.
 * 
 * @author anasseh
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @version 6.7.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
@ApiModel
public class PermissionsDto implements Serializable {

    /** The Constant serialVersionUID. */
    private static final long serialVersionUID = 1L;

    /** The permission. */
    @ApiModelProperty("List of permissions")
    private List<PermissionDto> permission = new ArrayList<PermissionDto>();

    /**
     * Gets the permission.
     *
     * @return the permission
     */
    public List<PermissionDto> getPermission() {
        return permission;
    }

    /**
     * Sets the permission.
     *
     * @param permission the new permission
     */
    public void setPermission(List<PermissionDto> permission) {
        this.permission = permission;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        return "PermissionsDto [permission=" + permission + "]";
    }

}
