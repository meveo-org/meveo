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
package org.meveo.model.resource;

/**
 * Allocated or released for Line. Allocated from preactivation, released on
 * MSISDN change.
 * 
 * @author Ignas Lelys
 * @created Oct 31, 2010
 * 
 */
public enum LineStatusEnum {

    ALLOCATED(1, "ALLOCATED"),
    RELEASED(2, "RELEASED");

    private String label;
    private Integer id;

    LineStatusEnum(Integer id, String label) {
        this.label = label;
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public Integer getId() {
        return id;
    }

    public static LineStatusEnum getValue(Integer id) {
        if (id != null) {
            for (LineStatusEnum status : values()) {
                if (id.equals(status.getId())) {
                    return status;
                }
            }
        }
        return null;
    }

}
