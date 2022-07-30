

/*
 * (C) Copyright 2018-2020 Webdrone SAS (https://www.webdrone.fr/) and contributors.
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

package com.blackbear.flatworm;

import java.lang.reflect.Method;

public class Callback
{
    private Object instance;
    private Method method;
    
    public Callback(final Object instance, final Method method) {
        this.instance = instance;
        this.method = method;
    }
    
    public Object getInstance() {
        return this.instance;
    }
    
    public Method getMethod() {
        return this.method;
    }
}
