const EVENT = {
  SUCCESS: "get#{ENDPOINT_CODE}-SUCCESS",
  ERROR: "get#{ENDPOINT_CODE}-ERROR"
};

export const registerEventListeners = (
  component,
  successCallback,
  errorCallback
) => {
  if (successCallback) {
    component.addEventListener(EVENT.SUCCESS, successCallback);
  }
  if (errorCallback) {
    component.addEventListener(EVENT.ERROR, errorCallback);
  }
};

export const getRequestSchema = async (parameters, config) => {
	 return {	 
		 return {
		"name": "#{ENDPOINT_CODE}Request",
		"description": "#{ENDPOINT_DESCRIPTION}",
		 #{REQUEST_SCHEMA}
	 }
};

export const getResponseSchema = async (parameters, config) => {
	return {
		"name": "#{ENDPOINT_CODE}Response",
		"description": "#{ENDPOINT_DESCRIPTION}",
		#{RESPONSE_SCHEMA}
	}
}