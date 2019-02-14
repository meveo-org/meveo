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

package org.meveo.api.function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.meveo.admin.exception.BusinessException;
import org.meveo.api.dto.function.FunctionDto;
import org.meveo.api.jmeter.JMXFileParser;
import org.meveo.model.scripts.test.TestConfiguration;
import org.meveo.model.scripts.Function;
import org.meveo.service.script.ConcreteFunctionService;
import org.meveo.service.script.FunctionService;
import org.meveo.service.script.ScriptInterface;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.core.Response;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Stateless
public class FunctionApi {

    private static final DocumentBuilder builder;

    static {
        try {
            builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            throw new RuntimeException(e);  // Should never happen
        }
    }

    @Inject
    private ConcreteFunctionService concreteFunctionService;

    public List<FunctionDto> list(){
        final List<Function> functions = concreteFunctionService.list();
        return functions.stream().map(e -> {
            final FunctionDto functionDto = new FunctionDto();
            functionDto.setCode(e.getCode());
            functionDto.setTestSuite(e.getTestSuite());
            return functionDto;
        }).collect(Collectors.toList());
    }

    public Map<String, Object> execute(String code, Map<String, Object> inputs) throws BusinessException {
        return concreteFunctionService.getFunctionService(code).execute(code, inputs);
    }

    public String getTest(String code) {
        final Function function = concreteFunctionService.findByCode(code);
        return function.getTestSuite();
    }

    public void updateTest(String code, File file) throws BusinessException, IOException {
        final String testSuite = FileUtils.readFileToString(file, "UTF-8");
        final Function function = concreteFunctionService.findByCode(code);
        function.setTestSuite(testSuite);
        concreteFunctionService.update(function);
    }

    public Response executeTest(String code) throws Exception {
        final Function function = concreteFunctionService.findByCode(code);
        final TestConfiguration testConfiguration = buildTestConfiguration(function.getTestSuite());
        final FunctionService<?, ScriptInterface> functionService = concreteFunctionService.getFunctionService(code);
        final Map<String, Object> execute = functionService.execute(code, testConfiguration.getTestInputs());
        return Response.ok().build();
    }

    private static TestConfiguration buildTestConfiguration(String jmxFile) throws IOException, SAXException, XPathExpressionException {
        Document xmlDocument = builder.parse(jmxFile);
        TestConfiguration testConfiguration = new TestConfiguration();
        testConfiguration.setExpectedOutputs(JMXFileParser.extractExpectedOutputs(xmlDocument));
        testConfiguration.setTestInputs(JMXFileParser.extractParameters(xmlDocument));
        return testConfiguration;
    }

}
