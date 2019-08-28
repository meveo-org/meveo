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

import org.apache.commons.io.IOUtils;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class JSONata {

    public static String transform(String expression, String data) {
        try {
            final InputStream jsonataFile = JSONata.class.getResourceAsStream("/jsonata/jsonata.js");
            final String jsonata = IOUtils.toString(jsonataFile, StandardCharsets.UTF_8);
            final InputStream jsonataRunnerFile = JSONata.class.getResourceAsStream("/jsonata/jsonata.execute.js");
            final String jsonataRunner = IOUtils.toString(jsonataRunnerFile, StandardCharsets.UTF_8);

            ScriptEngineManager factory = new ScriptEngineManager();
            ScriptEngine engine = factory.getEngineByName("JavaScript");
            Invocable inv = (Invocable) engine;

            engine.eval(jsonata);
            engine.eval(jsonataRunner);

            final Object runJsonata = inv.invokeFunction("runJsonata", data, expression);
            return runJsonata.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
