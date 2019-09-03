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

package org.meveo.service.script;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScriptUtils {

    private static Logger logger = LoggerFactory.getLogger(ScriptUtils.class);

    public static ClassAndValue findTypeAndConvert(String type, String value) {
        ClassAndValue classAndValue = new ClassAndValue();

        // Try to find boxed type
        switch (type) {
            case "int":
                classAndValue.setClass(int.class);
            case "Integer":
                classAndValue.setValue(Integer.parseInt(value));
                break;

            case "double":
                classAndValue.setClass(double.class);
            case "Double":
                classAndValue.setValue(Double.parseDouble(value));
                break;

            case "long":
                classAndValue.setClass(long.class);
            case "Long":
                classAndValue.setValue(Long.parseLong(value));
                break;

            case "byte":
                classAndValue.setClass(byte.class);
            case "Byte":
                classAndValue.setValue(Byte.parseByte(value));
                break;

            case "short":
                classAndValue.setClass(short.class);
            case "Short":
                classAndValue.setValue(Short.parseShort(value));
                break;

            case "float":
                classAndValue.setClass(float.class);
            case "Float":
                classAndValue.setValue(Float.parseFloat(value));
                break;

            case "boolean":
                classAndValue.setClass(boolean.class);
            case "Boolean":
                classAndValue.setValue(Boolean.parseBoolean(value));
                break;

            default:
                classAndValue.setValue(value);
                logger.warn("Type {} not handled for string parsing", type);
        }

        // Case where the class if boxed or we don't handle it yet
        if(classAndValue.clazz == null){
            classAndValue.clazz = classAndValue.value.getClass();
        }

        return classAndValue;
    }

    public static class ClassAndValue {
        private Object value;
        private Class<?> clazz;

        public Object getValue() {
            return value;
        }

        public Class<?> getTypeClass() {
            return clazz;
        }


        public void setValue(Object value) {
            this.value = value;
        }

        public void setClass(Class<?> setterClass) {
            this.clazz = setterClass;
        }
    }
}
