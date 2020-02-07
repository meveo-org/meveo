package org.meveo.service.technicalservice.endpoint;

import java.io.File;
import java.io.IOException;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.commons.io.FileUtils;
import org.apache.http.HttpStatus;
import org.meveo.api.swagger.SwaggerDocService;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.script.ScriptUtils;
import org.slf4j.Logger;

import io.swagger.models.Response;
import io.swagger.models.Swagger;
import io.swagger.util.Json;

/**
 * This service is use to build the endpoint interface from a template file. The
 * output is Javascript code to be use by frontend client.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.8.0
 * @version 6.8.0
 */
@Stateless
public class ESGeneratorService {

	@Inject
	private Logger log;

	@Inject
	private SwaggerDocService swaggerDocService;

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	/**
	 * Generates an endpoint interface in js code using a template file.
	 * 
	 * @param baseUrl  base url of the request
	 * @param endpoint endpoint
	 * @see Endpoint
	 * @return it returns the endpoint interface in js
	 * @throws IOException when template is not found
	 */
	public String buildJSInterfaceFromTemplate(String baseUrl, Endpoint endpoint) {

		EndpointJSInterface endpointJSInterface = new EndpointJSInterface();
		endpointJSInterface.setEndpointCode(endpoint.getCode());
		endpointJSInterface.setEndpointDescription(endpoint.getDescription());

		String returnedVariableType = ScriptUtils.getReturnedVariableType(endpoint.getService(), endpoint.getReturnedVariableName());
		CustomEntityTemplate returnedCet = customEntityTemplateService.findByDbTablename(returnedVariableType);
		if (returnedCet != null) {
			endpointJSInterface.setCet(true);
		}

		Swagger swaggerDoc = swaggerDocService.generateOpenApiJson(baseUrl, endpoint);
		Response response = swaggerDoc.getResponses().get("" + HttpStatus.SC_OK);
		endpointJSInterface.setResponseSchema(Json.pretty(response.getSchema()));

		try {
			if (endpoint.getMethod() == EndpointHttpMethod.GET) {
				endpointJSInterface
						.setTemplate(FileUtils.readFileToString(new File(ESGenerator.class.getClassLoader().getResource("endpoint-js-template/get-template.js").getFile())));

			} else if (endpoint.getMethod() == EndpointHttpMethod.POST) {
				endpointJSInterface
						.setTemplate(FileUtils.readFileToString(new File(ESGenerator.class.getClassLoader().getResource("endpoint-js-template/post-template.js").getFile())));
			}
		} catch (IOException e) {
			return "Missing template";
		}

		return endpointJSInterface.build();
	}
}
