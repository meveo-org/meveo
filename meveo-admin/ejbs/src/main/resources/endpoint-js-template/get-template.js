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
	 return #{REQUEST_SCHEMA}
};