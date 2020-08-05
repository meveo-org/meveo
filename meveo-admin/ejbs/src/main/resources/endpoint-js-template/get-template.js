import EndpointInterface from "#{API_BASE_URL}/api/rest/endpoint/EndpointInterface.js";

// the request schema, this should be updated
// whenever changes to the endpoint parameters are made
// this is important because this is used to validate and parse the request parameters
const requestSchema = #{REQUEST_SCHEMA}

// the response schema, this should be updated
// whenever changes to the endpoint parameters are made
// this is important because this could be used to parse the result
const responseSchema = #{RESPONSE_SCHEMA}

// should contain offline mock data, make sure it adheres to the response schema
const mockResult = {};

class #{ENDPOINT_CODE} extends EndpointInterface {
	constructor() {
		// name and http method, these are inserted when code is generated
		super("#{ENDPOINT_CODE}", "GET");
		this.requestSchema = requestSchema;
		this.responseSchema = responseSchema;
		this.mockResult = mockResult;
	}

	getRequestSchema() {
		return this.requestSchema;
	}

	getResponseSchema() {
		return this.responseSchema;
	}
}

export default new #{ENDPOINT_CODE}();