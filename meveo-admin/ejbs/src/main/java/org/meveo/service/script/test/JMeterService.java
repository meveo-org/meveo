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

package org.meveo.service.script.test;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ejb.EJB;
import javax.enterprise.context.ApplicationScoped;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.SecurityContext;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.jboss.resteasy.client.jaxrs.ResteasyClient;
import org.jboss.resteasy.client.jaxrs.ResteasyClientBuilder;
import org.jboss.resteasy.plugins.interceptors.encoding.AcceptEncodingGZIPFilter;
import org.jboss.resteasy.plugins.interceptors.encoding.GZIPDecodingInterceptor;
import org.jboss.resteasy.plugins.interceptors.encoding.GZIPEncodingInterceptor;
import org.keycloak.representations.AccessTokenResponse;
import org.meveo.commons.utils.ParamBean;
import org.meveo.model.scripts.Function;
import org.meveo.service.script.DefaultFunctionService;
import org.primefaces.shaded.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXParseException;

@ApplicationScoped 
public class JMeterService {


    private static final XPath XPATH = XPathFactory.newInstance().newXPath();
	private static final Logger LOG = LoggerFactory.getLogger(JMeterService.class);
    private static final String JMETER_BIN_FOLDER = ParamBean.getInstance().getProperty("jmeter", null);

    private String realm = System.getProperty("meveo.keycloak.realm");
    private String clientId = System.getProperty("meveo.keycloak.client");
    private String serverUrl = System.getProperty("meveo.keycloak.url");
    private String clientSecret = System.getProperty("meveo.keycloak.secret");

    private String userName = ParamBean.getInstance().getProperty("jmeter.credentials.username", null);
    private String password = ParamBean.getInstance().getProperty("jmeter.credentials.password", null);

    private String hostName = ParamBean.getInstance().getProperty("jmeter.server.hostname", null);
    private String protocol = ParamBean.getInstance().getProperty("jmeter.server.protocol", null);
    private String portNumber = ParamBean.getInstance().getProperty("jmeter.server.portnumber", null);

    @EJB
    private DefaultFunctionService functionService;
    
    @Context
    private SecurityContext sc;

    @PostConstruct
    private void init() {
        if (JMETER_BIN_FOLDER == null) {
            LOG.warn("JMeter binary path is not set, function test functionnality will therefore not be available.");
        } else if (userName == null || password == null) {
            LOG.warn("Jmeter user is not set, function test functionnality will therefore not be available.");
        } else if (hostName == null || protocol == null || portNumber == null) {
            LOG.warn("Jmeter test server is not set, function test functionnality will therefore not be available.");
        }
        
    }

    public TestResult executeTest(String functionCode) throws IOException {
        if (JMETER_BIN_FOLDER == null) {
            throw new IllegalArgumentException("JMeter binary path is not set.");
        }

        if (userName == null || password == null) {
            throw new IllegalArgumentException("Jmeter user is not set.");
        }

        if (hostName == null || protocol == null || portNumber == null) {
            throw new IllegalArgumentException("Jmeter test server is not set.");
        }
        
        final List<SampleResult> sampleResults = new ArrayList<>();
        
        // Temp log file
        File logFile = File.createTempFile(functionCode, ".log");

        // Retrieve and create test file
        final Function function;
        try {
        	function = functionService.findByCode(functionCode);
        } catch(Exception e) {
        	var error = new SampleResult(false, "Failed to retrieve function", "[WARN] " + e.getMessage());
        	sampleResults.add(error);
        	return new TestResult(e.toString(), sampleResults);
        }
        
        if(function == null) {
        	throw new IllegalArgumentException("Function with code " + functionCode + " does not exist");
        }
        
        functionService.detach(function);

        File jmxFile = File.createTempFile(functionCode, ".jmx");

		ResteasyClient client = new ResteasyClientBuilder()
				.register(AcceptEncodingGZIPFilter.class)
                .register(GZIPDecodingInterceptor.class)
                .register(GZIPEncodingInterceptor.class)
				.build();
		
		String accessTokenString;
		
		try {
			var target = client.target(serverUrl +"/realms/" + realm + "/protocol/openid-connect/token");
		
			Form form = new Form();
			form.param("client_id", clientId);
			form.param("username", userName);
			form.param("password", password);
			form.param("grant_type", "password");
			form.param("client_secret", clientSecret);
			
			try(var response = target.request("application/json")
					.header("Content-Type", "application/x-www-form-urlencoded")
					.post(Entity.form(form))) {
			
				accessTokenString = response.readEntity(AccessTokenResponse.class).getToken();
			}
			
		} finally {
			client.close();
		}
		
        if(accessTokenString == null) {
        	throw new NullPointerException("Cannot obtain access token for user " + userName);
        }
        
        String testSuiteString = function.getTestSuite();

        // Activate functional test mode to get full data to be saved
        testSuiteString = testSuiteString.replace(
                "<boolProp name=\"TestPlan.functional_mode\">false</boolProp>",
                "<boolProp name=\"TestPlan.functional_mode\">true</boolProp>"
        );

        FileWriter writer = new FileWriter(jmxFile);
        writer.write(testSuiteString);
        writer.close();
        
        // Execute test
        File jtlFile = File.createTempFile(functionCode, ".xml");

        ProcessBuilder processBuilder = new ProcessBuilder(
    		JMETER_BIN_FOLDER, 
    		"-n", 
    		"-t", jmxFile.getAbsolutePath(),
    		"-l", jtlFile.getAbsolutePath(),
    		"-j", logFile.getAbsolutePath(),
    		"-Dtoken=" + accessTokenString,
    		"-DhostName=" + hostName,
    		"-DportNumber=" + portNumber,
    		"-Dprotocol=" + protocol,
    		"-Jjmeter.save.saveservice.output_format=xml"
		);
        
        processBuilder.environment().put("HEAP", "-Xms256m -Xmx256m -XX:MaxMetaspaceSize=256m");

        final Process exec = processBuilder.start();
        
        try {
            exec.waitFor();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        
        String logs = IOUtils.toString(exec.getInputStream());
        if(!StringUtils.isBlank(logs)) {
        	LOG.info("[JMETER] " + logs);
        }
        
        String errors = IOUtils.toString(exec.getErrorStream());
        if(!StringUtils.isBlank(errors)) {
        	LOG.error("[JMETER] " + errors);
        }
        
        exec.destroy();
        
        String responeData = null;

        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();

        try {
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            Document doc = dBuilder.parse(jtlFile);

            // Retrieve assertions
            final NodeList assertionResults = doc.getElementsByTagName("assertionResult");
            for(int i = 0; i < assertionResults.getLength(); i++){
                Element n = (Element) assertionResults.item(i);
                boolean success = !Boolean.parseBoolean(n.getElementsByTagName("failure").item(0).getTextContent());
                String name = n.getElementsByTagName("name").item(0).getTextContent();
                final NodeList failureMessageNodes = n.getElementsByTagName("failureMessage");
                String failureMessage = "";
                if(failureMessageNodes != null && failureMessageNodes.getLength() > 0) {
                    failureMessage = failureMessageNodes.item(0).getTextContent();
                }
                
                SampleResult sampleResult = new SampleResult(success, name, failureMessage);
                
				sampleResults.add(sampleResult);

            }
            
			Node node = (Node) XPATH.compile("(//responseData)[last()]").evaluate(doc, XPathConstants.NODE);
			if(node != null) {
				responeData = node.getTextContent();
			}

        } catch(SAXParseException e) {
        	String message = "[WARN] " + e.getMessage() + "\n in file :\n\n" + FileUtils.readFileToString(jtlFile, StandardCharsets.UTF_8);

        	var error = new SampleResult(false, "XML Parsing error", message);
        	sampleResults.add(error);
        	
        	return new TestResult(message, sampleResults);
        	
        } catch (Exception e) {
        	var error = new SampleResult(false, "Error", e.getMessage());
        	sampleResults.add(error);
        	return new TestResult(e.toString(), sampleResults);
        	
        } finally {
        	if(logFile.exists()) {
        		logFile.delete();
        	}
        	
        	if(jtlFile.exists()) {
        		jtlFile.delete();
        	}
        	
        	if(jmxFile.exists()) {
        		jmxFile.delete();
        	}
        }

        return new TestResult(responeData, sampleResults);

    }

}
