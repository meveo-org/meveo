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
package org.manaty.utils;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilities class for working with Lists.
 * 
 * @author Ignas Lelys
 * @created Mar 6, 2009
 */
public final class ListUtils {

    /**
     * No need to create it.
     */
    private ListUtils() {

    }

    /**
     * This method creates a list and adds all passed parameters to it.
     * 
     * @param args
     *            Objects we want to create list from.
     * @return {@link ArrayList} of objects passed as parameters.
     */
    public static <T> List<T> createList(T... args) {
        List<T> list = new ArrayList<T>();
        for (T arg : args) {
            list.add(arg);
        }
        return list;
    }
}
