package org.meveo.service.technicalservice.endpoint.schema;

import java.util.Objects;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.scripts.Function;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.model.technicalservice.endpoint.TSParameterMapping;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.script.ScriptUtils;
import org.meveo.util.ClassUtils;

/**
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.9.0
 * @version 6.9.0
 */
@Stateless
public class EndpointSchemaService {

	@Inject
	private EndpointSchemaGeneratorService endpointSchemaGeneratorService;

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

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
					param.setType(ScriptUtils.findScriptVariableType(endpoint.getService(), tsParameterMapping.getEndpointParameter().getParameter()));
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

	private EndpointParameter buildBodyParameterSchema(Function service, TSParameterMapping tsParameterMapping) {

		EndpointParameter result = null;
		String cetCode = tsParameterMapping.getEndpointParameter().getParameter();
		String parameterDataType = ScriptUtils.findScriptVariableType(service, tsParameterMapping.getEndpointParameter().getParameter());

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

	private EndpointParameter cetToModel(CustomEntityTemplate cet) {

		EndpointParameter result = new EndpointParameter();
		result.setName(cet.getName());
		result.setCet(cet);

		return result;
	}

	private EndpointParameter buildObjectResponse(String parameterName) {

		EndpointParameter result = new EndpointParameter();
		result.setName(parameterName);
		result.setType("object");

		return result;
	}

	public EndpointParameter buildPrimitiveDataType(String parameterName, String parameterDataType) {

		EndpointParameter result = new EndpointParameter();
		result.setName(parameterName);
		result.setType(parameterDataType);

		return result;
	}
}
