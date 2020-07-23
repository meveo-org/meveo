package org.meveo.service.technicalservice.endpoint.schema;

import java.util.Objects;

import javax.ejb.Stateless;
import javax.ejb.TransactionAttribute;
import javax.ejb.TransactionAttributeType;
import javax.inject.Inject;

import org.meveo.api.swagger.SwaggerDocService;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.scripts.CustomScript;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.ScriptInstance;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.model.technicalservice.endpoint.TSParameterMapping;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.script.ScriptUtils;
import org.meveo.util.ClassUtils;

/**
 * Service class for generating the request and response schema of an endpoint.
 * This service is used by Swagger to generate a complete endpoint js interface
 * that will be consume by the frontend application.
 * 
 * @author Edward P. Legaspi | edward.legaspi@manaty.net
 * @version 6.10
 * @since 6.9.0
 * @see Endpoint
 * @see SwaggerDocService
 */
@Stateless
public class EndpointSchemaService {

	@Inject
	private EndpointSchemaGeneratorService endpointSchemaGeneratorService;

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	/**
	 * Generates the request schema of a given endpoint.
	 * 
	 * @param endpoint the endpoint
	 * @return request schema of the given endpoint
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String generateRequestSchema(Endpoint endpoint) {

		EndpointSchema requestSchema = new EndpointSchema();
		requestSchema.setName(endpoint.getCode() + "Request");
		requestSchema.setDescription("Schema definition for " + endpoint.getCode());

		if (!Objects.isNull(endpoint.getParametersMapping())) {

			for (TSParameterMapping tsParameterMapping : endpoint.getParametersMapping()) {

				if (endpoint.getMethod().equals(EndpointHttpMethod.GET)) {
					// query
					EndpointParameter param = new EndpointParameter();
					param.setId(endpoint.getCode() + "_" + tsParameterMapping.getParameterName());
					param.setName(tsParameterMapping.getParameterName());
					param.setType(ScriptUtils.findScriptVariableType(endpoint.getService(),
							tsParameterMapping.getEndpointParameter().getParameter()));
					param.setDefaultValue(tsParameterMapping.getDefaultValue());
					requestSchema.addEndpointParameter(tsParameterMapping.getParameterName(), param);

				} else {
					// body
					EndpointParameter param = buildBodyParameterSchema(endpoint.getService(), tsParameterMapping);
					requestSchema.addEndpointParameter(tsParameterMapping.getParameterName(), param);
				}
			}
		}

		return endpointSchemaGeneratorService.generateSchema("endpoint", requestSchema);
	}

	/**
	 * Creates the endpoint body parameter that will later be converted into schema.
	 * 
	 * @param service            the script
	 * @param tsParameterMapping endpoint parameter
	 * @return the created endpoint parameter
	 * @see ScriptInstance
	 * @see EndpointParameter
	 */
	private EndpointParameter buildBodyParameterSchema(Function service, TSParameterMapping tsParameterMapping) {

		EndpointParameter result = null;
		String cetCode = tsParameterMapping.getEndpointParameter().getParameter();
		String parameterDataType = ScriptUtils.findScriptVariableType(service,
				tsParameterMapping.getEndpointParameter().getParameter());

		if (ClassUtils.isPrimitiveOrWrapperType(parameterDataType)) {
			result = buildPrimitiveDataType(tsParameterMapping.getParameterName(), parameterDataType);

		} else {

			CustomEntityTemplate returnedCet = customEntityTemplateService.findByDbTablename(cetCode);
			if (returnedCet != null) {
				result = cetToModel(returnedCet);

			} else {
				result = buildObjectResponse(tsParameterMapping.getParameterName());
			}
		}

		return result;
	}

	/**
	 * Generates the response schema of a given endpoint.
	 * 
	 * @param endpoint the endpoint
	 * @return response schema of the given endpoint
	 */
	@TransactionAttribute(TransactionAttributeType.NOT_SUPPORTED)
	public String generateResponseSchema(Endpoint endpoint) {

		EndpointSchema responseSchema = new EndpointSchema();
		responseSchema.setName(endpoint.getCode() + "Response");
		responseSchema.setDescription("Schema definition for " + endpoint.getCode());
		responseSchema.addEndpointParameter(endpoint.getReturnedVariableName(), buildResponseSchema(endpoint));

		return endpointSchemaGeneratorService.generateSchema("endpoint", responseSchema);
	}

	/**
	 * Creates the endpoint response as endpoint parameter that will later be
	 * converted to schema.
	 * 
	 * @return the created endpoint parameter
	 * @see EndpointParameter
	 */
	private EndpointParameter buildResponseSchema(Endpoint endpoint) {

		EndpointParameter result = null;

		if (!StringUtils.isBlank(endpoint.getReturnedVariableName()) && endpoint.getService() != null
				&& CustomScript.class.isAssignableFrom(endpoint.getService().getClass())) {

			String returnedVariableType = ScriptUtils.findScriptVariableType(endpoint.getService(),
					endpoint.getReturnedVariableName());

			if (ClassUtils.isPrimitiveOrWrapperType(returnedVariableType)) {
				result = buildPrimitiveDataType(endpoint.getReturnedVariableName(), returnedVariableType);

			} else {

				CustomEntityTemplate returnedCet = customEntityTemplateService.findByDbTablename(returnedVariableType);
				if (returnedCet != null) {
					result = cetToModel(returnedCet);

				} else {
					result = buildObjectResponse(endpoint.getReturnedVariableName());
				}
			}
		}

		return result;
	}

	/**
	 * Utility method to create a primitive endpoint parameter from a given name and
	 * type.
	 * 
	 * @param parameterName     name of the parameter
	 * @param parameterDataType type of the parameter
	 * @return created endpoint parameter
	 * @see EndpointParameter
	 */
	public EndpointParameter buildPrimitiveDataType(String parameterName, String parameterDataType) {

		EndpointParameter result = new EndpointParameter();
		result.setName(parameterName);
		result.setType(parameterDataType);

		return result;
	}

	/**
	 * Utility method to create a object endpoint parameter from a given name.
	 * 
	 * @param parameterName name of the parameter
	 * @return created endpoint parameter
	 * @see EndpointParameter
	 */
	private EndpointParameter buildObjectResponse(String parameterName) {

		EndpointParameter result = new EndpointParameter();
		result.setName(parameterName);
		result.setType("object");

		return result;
	}

	/**
	 * Utility method to create an endpoint parameter from a given cet.
	 * 
	 * @param cet the custom entity template
	 * @return created endpoint parameter
	 * @see EndpointParameter
	 * @see CustomEntityTemplate
	 */
	private EndpointParameter cetToModel(CustomEntityTemplate cet) {

		EndpointParameter result = new EndpointParameter();
		result.setName(cet.getName());
		result.setCet(cet);

		return result;
	}
}
