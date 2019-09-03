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

package org.meveo.api.rest.technicalservice;

import javax.servlet.http.HttpServletRequest;

public enum Headers {

    PERSISTENCE_CONTEXT("Persistence-Context"),
    PERSISTENCE_CONTEXT_ID("Persistence-Context-Id"),
    BUDGET_UNIT("Budget-Unit"),
    BUDGET_MAX_VALUE("Budget-Max-Value"),
    DELAY_UNIT("Delay-Unit"),
    DELAY_MAX_VALUE("Delay-Max-Value"),
    KEEP_DATA("Keep-Data"),
    WAIT_FOR_FINISH("Wait-For-Finish");

    private final String headerName;

    Headers(String s) {
        this.headerName = s;
    }

    public String getValue(HttpServletRequest request){
        return request.getHeader(this.headerName);
    }

    public String getValue(HttpServletRequest request, String defaultValue){
        final String header = request.getHeader(this.headerName);
        return header != null ? header : defaultValue;
    }

    public <T> T getValue(HttpServletRequest request, Class<T> rawClass) {
        return getValue(request, rawClass, null);
    }

    @SuppressWarnings("unchecked")
    public <T> T getValue(HttpServletRequest request, Class<T> rawClass, T defaultValue){
        final String header = getValue(request);
        if(header != null){
            Object value = null;
            if(Boolean.class.equals(rawClass)){
                value = Boolean.parseBoolean(header);
            }else if(Integer.class.equals(rawClass)){
                value = Integer.parseInt(header);
            }else if(Long.class.equals(rawClass)){
                value = Long.parseLong(header);
            }else if(Float.class.equals(rawClass)){
                value = Float.parseFloat(header);
            }else if(Double.class.equals(rawClass)){
                value = Double.parseDouble(header);
            }else if(String.class.equals(rawClass)){
                value = header;
            } else if(Enum.class.isAssignableFrom(rawClass)){
                value = Enum.valueOf((Class) rawClass, header);
            }
            return (T) value;
        }else{
            return defaultValue;
        }
    }
}
