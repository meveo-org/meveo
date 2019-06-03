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

package org.meveo.persistence.neo4j.service.graphql;

import java.util.Objects;

public class GraphQLField implements Comparable {

    private String fieldName;
    private String fieldType;
    private boolean multivialued;
    private boolean required;
    private String query;

    public GraphQLField() {}

    public GraphQLField(String fieldName, String fieldType, boolean required) {
        this.fieldName = fieldName;
        this.fieldType = fieldType;
        this.required = required;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public boolean isMultivialued() {
        return multivialued;
    }

    public void setMultivalued(boolean multivialued) {
        this.multivialued = multivialued;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphQLField that = (GraphQLField) o;
        return getFieldName().equals(that.getFieldName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getFieldName());
    }

    @Override
    public int compareTo(Object o) {
       if(o instanceof GraphQLField){
           GraphQLField other = (GraphQLField) o;
           return this.getFieldName().compareTo(other.getFieldName());
       }

       return 0;
    }
}
