package org.meveo.api.swagger;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.ejb.Stateless;
import javax.inject.Inject;

import org.apache.http.HttpStatus;
import org.meveo.commons.utils.StringUtils;
import org.meveo.model.crm.CustomFieldTemplate;
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.scripts.CustomScript;
import org.meveo.model.scripts.Sample;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.model.technicalservice.endpoint.EndpointPathParameter;
import org.meveo.model.technicalservice.endpoint.TSParameterMapping;
import org.meveo.service.crm.impl.CustomFieldTemplateService;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.script.ScriptUtils;
import org.meveo.util.ClassUtils;
import org.meveo.util.Version;

import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.ModelImpl;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;
import io.swagger.models.properties.MapProperty;
import io.swagger.models.properties.Property;
import io.swagger.models.properties.StringProperty;

/**
 * Service class for generating swagger documentation on the fly.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.8.0
 * @version 6.8.0
 */
@Stateless
public class SwaggerDocService {

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	@Inject
	private CustomFieldTemplateService customFieldTemplateService;

	public ModelImpl cetToModel(CustomEntityTemplate cet) {

		Map<String, CustomFieldTemplate> cfts = customFieldTemplateService.findByAppliesTo(cet.getAppliesTo());

		ModelImpl result = new ModelImpl();
		result.setType("object");
		result.setDescription(cet.getDescription());

		result.setProperties(SwaggerHelper.convertCftsToProperties(cfts));
		result.setRequired(SwaggerHelper.getRequiredFields(cfts));

		return result;
	}

	public Swagger generateOpenApiJson(String baseUrl, Endpoint endpoint) {

		Info info = new Info();
		info.setTitle(endpoint.getCode());
		info.setDescription(endpoint.getDescription());
		info.setVersion(Version.appVersion);

		Map<String, Path> paths = new HashMap<>();
		Path path = new Path();

		Operation operation = new Operation();

		if (endpoint.getMethod().equals(EndpointHttpMethod.GET)) {
			path.setGet(operation);

		} else if (endpoint.getMethod().equals(EndpointHttpMethod.POST)) {
			path.setPost(operation);
		}

		if (!Objects.isNull(endpoint.getPathParameters())) {
			for (EndpointPathParameter endpointPathParameter : endpoint.getPathParameters()) {
				Parameter parameter = new PathParameter();
				parameter.setName(endpointPathParameter.getEndpointParameter().getParameter());
				path.addParameter(parameter);
			}
		}

		paths.put(endpoint.getEndpointUrl(), path);

		List<Sample> samples = endpoint.getService().getSamples();

		if (!Objects.isNull(endpoint.getParametersMapping())) {
			List<Parameter> operationParameter = new ArrayList<>();

			for (TSParameterMapping tsParameterMapping : endpoint.getParametersMapping()) {

				if (endpoint.getMethod().equals(EndpointHttpMethod.GET)) {
					QueryParameter queryParameter = new QueryParameter();
					queryParameter.setName(tsParameterMapping.getParameterName());
					operationParameter.add(queryParameter);

					if (samples != null && !samples.isEmpty()) {
						Object inputExample = samples.get(0).getInputs().get(tsParameterMapping.getParameterName());
						queryParameter.setExample(String.valueOf(inputExample));
					}

				} else if (endpoint.getMethod().equals(EndpointHttpMethod.POST)) {
					BodyParameter bodyParameter = new BodyParameter();
					bodyParameter.setName(tsParameterMapping.getParameterName());
					operationParameter.add(bodyParameter);

					if (samples != null && !samples.isEmpty()) {
						Object inputExample = samples.get(0).getInputs().get(tsParameterMapping.getParameterName());
						String mediaType = endpoint.getContentType() != null ? endpoint.getContentType() : "application/json";
						String inputExampleSerialized = inputExample.getClass().isPrimitive() ? String.valueOf(inputExample) : JacksonUtil.toString(inputExample);
						bodyParameter.addExample(mediaType, inputExampleSerialized);
					}

				}
			}

			operation.setParameters(operationParameter);
		}

		Map<String, io.swagger.models.Response> responses = new HashMap<>();
		io.swagger.models.Response response = new io.swagger.models.Response();

		if (samples != null && !samples.isEmpty()) {
			Object outputExample = samples.get(0).getOutputs();
			String mediaType = endpoint.getContentType() != null ? endpoint.getContentType() : "application/json";
			response.example(mediaType, outputExample);
		}

		if (!StringUtils.isBlank(endpoint.getReturnedVariableName()) && endpoint.getService() != null && endpoint.getService() instanceof CustomScript) {
			String returnedVariableType = ScriptUtils.getReturnedVariableType(endpoint.getService(), endpoint.getReturnedVariableName());

			Model returnModelSchema;

			if (ClassUtils.isPrimitiveOrWrapperType(returnedVariableType)) {
				returnModelSchema = SwaggerHelper.buildPrimitiveResponse(endpoint.getReturnedVariableName(), returnedVariableType);
				returnModelSchema.setReference("primitive");

			} else {

				CustomEntityTemplate returnedCet = customEntityTemplateService.findByDbTablename(returnedVariableType);
				if (returnedCet != null) {
					returnModelSchema = cetToModel(returnedCet);

				} else {
					returnModelSchema = SwaggerHelper.buildObjectResponse(endpoint.getReturnedVariableName());
				}
			}

			response.setResponseSchema(returnModelSchema);
		}

		responses.put("" + HttpStatus.SC_OK, response);

		Swagger swagger = new Swagger();
		swagger.setInfo(info);
		swagger.setBasePath(baseUrl);
		swagger.setSchemes(Arrays.asList(Scheme.HTTP, Scheme.HTTPS));
		swagger.setProduces(Collections.singletonList(endpoint.getContentType()));
		if (endpoint.getMethod() == EndpointHttpMethod.POST) {
			swagger.setConsumes(Arrays.asList("application/json", "application/xml"));
		}
		swagger.setPaths(paths);
		swagger.setResponses(responses);

		return swagger;
	}
}
