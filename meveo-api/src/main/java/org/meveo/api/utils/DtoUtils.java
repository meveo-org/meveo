package org.meveo.api.utils;

import org.meveo.api.dto.AuditableEntityDto;
import org.meveo.api.dto.BaseEntityDto;
import org.meveo.api.dto.BusinessEntityDto;
import org.meveo.api.dto.EnableEntityDto;
import org.meveo.model.AuditableEntity;
import org.meveo.model.BaseEntity;
import org.meveo.model.BusinessEntity;
import org.meveo.model.EnableEntity;

/**
 * @author Edward P. Legaspi
 */
public class DtoUtils {

	public static BusinessEntityDto fromBusinessEntity(BusinessEntity source, BusinessEntityDto target) {
		fromEnableEntity(source, target);
		target.setCode(source.getCode());
		target.setDescription(source.getDescription());

		return target;
	}

	public static BusinessEntity toBusinessEntity(BusinessEntityDto source, BusinessEntity target) {
		toEnableEntity(source, target);
		target.setCode(source.getCode());
		target.setDescription(source.getDescription());

		return target;
	}

	public static EnableEntityDto fromEnableEntity(EnableEntity source, EnableEntityDto target) {
		fromAuditableEntity(source, target);
		target.setActive(source.isActive());

		return target;
	}

	public static EnableEntity toEnableEntity(EnableEntityDto source, EnableEntity target) {
		toAuditableEntity(source, target);
		if (source.isActive() != null) {
			target.setActive(source.isActive());
		}

		return target;
	}

	public static AuditableEntityDto fromAuditableEntity(AuditableEntity source, AuditableEntityDto target) {
		fromBaseEntity(source, target);
		if (source.getAuditable() != null) {
			if (source.getAuditable().getUpdated() != null) {
				target.setUpdated(source.getAuditable().getUpdated());

			} else {
				target.setUpdated(source.getAuditable().getCreated());
			}
		}

		return target;
	}

	public static AuditableEntityDto toAuditableEntity(AuditableEntityDto source, AuditableEntity target) {
		toBaseEntity(source, target);

		return source;
	}

	public static BaseEntityDto fromBaseEntity(BaseEntity source, BaseEntityDto target) {
		target.setId(source.getId());
		return target;
	}

	public static BaseEntityDto toBaseEntity(BaseEntityDto source, BaseEntity target) {
		// no we don't override the id :-)
		// target.setId(source.getId());

		return source;
	}
}
