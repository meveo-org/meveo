# Notifications

When an event is triggered,  an object, described in the following table, is attached to an event for use in notification scripts and EL expressions: 

| Event type | Apply to | Description | Event object |
|:----------- | :------ | :---------- | :----------- |
| Created | Any Observable entity (i.e. class of org.meveo.model.* packages) | Fired when an entity is persisted in database (but before transaction is commited) | The entity persisted |
| Disabled | Any Observable entity | Fired when an entity is disabled | The entity disabled |
| Inbound request | Not applicable | Fired when an inbound request is received and before the response is produced | The inbound request entity |
| Logged In | Not applicable | Fired when a user successfully loggedin | The user entity |
| Processed | TBD | | |
| Removed | Any Observable  entity | Fired when an entity is deleted from database (but before transaction is commited) | The deleted entity |
| Updated | Any Observable  entity | Fired when an entity is updated in database (but before transaction is commited) | The entity updated|
| Enabled | Any Observable entity | Fired when an entity is enabled | The entity enabled |
| Install | MeveoModule entity | Fired while the module is being installed | The module being installed |
| Post install | MeveoModule entity |Fired after the module is installed | The module being installed |
| File upload | MeveoFtpFile | Fired when a file is uploaded to the meveo server | The MeveoFtpFile being uploaded |
| File download | MeveoFtpFile | Fired when a file is downloaded from the meveo server | The MeveoFtpFile being downloaded |
| File rename | MeveoFtpFile | Fired when a file is renamed in the meveo server | The MeveoFtpFile being renamed |
| File delete | MeveoFtpFile | Fired when a file is deleted in the meveo server | The MeveoFtpFile being deleted |
| Terminated | Not implemented yet | | |
| Rejected | Not implemented yet | | |
| Rejected CDR | Not implemented yet | | |
| Low balance | Not implemented yet | | |
| Counter deduce | Not implemented yet | | |
| End of term | Not implemented yet | | |

Notification allow to perform some action, e.g send a message, when some event is triggered
e.g. when an entity is created.

A Notification is associated to a type of event and a class and has an EL to filter on what condition it is triggered.
It contains some parameters that are EL feeding a context, a script that is executed before the notification is sent.
It can be associated to a counter that allow to limit the trigger rate.
It is associated to a list of NofificationHistory for audit purpose.
This entity is subclassed for extra parameters specific to [EmailNotification,...](../../../../../../../../..//meveo-model/src/main/java/org/meveo/model/notification/)

Technically the events are [CDI events](https://docs.oracle.com/javaee/6/tutorial/doc/gkhic.html).
An [observer](./DefaultObserver.java) has a set of methods that observes CDI event according
to their [qualifiers](../../../../../../../../../meveo-model/src/main/java/org/meveo/event/qualifier/) .
Each one on those observing method call the checkEvent method for the event type corresponding to the qualifier
and the object that is at the origin of the event :
* the entity, neo4J entity or relation, git Branch that has been CREATED, UPDATED, REMOVED, TERMINATED, ENABLED, DISABLED
* the jobResult of the job that has been PROCESSED
* the inputRequest that has been INBOUND_REQ
* the commitEvent for git events (looks like they are not yet used in notification triggering though [BUG])
* the MeveoFtpFile for FTP related events FILE_UPLOAD, FILE_DOWNLOAD, FILE_RENAME, FILE_DELETE
* the counter instance when a counter is COUNTER_DEDUCED

This method lookup all notification instances that match the event type and whose class is assignable from the class
of the object or any of its interface.

Then for each of those matching notification ordered by ascending priority :
* check its application EL on a context containing the single variable "event" set to the object evaluate to true.
* if a counter is set, decrease it and check it is not 0
* if the notification is not a webhook, execute it associated script on a context containing the parameters of the
 notification evaluated on a context containing the 2 variables
"event" (the object) and  "manager" (an instance of [BeanManager](https://docs.oracle.com/javaee/7/api/javax/enterprise/inject/spi/BeanManager.html))
* if the notification is a Script Notification of type Inbound request the "RESULT" variable of the script context
 is added the response

when triggering the following notifications, all params of the notification are evaluated on a context containing
the object in the "event" variable and the params and script context in a "context" variable
* if it is a EmailNotification an [email is sent](./EmailNotifier.java)
* if it is a WebHook, the webhook url [is called](./WebHookNotifier.java)
* if it is a InstantMessagingNotification, a [message is sent](./InstantMessagingNotifier.java)
* if it is a JobTrigger the job [is executed](./JobTriggerLauncher.java)
* if it is a WebNotification, a [message is sent](../communication.impl/SseManager.java) to connected users.

Note that the classes extending NotificationInstanceService ([EmailNotificationService](./EmailNotificationService.java),...) are used to persist the
notifications both in datastore and cache and to handle their associated counter.

## Web notification

when a web notification is created and active, the web clients can connect to its channel either using SSE or Websocket

### SSE 
To use Server Sent Event, the client send a GET request to the url `/sse/register/<notificationCode>?filter=<filterEL>`
where notificationCode is the code of the web notification
and filterEL is a base64 encode EL that will be applied to context of the event prior to sending it.

then [it will receive all SSE events](https://www.w3schools.com/html/html5_serversentevents.asp) triggered by the notification, with
```
id : either a UUID or a timestamp depending on the notification Id strategy
event : code of the notification
data : the notification data EL applied to the event context
: the description of the notification
```

in order to send a message to the channel of the web notification (in case the web notification allow publication),
a client can post a message (plain text body) to the url  `/sse/publish/<notificationCode>`
The message will be broadcasted to the other users connected to the notification by SSE if their
 filter match on the context with "PUBLICATION_MESSAGE" being the message sent and "PUBLICATION_AUTHOR" set to the username of the sender.

### Websocket
To use websocket, the client connects to `/websocket/<notificationCode>?filter=<filterEL>` 
it will receive message triggered by the notification that match the filter in the form `{"id":<id>,"name":<notificationCode>,"data":<message>"}`
all messages sent by the client are broadcasted to the other users connected to the notification by websocket if their
 filter match on the context with "PUBLICATION_MESSAGE" being the message sent and "PUBLICATION_AUTHOR" set to the username of the sender.
 
 The filterEl has a context containing the following variables:
 
 - event : the object or event triggering the notification
 - all parameters of the notifications
 
 
## Script notifications

Notification executes a script in SYNCHRONOUS mode. Script must implement org.meveo.script.ScriptInterface. All init(), execute() and finalize() methods are called

The following variables are available to the script methods:

* parameters defined in Script parameters field indicating parameter name and value. Script parameter value is an EL expression with an event object, described in a section above, exposed as variable "event" and bean manager exposed as "manager" variable.

Note: notification can be used to handle inbound requests, as script is executed and notification history is created SYNCHRONOUSLY from firing an event.

## Webhooks

Notification that calls a web URL in ASYNCHRONOUS mode. Headers and request parameters can be set indicating header or a request parameter name and a value. Header or a request parameter value is an EL expression with an event object, described in a section above, exposed as variable "event". Values are encoded in UTF-8.

If username is set and not “Authorization”  header has been set, then username and password are used to create an HTTP Basic authentication “Authorization” header.

A script can be executed AFTER a successful call to a web url (http response code 200).  Script must implement org.meveo.script.ScriptInterface.execute() method.

The following variables are available to the script methods:

* parameters defined in Script parameters field indicating parameter name and value. Script parameter value is an EL expression with an event object, described in a section above, exposed as variable "event". Web call response is exposed as variable "result" in both EL expression and script execution context.
    web call response is available as variable "result"

To limit the number of notification created in some time period, a counter (of type notification) can be associated to the notification.

Note: script is executed ASYNCHRONOUSLY from firing an event.

## Email notifications

Notification sends an email in ASYNCHRONOUS mode. Most fields (subject, html body, text body, emailToEL) are EL expressions with an event object, described in a section above, exposed as variable "event".

Notification executes a script in SYNCHRONOUS mode BEFORE the email is send. Script must implement org.meveo.script.ScriptInterface. All init(), execute() and finalize() methods are called.

The following variables are available to the script methods:

* parameters defined in Script parameters field indicating parameter name and value. Script parameter value is an EL expression with an event object, described in a section above, exposed as variable "event" and bean manager exposed as "manager" variable.

To limit the number of notification created in some time period, a counter (of type notification) can be associated to the notification.

## Job triggers

Notifications that launch a job in ASYNCHRONOUS mode.

Notification executes a script in SYNCHRONOUS mode BEFORE the job is launched. Script must implement org.meveo.script.ScriptInterface. All init(), execute() and finalize() methods are called.

The following variables are available to the script methods:

* parameters defined in Script parameters field indicating parameter name and value. Script parameter value is an EL expression with an event object, described in a section above, exposed as variable "event" and bean manager exposed as "manager" variable.

 
## Inbound requests

Notifications with event type filter as "Inbound request" handle any request made to "/inbound/..." path of Opencell application similarly to an event and handeled further by notification.

Inbound request returns http response code 404 if no notification was matched.

Inbound request considers to be processed and returns http response code of 200 if some notification was matched.

InboundRequest type object is created from request information and is passed to a notification as event object. The following information is filled from JAVA Http servlet request object. For more details see http://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServletRequest.html.

* content length - the length, in bytes, of the request body and made available by the input stream, or -1 if the length is not known
* content type - the MIME type of the body of the request, or null if the type is not known
* parameters - a map of request parameters. Multi-value parameter values are concatenated with "|" symbol.
* protocol - the name and version of the protocol the request uses in the form protocol/majorVersion.minorVersion, for example, HTTP/1.1
* scheme - the name of the scheme used to make this request, for example http or https
* remote address - the Internet Protocol (IP) address of the client or last proxy that sent the request.
* remote port - the Internet Protocol (IP) source port of the client or last proxy that sent the request
* body - request body
* method - http method with which this request was made, for example, GET, POST, or PUT
* authentication type -  the name of the authentication scheme used
* cookies - a map of cookies send with request
* headers - a map of headers present in request
* path info - any extra path information associated with the URL the client sent when it made this request. The extra path information follows the servlet path but precedes the query string and will start with a "/" character.
* request URI -  the part of this request's URL from the protocol name up to the query string in the first line of the HTTP request

The inbound request's response (part of http response code 200) can be enriched with the following information IF InboundRequest type object was exposed entirely as a script parameter:

* response encoding - defaults to request character encoding value
* response cookies - a map of additional cookies to return with response
* response headers - a map of headers to return in response
* response body - content to send as response body

Note: Inbound request processing is available with Script type notifications ONLY.


## Integration with modules

When a notification is installed using a module, it won't be triggered for the entities of the module during the installation