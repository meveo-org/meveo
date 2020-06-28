# Notifications

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

when a web notification is created and active, the web clients can connect to its channel using the
url `/sse/register/<notificationCode>?filter=<filterEL>`
where notificationCode is the code of the web notification
and filterEL is a base64 encode EL that will be applied to context of the event prior to sending it.

then it will receive all SSE events triggering the notification, with
```
id : either a UUID or a timestamp depending on the notification Id strategy
event : code of the notification
data : the notification data EL applied to the event context
: the description of the notification
```

in order to send a message to the channel of the web notification (in case the web notification allow publication),
a client can post a message (plain text body) to the url  `/sse/publish/<notificationCode>`
