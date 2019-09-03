/*
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

public abstract class CustomizedEntityLazyDataModel<T extends CustomizedEntity> extends LazyDataModel<CustomizedEntity> {

    private static final long serialVersionUID = -5796910936316457321L;

    @Override
    public Long getRowKey(CustomizedEntity object) {
        return object.getCustomEntityId();
    }

    @Override
    public int getRowCount() {
        return super.getRowCount();
    }

    @Override
    public CustomizedEntity getRowData() {
        return super.getRowData();
    }

    @Override
    public int getRowIndex() {
        return super.getRowIndex();
    }

    @Override
    public void setRowIndex(int rowIndex) {
        super.setRowIndex(rowIndex);
    }

    @Override
    public CustomizedEntity getRowData(String rowKey) {
        return super.getRowData(rowKey);
    }
}