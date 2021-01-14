# Endpoint

An endpoint allows a user to expose any function (script or technical service) as a REST endpoint. He can configure the path that will be exposed, the method to access it (POST or GET), whether the execution should be synchronous or asynchronous, and make some mappings about input parameters of the function and the input parameters of the endpoint.

It can be execute in synchronous or asynchonous mode. In asynchronous mode, a random Uuid is return that can be query later.

## How to define input for a script

When writing a script, any setter (method starting by "set") will be considered as an input. To add description to this input, we can simply write a Javadoc for the setter.

## GUI and API

CRUD for endpoint is available on both GUI and API.

The endpoint API is /api/rest/endpoint. It is implemented in the class org.meveo.api.rest.technicalservice.impl.EndpointRs and accepts a dto of type org.meveo.api.dto.technicalservice.endpoint.EndpointDto for create and update REST operation.

## How to use an Endpoint

See class org.meveo.api.rest.technicalservice.EndpointServlet.

The path to access the exposed endpoint depends on the configuration of the endpoint itself. It will always start with "/rest/", followed by the basePath of the endpoint (which is its code if not overriden), and the path parameter (wichi is just the ordered list of path parameters if not overridem).
An endpoint is accessible via the URL <meveoURL>/rest/<endpoint.basePath><endpoint.path>.

If the endpoint was defined as GET, the parameters must be passed as query parameters, 
and if it was defined as POST or PUT the parameters must be passed as JSON in the body of the request.

There are several headers that was defined to modify the default behavior of the endpoint: 

 - **Keep-Data:** indicates we don't want to remove the execution result from cache
 - **Wait-For-Finish:** indicates that we want to wait until one execution finishes and get results after. (Otherwise returns status 102)
 - **Persistence-Context-Id:** indicates the id of the persistence context we want to save theresult
 
To retrieve the result of an asynchronous request, call the servlet “/rest/” followed by the id previously returned

## Examples

For the following examples, let’s consider that we have three setters defined, and therefore three inputs named companyName, year and location and that companyName has been defined as a path parameter.

### GET Synchronous endpoint

We should first call the creation rest service `POST on /endpoint` with JSON: 

```
{
	"serviceCode": "fr.score3",
	"code": "get-synchronous-endpoint",
	"synchronous": true,
	"method": "GET",
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

So, the endpoint generated will be accessible with GET method under /rest/get-synchronous-endpoint/Webdrone?creationDate=2011&headOffice=Dijon and the result will be returned once the script has been executed.

Both basePath and path coulde

### POST Asynchronous endpoint

We should first call the creation rest service with JSON:

```
{
	"serviceCode": "fr.score3",
	"code": "post-synchronous-endpoint",
	"synchronous": true,
	"method": "POST",
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

So, the endpoint generated will be accessible with POST method under /rest/post-asynchronous-endpoint/Webdrone

The request of the body should be:

```
{
	"creationDate": 2011,
	"headOffice": "Dijon"
}
```

The execution will return an UUID like “6bbb2e71-8361-4d51-887d-91c2a52d08f0” and the script will be executed. So, to retrieve the result, we need to call the endpoint /rest/6bbb2e71-8361-4d51-887d-91c2a52d08f0. If the execution is still not finished when we make the request, it will return with code 102. If we want to wait until finish and get result, we must set the header “Wait-For-Finish” to true.

## How to get the POST body in Script

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

## JSONata

In the field  jsonataTransformer  you can provide a JSONata expression to transform the output ofthe endpoint.