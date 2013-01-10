/*
* (C) Copyright 2009-2013 Manaty SARL (http://manaty.net/) and contributors.
*
* Licensed under the GNU Public Licence, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*     http://www.gnu.org/licenses/gpl-2.0.txt
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.meveo.model.rating;

/**
 * Matrix entry types.
 * 
 * @author Ignas Lelys
 * @created 2009.08.11
 */
public enum MatrixEntryType {

    NUMBER(1, "commons.number"), STRING(2, "commons.string");

    private int id;

    private String label;

    MatrixEntryType(int id, String label) {
        this.id = id;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String toString() {
        return label;
    }

    public static MatrixEntryType getValue(Integer id) {
        if (id != null) {
            for (MatrixEntryType type : values()) {
                if (id.equals(type.getId())) {
                    return type;
                }
            }
        }
        return null;
    }

}
