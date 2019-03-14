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

package org.meveo.api.utils;

import com.eclipsesource.v8.V8;
import com.eclipsesource.v8.V8Object;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class JSONata {

    public static String transform(String expression, String data) {
        V8 runtime = V8.createV8Runtime();
        try {
            final InputStream jsonataFile = JSONata.class.getResourceAsStream("/jsonata/jsonata.js");
            final String jsonata = IOUtils.toString(jsonataFile, "UTF-8");
            runtime.executeScript(jsonata); // Load jsonata

            final InputStream jsonataRunnerFile = JSONata.class.getResourceAsStream("/jsonata/jsonata.execute.js");
            final String jsonataRunner = IOUtils.toString(jsonataRunnerFile, "UTF-8");
            runtime.executeScript(jsonataRunner); // Load runner

            Object runJsonata = runtime.executeJSFunction("runJsonata", data, expression);
            String transformedJson = runJsonata.toString();

            runtime.release();

            return transformedJson;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
