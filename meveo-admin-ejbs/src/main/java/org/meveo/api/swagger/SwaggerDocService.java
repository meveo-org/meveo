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
import org.meveo.model.customEntities.CustomEntityTemplate;
import org.meveo.model.persistence.JacksonUtil;
import org.meveo.model.scripts.CustomScript;
import org.meveo.model.scripts.Function;
import org.meveo.model.scripts.Sample;
import org.meveo.model.technicalservice.endpoint.Endpoint;
import org.meveo.model.technicalservice.endpoint.EndpointHttpMethod;
import org.meveo.model.technicalservice.endpoint.EndpointPathParameter;
import org.meveo.model.technicalservice.endpoint.TSParameterMapping;
import org.meveo.service.custom.CustomEntityTemplateService;
import org.meveo.service.script.ScriptUtils;
import org.meveo.util.ClassUtils;
import org.meveo.util.Version;

import io.swagger.models.Info;
import io.swagger.models.Model;
import io.swagger.models.Operation;
import io.swagger.models.Path;
import io.swagger.models.Scheme;
import io.swagger.models.Swagger;
import io.swagger.models.parameters.BodyParameter;
import io.swagger.models.parameters.Parameter;
import io.swagger.models.parameters.PathParameter;
import io.swagger.models.parameters.QueryParameter;

/**
 * Service class for generating swagger documentation on the fly.
 * 
 * @author Edward P. Legaspi | czetsuya@gmail.com
 * @since 6.8.0
 * @version 6.10
 */
@Stateless
public class SwaggerDocService {

	@Inject
	private CustomEntityTemplateService customEntityTemplateService;

	@Inject
	private SwaggerHelperService swaggerHelperService;

	public Swagger generateOpenApiJson(String baseUrl, Endpoint endpoint) {

		Info info = new Info();
		info.setTitle(endpoint.getCode());
		info.setDescription(endpoint.getDescription());
		info.setVersion(Version.appVersion);

		Map<String, Path> paths = new HashMap<>();
		Path path = new Path();

		Operation operation = new Operation();
		boolean isHeadMethod=false;

		switch(endpoint.getMethod()){
			case DELETE:
				path.setDelete(operation);
				break;
			case GET:
				path.setGet(operation);
				break;
			case HEAD:
				path.setHead(operation);
				isHeadMethod=true;
				break;
			case POST:
				path.setPost(operation);
				break;
			case PUT:
				path.setPut(operation);
				break;
			default:
				break;

		}

		if (!Objects.isNull(endpoint.getPathParametersNullSafe())) {
			for (EndpointPathParameter endpointPathParameter : endpoint.getPathParametersNullSafe()) {
				Parameter parameter = new PathParameter();
				parameter.setName(endpointPathParameter.getEndpointParameter().getParameter());
				path.addParameter(parameter);
			}
		}

		paths.put(endpoint.getEndpointUrl(), path);

		List<Sample> samples = endpoint.getService().getSamples();

		if (!Objects.isNull(endpoint.getParametersMappingNullSafe())) {
			List<Parameter> operationParameter = new ArrayList<>();

			for (TSParameterMapping tsParameterMapping : endpoint.getParametersMappingNullSafe()) {

				if (endpoint.getMethod().equals(EndpointHttpMethod.GET)) {
					QueryParameter queryParameter = new QueryParameter();
					queryParameter.setName(tsParameterMapping.getParameterName());
					queryParameter.setDefaultValue(tsParameterMapping.getDefaultValue());
					queryParameter.setFormat(ScriptUtils.findScriptVariableType(endpoint.getService(), tsParameterMapping.getEndpointParameter().getParameter()));
					operationParameter.add(queryParameter);

					if (samples != null && !samples.isEmpty()) {
						Object inputExample = samples.get(0).getInputs().get(tsParameterMapping.getParameterName());
						queryParameter.setExample(String.valueOf(inputExample));
					}

				} else if (endpoint.getMethod().equals(EndpointHttpMethod.POST)) {
					BodyParameter bodyParameter = new BodyParameter();
					bodyParameter.setName(tsParameterMapping.getParameterName());
					bodyParameter.setSchema(buildBodyParameterSchema(endpoint.getService(), tsParameterMapping));
					operationParameter.add(bodyParameter);

					if (samples != null && !samples.isEmpty()) {
						Object inputExample = samples.get(0).getInputs().get(tsParameterMapping.getParameterName());
						String mediaType = endpoint.getContentType();
						if (inputExample != null) {
							String inputExampleSerialized = inputExample.getClass().isPrimitive() ? String.valueOf(inputExample) : JacksonUtil.toString(inputExample);
							bodyParameter.addExample(mediaType, inputExampleSerialized);
						}
					}

				}
			}

			operation.setParameters(operationParameter);
		}

		Map<String, io.swagger.models.Response> responses = new HashMap<>();
		io.swagger.models.Response response = new io.swagger.models.Response();

		if ((!isHeadMethod) && (samples != null) && (!samples.isEmpty())) {
			Object outputExample = samples.get(0).getOutputs();
			String mediaType = endpoint.getContentType();
			response.example(mediaType, outputExample);
		}

		if(!isHeadMethod){
			buildResponseSchema(endpoint, response);
		}

		responses.put("" + HttpStatus.SC_OK, response);

		Swagger swagger = new Swagger();
		swagger.setInfo(info);
		swagger.setBasePath(baseUrl);
		swagger.setSchemes(Arrays.asList(Scheme.HTTPS));
		swagger.setProduces(Collections.singletonList(endpoint.getContentType()));
		if (endpoint.getMethod() == EndpointHttpMethod.POST) {
			swagger.setConsumes(Arrays.asList("application/json", "application/xml"));
		}
		swagger.setPaths(paths);
		swagger.setResponses(responses);

		return swagger;
	}

	private Model buildBodyParameterSchema(Function service, TSParameterMapping tsParameterMapping) {

		Model returnModelSchema;
		String cetCode = tsParameterMapping.getEndpointParameter().getParameter();
		String parameterDataType = ScriptUtils.findScriptVariableType(service, tsParameterMapping.getEndpointParameter().getParameter());
		
		if (ClassUtils.isPrimitiveOrWrapperType(parameterDataType)) {
			returnModelSchema = swaggerHelperService.buildPrimitiveResponse(tsParameterMapping.getParameterName(), parameterDataType);
			returnModelSchema.setReference("primitive");

		} else {

			CustomEntityTemplate returnedCet = customEntityTemplateService.findByDbTablename(cetCode);
			if (returnedCet != null) {
				returnModelSchema = swaggerHelperService.cetToModel(returnedCet);

			} else {
				returnModelSchema = swaggerHelperService.buildObjectResponse(tsParameterMapping.getParameterName());
			}
		}

		return returnModelSchema;
	}

	private void buildResponseSchema(Endpoint endpoint, io.swagger.models.Response response) {

		if (!StringUtils.isBlank(endpoint.getReturnedVariableName()) && endpoint.getService() != null && endpoint.getService() instanceof CustomScript) {

			Model returnModelSchema;
			String returnedVariableType = ScriptUtils.findScriptVariableType(endpoint.getService(), endpoint.getReturnedVariableName());

			if (ClassUtils.isPrimitiveOrWrapperType(returnedVariableType)) {
				returnModelSchema = swaggerHelperService.buildPrimitiveResponse(endpoint.getReturnedVariableName(), returnedVariableType);
				returnModelSchema.setReference("primitive");

			} else {

				CustomEntityTemplate returnedCet = customEntityTemplateService.findByDbTablename(returnedVariableType);
				if (returnedCet != null) {
					returnModelSchema = swaggerHelperService.cetToModel(returnedCet);

				} else {
					returnModelSchema = swaggerHelperService.buildObjectResponse(endpoint.getReturnedVariableName());
				}
			}

			response.setResponseSchema(returnModelSchema);
		}
	}
}
