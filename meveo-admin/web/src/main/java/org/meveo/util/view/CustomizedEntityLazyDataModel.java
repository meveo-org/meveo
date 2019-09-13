/*
 * (C) Copyright 2018-2019 Webdrone SAS (https://www.webdrone.fr/) and contributors.
 * (C) Copyright 2015-2016 Opencell SAS (http://opencellsoft.com/) and contributors.
 * (C) Copyright 2009-2014 Manaty SARL (http://manaty.net/) and contributors.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  
 * This program is not suitable for any direct or indirect application in MILITARY industry
 * See the GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.meveo.util.view;

import org.meveo.service.custom.CustomizedEntity;
import org.primefaces.model.LazyDataModel;

import java.util.List;

public abstract class CustomizedEntityLazyDataModel<T extends CustomizedEntity> extends LazyDataModel<CustomizedEntity> {

    private static final long serialVersionUID = -5796910936316457321L;

    @Override
    public CustomizedEntity getRowData(String rowKey) {
        List<CustomizedEntity> entities = (List<CustomizedEntity>) getWrappedData();
        Long value = Long.parseLong(rowKey);

        for (CustomizedEntity customizedEntity : entities) {
            if (customizedEntity.getCustomEntityId() != null && customizedEntity.getCustomEntityId().equals(value)) {
                return customizedEntity;
            }
        }

        return null;
    }
}