# Websocket endpoint

Websocket endpoints allow realtime bidirectional communications between a user agent and a meveo instance server.
Although this can be achieved by the user agent calling some Rest endpoint on the server and the server notifying the user agent using webnotifications (either on top SSE or Websocket),
the Websocket endpoints are usefull when we want to implement direct communication between the user agents as the meveo Websocket service allow users to send messages to connected messages.

## Websocket logic

You first need to implement a function in order to be able to create a Websocket endpoint
In meveo admin you can create a Function by using the "Services/Functions" menu
an empty script logging the parameters is enough to start

```
package com.mycompany.websocket;

import java.util.Map;

import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WSExchange extends Script {
  
    private static final Logger log = LoggerFactory.getLogger(WSExchange.class);
  
	public void execute(Map<String, Object> parameters) throws BusinessException {
	  log.info("params:{}",parameters);
  }
}
```

## Websocket endpoint creation

In meveo admin you can create a Websocket endpoint by using the "Services/Endpoints/Websocket" menu

Like a Rest endpoint, a Websocket endpoint, has a name, can be secured and is associated to the function that perform its logic.

For rest API, use the `https://<host>:<port>/meveo/api/rest/wsendpoint` POST endpoint with body
```
{
    "code": "mywsendpoint",
    "description": "my websocket endpoint",
    "secured": false,
    "serviceCode": "com.mycompany.websocket.WSExchange",
    "roles": []
}
```

## Lifecycle

on the server side, the websocket will need to react to  `onOpen`, `onMessage`, `onError` and `onClose` events

### onOpen
A client opens a websocket using the url `ws://<host>:<port>/meveo/ws/<wsEndointName>` where `<wsEndointName>` is the name of the websocket endpoont
this will call the `execute` method of the associated function with the following parameters
* `WS_EVENT` : "open"
* `WS_SESSION` : the [websocket session](https://docs.oracle.com/javaee/7/api/javax/websocket/Session.html)

If the function throws an exception then the websocket is closed.

If you want to store variables in the session you can simply add them to the parameters map of the execute method: 
this map is stored in the session userProperties under the "context" key.


### onMessage
when a client sends a message on an open socket, `execute` method of the associated function with the parameters that resulted from the call to the function when opening and
* `WS_EVENT` : "message"
* `WS_MESSAGE` : the text message sent by the client

This imply for instance that the parameters still contain the websocket session under the `WS_SESSION` key.

### onClose
Whe the server is notified that the session is closes, `execute` method of the associated function with the parameters that resulted from the call to the function when opening and
* `WS_EVENT` : "close"
* `WS_REASON_CODE` : closing reason code
* `WS_REASON_PHRASE` : closing reason phrase
and the session is closed.

### onError
Whe the server is notified of a session error, `execute` method of the associated function with the parameters that resulted from the call to the function when opening and
* `WS_EVENT` : "error"
* `WS_ERROR` : the description of the error
and the session is closed.

## Sending message

You can send message to a websocket client from any script by using the method 
```
public void sendMessage(String enpointCode, String username, String txtMessage) 
```
of the `WebsocketServerEndpoint` service 




