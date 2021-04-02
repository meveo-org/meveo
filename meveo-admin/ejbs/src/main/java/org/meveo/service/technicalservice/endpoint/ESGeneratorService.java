package org.meveo.service.technicalservice.endpoint;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.apache.commons.io.IOUtils;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.script.ScriptUtils;
import org.meveo.service.technicalservice.endpoint.schema.EndpointSchemaService;
import org.slf4j.Logger;

/**
 * This service is use to build the endpoint interface from a template file. The
 * output is Javascript code to be use by frontend client.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.8.0
 * @version 6.9.0
 */
@Stateless
public class ESGeneratorService {

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	@Inject
	private EndpointSchemaService endpointSchemaService;

	@Inject
	private EndpointService endpointService;

	@Inject
	private ESGeneratorService esGeneratorService;

	@Inject
	private Logger log;

	/**
	 * Generates an endpoint interface in js code using a template file.
	 * 
	 * @param endpoint endpoint
	 * @see Endpoint
	 * @return it returns the endpoint interface in js
	 */
	public String buildJSInterface(Long id) {
		Endpoint endpoint = endpointService.findById(id);
		return esGeneratorService.buildJSInterface(endpoint);
	}

	/**
	 * Generates an endpoint interface in js code using a template file.
	 * 
	 * @param endpoint endpoint
	 * @see Endpoint
	 * @return it returns the endpoint interface in js
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String buildJSInterface(Endpoint endpoint) {
		return buildJSInterface("", endpoint);
	}

	/**
	 * Generates an endpoint interface in js code using a template file or the file
	 * committed in repository.
	 * 
	 * @param baseUrl  base url of the request
	 * @param endpoint endpoint
	 * @see Endpoint
	 * @return it returns the endpoint interface in js
	 */
	public String buildJSInterface(String baseUrl, Endpoint endpoint) {
		return buildJSInterfaceFromTemplate(baseUrl, endpoint, "");
	}

	/**
	 * Build the base endpoint interface for all classes. Accessible at
	 * MEVEO_URL/api/rest/endpoint/EndpointInterface.js
	 * 
	 * @param baseUrl  base url of the request
	 * @param endpoint endpoint
	 * @return
	 * @throws IOException
	 */
	public String buildBaseEndpointInterface(String baseUrl) throws IOException {

		String template = "";
		// checks if the file exists in repository
		if (endpointService.isBaseEndpointScriptExists()) {
			StringWriter writer = new StringWriter();
			try {
				IOUtils.copy(new InputStreamReader(new FileInputStream(endpointService.getBaseScriptFile())), writer);

			} catch (IOException e) {
				log.error("Failed loading base js interface template with error {}", e.getMessage());
				return "Missing base endpointInterface template";
			}

			template = writer.toString();
		}

		return buildBaseJSInterfaceFromTemplate(baseUrl, template);
	}

	private String buildBaseJSInterfaceFromTemplate(String baseUrl, String template) throws IOException {

		EndpointJSInterface endpointJSInterface = new EndpointJSInterface();
		endpointJSInterface.setApiUrl(baseUrl);
		URL templateUrl = ESGenerator.class.getClassLoader().getResource("/endpoint-js-template/endpoint-interface-template.js");
		endpointJSInterface.setTemplate(IOUtils.toString(templateUrl, StandardCharsets.UTF_8));

		return endpointJSInterface.build();
	}

	/**
	 * Generates an endpoint interface in js code using a template file.
	 * 
	 * @param baseUrl  base url of the request
	 * @param endpoint endpoint
	 * @param template
	 * @see Endpoint
	 * @return it returns the endpoint interface in js
	 * @throws IOException when template is not found
	 */
	private String buildJSInterfaceFromTemplate(String baseUrl, Endpoint endpoint, String template) {

		EndpointJSInterface endpointJSInterface = new EndpointJSInterface();
		endpointJSInterface.setEndpointCode(endpoint.getCode());
		endpointJSInterface.setEndpointDescription(endpoint.getDescription());
		endpointJSInterface.setHttpMethod(endpoint.getMethod());
		endpointJSInterface.setApiUrl(baseUrl);

		String returnedVariableType = ScriptUtils.findScriptVariableType(endpoint.getService(), endpoint.getReturnedVariableName());
		CustomEntityTemplate returnedCet = customEntityTemplateService.findByDbTablename(returnedVariableType);
		if (returnedCet != null) {
			endpointJSInterface.setCet(true);
		}

		endpointJSInterface.setResponseSchema(endpointSchemaService.generateResponseSchema(endpoint));

		try {
			endpointJSInterface.setRequestSchema(endpointSchemaService.generateRequestSchema(endpoint));

			if (StringUtils.isBlank(template)) {
				URL templateUrl = null;
				if (endpoint.getMethod() == EndpointHttpMethod.GET) {
					templateUrl = ESGenerator.class.getClassLoader().getResource("/endpoint-js-template/get-template.js");

				} else if (endpoint.getMethod() == EndpointHttpMethod.POST) {
					templateUrl = ESGenerator.class.getClassLoader().getResource("/endpoint-js-template/post-template.js");

				} else {
					return "Missing template";
				}

				log.debug("Loading template file {}", templateUrl.getFile());

				endpointJSInterface.setTemplate(IOUtils.toString(templateUrl, StandardCharsets.UTF_8));

			} else {
				endpointJSInterface.setTemplate(template);
			}

		} catch (IOException e) {
			log.error("Failed loading js template with error {}", e.getMessage());
			return "Missing template";
		}

		return endpointJSInterface.build();
	}
}
