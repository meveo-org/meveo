/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify it under the terms of the
 * GNU Affero General Public License as published by the Free Software Foundation, either version 3
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without
 * even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. This program is
 * not suitable for any direct or indirect application in MILITARY industry See the GNU Affero
 * General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License along with this program.
 * If not, see <http://www.gnu.org/licenses/>.
 */

package org.meveo.comparators;

import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.crm.custom.CustomFieldTypeEnum;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.qualifiers.Comparator;
import org.meveo.service.crm.impl.CustomFieldTemplateService;

import javax.inject.Inject;
import java.util.Map;

@Comparator(CustomEntityTemplate.class)
public class CustomEntityTemplateComparator implements GenericComparator<CustomEntityTemplate>{

    @Inject
    private CustomFieldTemplateService customFieldTemplateService;

    @Override
    public int compare(CustomEntityTemplate o1, CustomEntityTemplate o2) {
        final Map<String, CustomFieldTemplate> firstEntityFields = customFieldTemplateService.findByAppliesTo(o1.getAppliesTo());
        final boolean secondEntityIsReferenced = firstEntityFields.values().stream()
                .filter(customFieldTemplate -> customFieldTemplate.getFieldType() == CustomFieldTypeEnum.ENTITY)
                .anyMatch(customFieldTemplate -> customFieldTemplate.getEntityClazzCetCode().equals(o2.getCode()));

        if(secondEntityIsReferenced){
            return 1;
        }

        final Map<String, CustomFieldTemplate> secondEntityFields = customFieldTemplateService.findByAppliesTo(o1.getAppliesTo());
        final boolean firstEntityIsReferenced = secondEntityFields.values().stream()
                .filter(customFieldTemplate -> customFieldTemplate.getFieldType() == CustomFieldTypeEnum.ENTITY)
                .anyMatch(customFieldTemplate -> customFieldTemplate.getEntityClazzCetCode().equals(o1.getCode()));



        if(firstEntityIsReferenced){
            return -1;
        }

        return 0;
    }
}
