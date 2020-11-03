/**
 * Extracts the value from parameters based on property name.
 *
 * @param {*} parameters an object that contains the values that will be parsed
 * @returns a reduce function that extracts the value that correspond
 * to a property and adds it to the resulting request parameters object
 */
function mapProperties(parameters) {
	return function(requestParameters, property) {
		const value = parameters[property];
		if (!!value) {
			return { ...requestParameters, [property]: value };
		}
		return requestParameters;
	};
}
/**
 * Traverse schema and refSchemas and extracts their keys.
 *
 * @param {*} schema the schema object that holds the properties that will be
 * extracted from the parameters
 * @param {*} refSchemas the list of parent schemas that are referenced in the
 * schema
 * @returns an object with only the parameter values that are defined
 * in the schema properties
 */
function getSchemaKeys(schema, refSchemas) {
	const hasParentSchema = !!schema.allOf && schema.allOf.length > 0;
	const keys = Object.keys(schema.properties);
	if (hasParentSchema) {
		return schema.allOf.reduce(
			(allKeys, schemaId) => {
				const parentSchema = refSchemas.find(
					(refSchema) => refSchema.id === schemaId["$ref"]
				);
				return [...allKeys, ...getSchemaKeys(parentSchema, refSchemas)];
			},
			[...keys]
		);
	}
	return keys;
}
/**
 * Retrieves the custom config object from the config.js file on the root of
 * the app if specified
 *
 * @param {*} endpoint the EndpointInterface object
 * @param {*} parameters the parameters passed in by the client
 */
function buildEndpointConfig(endpoint, parameters) {
	const { name } = endpoint;
	const { config } = parameters;
	const getEndpointConfig = (config || {})[name] || (() => { });
	const endpointConfig = getEndpointConfig({ endpoint, parameters });
	return endpointConfig || {};
}
/**
 * Traverse schema.properties and extracts the values
 * from the parameters object. It only traverses the top-level
 * properties it does not check child nodes
 *
 * @param {*} parameters an object that contains the parameter values
 * @param {*} endpoint the endpoint object
 * @returns an object with only the parameter values that are defined
 * in the schema properties
 */
function buildRequestParameters(parameters, endpoint) {
	const { requestSchema, refSchemas } = endpoint;
	if (requestSchema) {
		const keys = getSchemaKeys(requestSchema, refSchemas);
		const endpointConfig = buildEndpointConfig(endpoint, parameters);
		const { decorateProperties } = endpointConfig;
		const props = keys.reduce(mapProperties(parameters), {});
		const properties = !!decorateProperties
			? decorateProperties(props, parameters, endpoint)
			: props;
		return properties;
	}
	return null;
}
/**
 * Generate a normalized string made up of capital letters
 * with no spaces and special characters
 *
 * @param {*} text string to be normalized
 * @returns normalized string
 */
function normalize(text) {
	return (text || "")
		.toUpperCase()
		.replace(/\s+/g, "_")
		.replace(/[^ -~]+/g, "");
}
/**
 * Generate the api URL using the base endpoint URL and then appending
 * any included path parameters
 *
 * @param {*} endpoint the EndpointInterface object
 * @param {*} parameters the parameters passed in by the client
 * @returns
 */
function buildApiUrl(endpoint, parameters) {
	const { endpointUrl, pathParameters } = endpoint;
	const endpointConfig = buildEndpointConfig(endpoint, parameters);
	const { OVERRIDE_URL } = endpointConfig;
	const hasPathParameters = !!pathParameters && pathParameters.length > 0;
	let apiUrl = endpointUrl;
	if (!OVERRIDE_URL && hasPathParameters) {
		const parameterMap = pathParameters.reduce(mapProperties(parameters), {});
		const pathParams =
			Object.keys(parameterMap)
				.map(function(key) {
					return parameterMap[key];
				})
				.join("/") || "";
		if (!!pathParams) {
			apiUrl = `${endpointUrl}/${pathParams}`;
		}
	}
	return OVERRIDE_URL || apiUrl;
}
/**
 * The base class for Requests
 *
 * @class ApiRequest
 */
class ApiRequest {
	constructor(endpoint) {
		this.endpoint = endpoint;
	}
	/**
	 * Builds an Http Request header based on the user defined headers of the default headers
	 *
	 * @param {*} parameters an object contains the authorization token and configs
	 * @returns an http Header object with corresponding properties defined
	 * @memberof ApiRequest
	 */
	buildHeaders(parameters) {
		const { token, noAuth } = parameters;
		const endpointConfig = buildEndpointConfig(this.endpoint, parameters);
		const { OVERRIDE_HEADER } = endpointConfig;
		const headers = new Headers();
		const appendHeaders = OVERRIDE_HEADER || {
			"Content-Type": "application/json",
			Accept: "application/json",
		};
		if (!noAuth) {
			appendHeaders.Authorization = `Bearer ${token}`;
		}
		Object.keys(appendHeaders).forEach(function(key) {
			headers.append(key, appendHeaders[key]);
		});
		return headers;
	}
	/**
	 * The main method for calling a fetch request to the API
	 *
	 * @param {*} requestUrl an Http URL object
	 * @param {*} options object to be passed into the fetch request
	 * contains the Http method, headers, and body
	 * @memberof ApiRequest
	 */
	callApi(requestUrl, options) {
		const { component, endpointUrl, successEvent, errorEvent } = this.endpoint;
		fetch(requestUrl, options)
			.then(function(response) {
				if (!response.ok) {
					throw [
						`Encountered error calling API: ${endpointUrl}`,
						`Status code: ${response.status} [${response.statusText}]`,
					];
				}
				return response.json();
			})
			.then(function(result) {
				component.dispatchEvent(
					new CustomEvent(successEvent, { detail: { result }, bubbles: true })
				);
			})
			.catch(function(error) {
				component.dispatchEvent(
					new CustomEvent(errorEvent, { detail: { error }, bubbles: true })
				);
			});
	}
}
/**
 * The ApiRequest for GET requests
 *
 * @class GetRequest
 * @extends {ApiRequest}
 */
class GetRequest extends ApiRequest {
	constructor(endpoint) {
		super(endpoint);
	}
	/**
	 * Concrete implementation of the executeRequest interface specifically for Http GET requests
	 *
	 * @param {*} parameters
	 * @memberof GetRequest
	 */
	executeRequest(parameters) {
		const { mock } = this.endpoint;
		const requestParameters = buildRequestParameters(parameters, this.endpoint);
		const parameterKeys = Object.keys(requestParameters || {});
		const hasParameters = requestParameters && parameterKeys.length > 0;
		const apiUrl = buildApiUrl(this.endpoint, parameters);
		const requestUrl = new URL(apiUrl);
		if (hasParameters) {
			parameterKeys.forEach(function(key) {
				requestUrl.searchParams.append(key, requestParameters[key]);
			});
		}
		if (mock) {
			requestUrl.searchParams.append("mock", true);
		}
		const headers = this.buildHeaders(parameters);
		const options = { method: "GET", headers };
		this.callApi(requestUrl, options);
	}
}
/**
 * The ApiRequest for POST requests
 *
 * @class PostRequest
 */
class PostRequest extends ApiRequest {
	constructor(endpoint) {
		super(endpoint);
	}
	/**
	 * Concrete implementation of the executeRequest interface specifically for Http POST requests
	 *
	 * @param {*} parameters
	 * @memberof PostRequest
	 */
	executeRequest(parameters) {
		const { mock } = this.endpoint;
		const requestParameters = buildRequestParameters(parameters, this.endpoint);
		const apiUrl = buildApiUrl(this.endpoint, parameters);
		const requestUrl = new URL(apiUrl);
		if (mock) {
			requestUrl.searchParams.append("mock", true);
		}
		const headers = this.buildHeaders(parameters);
		const options = {
			method: "POST",
			headers,
			body: JSON.stringify(requestParameters),
		};
		this.callApi(requestUrl, options);
	}
}
/**
 * The ApiRequest for DELETE requests
 *
 * @class DeleteRequest
 */
class DeleteRequest extends ApiRequest {
	constructor(endpoint) {
		super(endpoint);
	}
	/**
	 * Concrete implementation of the executeRequest interface specifically for Http DELETE requests
	 *
	 * @param {*} parameters
	 * @memberof DeleteRequest
	 */
	executeRequest(parameters) {
		const { mock } = this.endpoint;
		const requestParameters = buildRequestParameters(parameters, this.endpoint);
		const parameterKeys = Object.keys(requestParameters || {});
		const hasParameters = requestParameters && parameterKeys.length > 0;
		const apiUrl = buildApiUrl(this.endpoint, parameters);
		const requestUrl = new URL(apiUrl);
		if (hasParameters) {
			parameterKeys.forEach(function(key) {
				requestUrl.searchParams.append(key, requestParameters[key]);
			});
		}
		if (mock) {
			requestUrl.searchParams.append("mock", true);
		}
		const headers = this.buildHeaders(parameters);
		const options = { method: "DELETE", headers };
		this.callApi(requestUrl, options);
	}
}
/**
 * The ApiRequest for PUT requests
 *
 * @class PutRequest
 */
class PutRequest extends ApiRequest {
	constructor(endpoint) {
		super(endpoint);
	}
	/**
	 * Concrete implementation of the executeRequest interface specifically for Http PUT requests
	 *
	 * @param {*} parameters
	 * @memberof PutRequest
	 */
	executeRequest(parameters) {
		const { mock } = this.endpoint;
		const requestParameters = buildRequestParameters(parameters, this.endpoint);
		const apiUrl = buildApiUrl(this.endpoint, parameters);
		const requestUrl = new URL(apiUrl);
		if (mock) {
			requestUrl.searchParams.append("mock", true);
		}
		const headers = this.buildHeaders(parameters);
		const options = {
			method: "PUT",
			headers,
			body: JSON.stringify(requestParameters),
		};
		this.callApi(requestUrl, options);
	}
}
const REQUEST_TYPE = {
	GET: GetRequest,
	POST: PostRequest,
	DELETE: DeleteRequest,
	PUT: PutRequest,
};
/**
 * The base class for endpoint interface classes.  To use it, create a subclass and initialize
 * the name and Http method of the endpoint via constructor.
 *
 * @export
 * @class EndpointInterface
 */
export default class EndpointInterface {
	constructor(name, method = "GET") {
		const eventName = normalize(name);
		this.name = name;
		this.endpointUrl = `#{API_BASE_URL}/rest/${name}`;
		this.successEvent = `${eventName}_SUCCESS`;
		this.errorEvent = `${eventName}_ERROR`;
		this.method = method;
	}
	/**
	 * The main method to call an API.
	 *
	 * @param {*} component the web component that will listen and dispatch the events for this api call
	 * @param {*} params the parameters used for calling the API, main properties are token and config.
	 * All other parameters passed into this object are considered endpoint parameters which will be
	 * parsed based on the request schema
	 * token - contains the authorization token used for calling the API
	 * config - contains the configuration options for the endpoint.  The property should have the same
	 * name as the endpoint and may contain an OVERRIDE_URL and/or OVERRIDE_HEADER.
	 * e.g. Endpoint name: list-investigations
	 *      config: {"list-investigations": {OVERRIDE_URL: "http://my.other.api/override/params"}}
	 * noAuth - set to true if the endpoint does not require authorization
	 * @param {*} successCallback the function to be called when an api call is successful
	 * @param {*} errorCallback the function to be called when an api call throws an error
	 * @memberof EndpointInterface
	 */
	executeApiCall(component, params, successCallback, errorCallback) {
		const {
			name,
			endpointUrl,
			method,
			successEvent,
			errorEvent,
			mockResult,
			requestSchema,
			refSchemas,
			pathParameters,
		} = this;
		// Register event listeners
		if (successCallback) {
			component.addEventListener(successEvent, successCallback);
		}
		if (errorCallback) {
			component.addEventListener(errorEvent, errorCallback);
		}
		const parameters = params || {};
		const endpointConfig = buildEndpointConfig(this, parameters);
		const { USE_MOCK = false } = endpointConfig;
		if (USE_MOCK === "OFFLINE") {
			component.dispatchEvent(
				new CustomEvent(successEvent, {
					detail: { result: mockResult },
					bubbles: true,
				})
			);
		} else if (USE_MOCK === "ENDPOINT") {
			// Fetch from endpoint mock
			const endpointRequest = new REQUEST_TYPE[method]({
				mock: true,
				name,
				component,
				endpointUrl,
				successEvent,
				errorEvent,
				requestSchema,
				refSchemas,
				pathParameters,
			});
			endpointRequest.executeRequest(parameters);
		} else {
			// Fetch from actual endpoint
			try {
				const endpointRequest = new REQUEST_TYPE[method]({
					name,
					component,
					endpointUrl,
					successEvent,
					errorEvent,
					requestSchema,
					refSchemas,
					pathParameters,
				});
				endpointRequest.executeRequest(parameters);
			} catch (error) {
				component.dispatchEvent(
					new CustomEvent(errorEvent, { detail: { error }, bubbles: true })
				);
			}
		}
	}
}