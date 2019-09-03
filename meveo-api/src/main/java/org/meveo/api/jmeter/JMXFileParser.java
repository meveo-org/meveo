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

package org.meveo.api.jmeter;

import com.fasterxml.jackson.core.type.TypeReference;
import org.meveo.model.scripts.test.ExpectedOutput;
import org.meveo.model.persistence.JacksonUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JMXFileParser {

    private static final XPath xPath = XPathFactory.newInstance().newXPath();

    private static final String REQUEST_NAME = "Execute Service";

    private static final String REQUEST_BODY_EXPR = String.format("//HTTPSamplerProxy[@testname='%s']//stringProp[@name='Argument.value']//text()", REQUEST_NAME);
    private static final String JSON_PATH_ASSERTIONS_EXPR = "//JSONPathAssertion";
    private static final String EXPECTED_VALUE_EXPRESSION = "//stringProp[@name='EXPECTED_VALUE']//text()";
    private static final String JSON_PATH_EXPRESSION = "//stringProp[@name='JSON_PATH']//text()";

    private static final Pattern PROP_NAME_EXTRACTOR = Pattern.compile("\\$\\.\\.entityOrRelations\\[\\?\\(@\\.name=='(.*)'\\)]\\.properties\\.(.*)");

    public static Map<String, Object> extractParameters(Document xmlDocument) throws XPathExpressionException {

        String requestBody = (String) xPath.compile(REQUEST_BODY_EXPR).evaluate(xmlDocument, XPathConstants.STRING);

        return JacksonUtil.fromString(requestBody, new TypeReference<Map<String, Object>>() {});

    }

    public static List<ExpectedOutput> extractExpectedOutputs(Document xmlDocument) throws XPathExpressionException {
        List<ExpectedOutput> expectedOutputs = new ArrayList<>();

        NodeList jsonPathAssertions = (NodeList) xPath.compile(JSON_PATH_ASSERTIONS_EXPR).evaluate(xmlDocument, XPathConstants.NODESET);

        for (int i = 0; i < jsonPathAssertions.getLength(); i++) {
            Node jsonPathAssertion = jsonPathAssertions.item(i);
            ExpectedOutput expectedOutput = new ExpectedOutput();

            // Note : Only works if technical service JSON Path extraction
            String jsonPath = (String) xPath.compile(JSON_PATH_EXPRESSION).evaluate(jsonPathAssertion, XPathConstants.STRING);
            final Matcher matcher = PROP_NAME_EXTRACTOR.matcher(jsonPath);
            expectedOutput.setVariableName(matcher.group(1));
            expectedOutput.setPropertyName(matcher.group(2));

            String expectedValue = (String) xPath.compile(EXPECTED_VALUE_EXPRESSION).evaluate(jsonPathAssertion, XPathConstants.STRING);
            expectedOutput.setExpectedValue(expectedValue);

            expectedOutputs.add(expectedOutput);
        }

        return expectedOutputs;
    }

}



