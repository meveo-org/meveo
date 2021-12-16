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

package org.meveo.api.technicalservice.endpoint;

import org.meveo.model.technicalservice.endpoint.EndpointVariables;
import org.meveo.service.script.Script;

import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ExposedScriptTest extends Script {

    private String city;

    private String country;

    @Override
    public void execute(Map<String, Object> methodContext) {
        methodContext.put("resultCity", "The city is : " + city);
        methodContext.put("resultCountry", "The country is : " + country);
        Double maxBudget = (Double) methodContext.get(EndpointVariables.MAX_BUDGET);
        String unit = (String) methodContext.get(EndpointVariables.BUDGET_UNIT);
        methodContext.put("budget", "The budget is " + maxBudget + " " + unit);
        Long maxDelay = (Long) methodContext.get(EndpointVariables.MAX_DELAY);
        TimeUnit delayUnit = (TimeUnit) methodContext.get(EndpointVariables.DELAY_UNIT);
        methodContext.put("delay", "The delay is " + maxDelay + " " + delayUnit);
    }

    public void setCity(String city){
        this.city = city;
    }

    public void setCountry(String coutry){
        this.country = coutry;
    }

}
