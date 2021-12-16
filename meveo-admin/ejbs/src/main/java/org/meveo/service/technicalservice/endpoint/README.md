# Endpoint

An endpoint allows a user to expose any function (script or technical service) as a REST endpoint. The endpoint's path can be configured along with the method to access it (POST or GET), whether the execution should be synchronous or asynchronous, and map input parameters of the function to the input and/or path parameters of the endpoint.

It can be executed in synchronous or asynchonous mode. In asynchronous mode, a unique uuid is returned which can then be used to query later.

## How to define input for a script

When writing a script, any setter (methods that start with "_set_") will be considered as an input. To add description to this input, we can simply write a Javadoc for the setter.

Some parameters are set in the input parameters indicating the delay and budget allowed for the execution :

 - maxBudget (Double)  : come from request header **Budget-Max-Value** 
 - budgetUnit (String) : in Joule if not set, come from header **Budget-Unit**
 - maxDelay (Long)  : come from request header **Delay-Max-Value** 
 - delayUnit ([TimeUnit](https://docs.oracle.com/en/java/javase/11/docs/api/java.base/java/util/concurrent/TimeUnit.html)) : in Second if not set, come from header **Delay-Unit**
 
It is the responsibility of the Script to implement that its execution is done within the given budget and delay.

In case the script is executed in an asynchronous way, then after the max delay in case the execute method has not yet returned
then the cancel method of the script is called. It should stop the execution and return immediatly the current result.

If the script extends [EndpointScript](../../../../../../../../../../meveo-api/src/main/java/org/meveo/api/rest/technicalservice/EndpointScript.java)
then the [EndpointRequest](../../../../../../../../../../meveo-api/src/main/java/org/meveo/api/rest/technicalservice/impl/EndpointRequest.java) is set , and in case the call is synchronous the EndpointResponse is set.
If the script does not extend EndpointScript then the request (and response in synchronous case) are set
in the parameters `request` and `response` respectively.

```
import org.meveo.api.rest.technicalservice.impl.EndpointRequest;
	
   public void execute(Map<String, Object> parameters) throws BusinessException {
	EndpointRequest req = (EndpointRequest) parameters.get("request");
	...
   }
```

## GUI and API

CRUD for endpoint is available on both GUI and API.

The endpoint API is **/api/rest/endpoint**. It is implemented in the class **org.meveo.api.rest.technicalservice.impl.EndpointRs** and accepts a dto of type **org.meveo.api.dto.technicalservice.endpoint.EndpointDto** for create and update REST operation.

## How to use an Endpoint

See class org.meveo.api.rest.technicalservice.EndpointServlet.

The path to access the exposed endpoint depends on the configuration of the endpoint itself. It will always start with "/rest/", followed by the basePath of the endpoint (which is its code if not overriden), and the path parameter (wichi is just the ordered list of path parameters if not overridem).
An endpoint is accessible via the URL <meveoURL>/rest/<endpoint.basePath><endpoint.path>.

If the endpoint was defined as GET, the parameters must be passed as query parameters, 
and if it was defined as POST or PUT the parameters must be passed as JSON in the body of the request.

There are several headers that was defined to modify the default behavior of the endpoint:

- **Keep-Data:** indicates we don't want to remove the execution result from cache
- **Wait-For-Finish:** indicates that we want to wait until one execution finishes and get results after. (Otherwise returns status 102)
- **Persistence-Context-Id:** indicates the id of the persistence context we want to save the result
 
To retrieve the result of an asynchronous request, call the servlet “**/rest/**” followed by the id previously returned

## Endpoint security
The endpoint can be configured to be secured and only allow REST clients that are authenticated.

To enable this feature, the **endpointSecurityEnabled** property found in **{wildfly directory}/standalone/configuration/meveo-admin.properties** should be set to **true**. It can also be configured on the meveo admin GUI.  To do that, select **Configuration > Settings > System settings**.

Once **endpointSecurityEnabled** is enabled, select the **Secured** setting on the GUI, or set it to true in the JSON configuration when using the API as shown in the examples below.

## Examples

For the following examples, let’s consider that we have three setters defined, and therefore three inputs named companyName, year and location and that companyName has been defined as a path parameter.

### GET Synchronous endpoint

We should first call the creation rest service `POST on /endpoint` with JSON: 

```
{
	"serviceCode": "fr.score3",
	"code": "myEndpoint",
	"synchronous": true,
	"method": "GET",
	"secured": true,
	"pathParameters": [
		{
			"property": "companyName",
			"required": true
		}
	],
	"parameterMappings": [
		{
			"serviceParameter": {
				"property": "year",
				"required": true
			},
			"parameterName": "creationDate",
			"defaultValue": "2010"
		},
		{
			"serviceParameter": {
				"property": "location",
				"required": false
			},
			"parameterName": "headOffice",
			"defaultValue": "Paris"
		}
	]
	
}
```

So, the endpoint generated will be accessible with GET method under **/rest/myEndpoint/theCompanyName?creationDate=2011&headOffice=Dijon** and the result will be returned once the script has been executed.

Both basePath and path could be set

### POST Asynchronous endpoint

We should first call the creation rest service with JSON:

```
{
	"serviceCode": "fr.score3",
	"code": "myEndpoint",
	"synchronous": true,
	"method": "POST",
	"secured": true,
	"pathParameters": [
		{
			"property": "companyName",
			"required": true
		}
	],
	"parameterMappings": [
		{
			"serviceParameter": {
				"property": "year",
				"required": true
			},
			"parameterName": "creationDate",
			"defaultValue": "2010"
		},
		{
			"serviceParameter": {
				"property": "location",
				"required": false
			},
			"parameterName": "headOffice",
			"defaultValue": "Paris"
		}
	]
	
}
```

So, the endpoint generated will be accessible with POST method under **/rest/myEndpoint/theCompanyName**

The request of the body should be:

```
{
	"creationDate": 2011,
	"headOffice": "Dijon"
}
```

The execution will return an UUID like **6bbb2e71-8361-4d51-887d-91c2a52d08f0** and the script will be executed. So, to retrieve the result, we need to call the endpoint **/rest/6bbb2e71-8361-4d51-887d-91c2a52d08f0**. If the execution is still not finished when we make the request, it will return with status code **102**. If we want to wait until finish and get result, we must set the header **Wait-For-Finish** to **true**.

## How to get the POST or PUT body in Script

The POST body parameters are added into the context map parameter of the Script's execute method. In our example above, if we call the POST API with the following JSON body:

```
{
	"creationDate": 2011,
	"headOffice": "Dijon"
}
```

Then we can access both JSON fields in the script as:

```
context.get("creationDate");
context.get("headOffice");
```

if you need the original body you can retrieve it from the `REQUEST_BODY` parameter.

	
	
## JSONata

In the field  **jsonataTransformer**  you can provide a JSONata expression to transform the output ofthe endpoint.
	
## OpenAPI definition
	
Go to ```GET /meveo/api/rest/endpoint/openApi/{code}```
