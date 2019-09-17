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

package org.meveo.api.jsonata;

import org.junit.Assert;
import org.junit.Test;
import org.meveo.api.utils.JSONata;

public class JsonataTest {

    private static String input = "{\n" +
            "  \"Account\": {\n" +
            "    \"Account Name\": \"Firefly\",\n" +
            "    \"Order\": [\n" +
            "      {\n" +
            "        \"OrderID\": \"order103\",\n" +
            "        \"Product\": [\n" +
            "          {\n" +
            "            \"Product Name\": \"Bowler Hat\",\n" +
            "            \"ProductID\": 858383,\n" +
            "            \"SKU\": \"0406654608\",\n" +
            "            \"Description\": {\n" +
            "              \"Colour\": \"Purple\",\n" +
            "              \"Width\": 300,\n" +
            "              \"Height\": 200,\n" +
            "              \"Depth\": 210,\n" +
            "              \"Weight\": 0.75\n" +
            "            },\n" +
            "            \"Price\": 34.45,\n" +
            "            \"Quantity\": 2\n" +
            "          },\n" +
            "          {\n" +
            "            \"Product Name\": \"Trilby hat\",\n" +
            "            \"ProductID\": 858236,\n" +
            "            \"SKU\": \"0406634348\",\n" +
            "            \"Description\": {\n" +
            "              \"Colour\": \"Orange\",\n" +
            "              \"Width\": 300,\n" +
            "              \"Height\": 200,\n" +
            "              \"Depth\": 210,\n" +
            "              \"Weight\": 0.6\n" +
            "            },\n" +
            "            \"Price\": 21.67,\n" +
            "            \"Quantity\": 1\n" +
            "          }\n" +
            "        ]\n" +
            "      },\n" +
            "      {\n" +
            "        \"OrderID\": \"order104\",\n" +
            "        \"Product\": [\n" +
            "          {\n" +
            "            \"Product Name\": \"Bowler Hat\",\n" +
            "            \"ProductID\": 858383,\n" +
            "            \"SKU\": \"040657863\",\n" +
            "            \"Description\": {\n" +
            "              \"Colour\": \"Purple\",\n" +
            "              \"Width\": 300,\n" +
            "              \"Height\": 200,\n" +
            "              \"Depth\": 210,\n" +
            "              \"Weight\": 0.75\n" +
            "            },\n" +
            "            \"Price\": 34.45,\n" +
            "            \"Quantity\": 4\n" +
            "          },\n" +
            "          {\n" +
            "            \"ProductID\": 345664,\n" +
            "            \"SKU\": \"0406654603\",\n" +
            "            \"Product Name\": \"Cloak\",\n" +
            "            \"Description\": {\n" +
            "              \"Colour\": \"Black\",\n" +
            "              \"Width\": 30,\n" +
            "              \"Height\": 20,\n" +
            "              \"Depth\": 210,\n" +
            "              \"Weight\": 2\n" +
            "            },\n" +
            "            \"Price\": 107.99,\n" +
            "            \"Quantity\": 1\n" +
            "          }\n" +
            "        ]\n" +
            "      }\n" +
            "    ]\n" +
            "  }\n" +
            "}";

    private static String expr = "Account.Order[0].OrderID";

    private static String returned = "\"order103\"";

    @Test
    public void testTransformation(){
        String returnedValue = JSONata.transform(expr, input);
        Assert.assertEquals(returned, returnedValue);
    }
}
