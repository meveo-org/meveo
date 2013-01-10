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
package org.meveo.core.inputloader;

import java.util.HashMap;
import java.util.Map;

/**
 * Meveo input class. Hold input name, input itself and map of properties for
 * input.
 * 
 * @author Ignas Lelys
 * @created Apr 20, 2010
 * 
 */
public class Input {

    /** Input name (e.g. file name). */
    private String name;

    /** Input object (e.g. file, DTO etc.). */
    private Object inputObject;

    /** Input properties if needed. */
    private Map<String, String> properties;

    public Input(String name, Object inputObject) {
        this.name = name;
        this.inputObject = inputObject;
        this.properties = new HashMap<String, String>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getInputObject() {
        return inputObject;
    }

    public void setInputObject(Object inputObject) {
        this.inputObject = inputObject;
    }

    public void addProperty(String key, String value) {
        this.properties.put(key, value);
    }

    public String getProperty(String key) {
        return this.properties.get(key);
    }

}
