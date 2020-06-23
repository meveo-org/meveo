# Notifications

Notification allow to perform some action, e.g call a script or send a message, when some event is triggered
e.g. when a entity is created._

Technically the events are [CDI events](https://docs.oracle.com/javaee/6/tutorial/doc/gkhic.html).
An [observer](./DefaultObserver.java) has a set of methods that observes CDI event according
to their [qualifiers](../../../../../../../../../meveo-model/src/main/java/org/meveo/event/qualifier/) .
Each one on those observing method call the checkEvent method for the event type corresponding to the qualifier
and the object that is at the origin of the event :
* the entity, neo4jentity or relation, giyBranch that has been CREATED, UPDATED, REMOVED, TERMINATED, DISABLED
* the jobResult of the job that has been PROCESSED
* the inputRequest that has been INBOUND_REQ
* the commitEvent for git events (looks like they are not yet used in notification triggering though [BUG])
* the MeveoFtpFile for FTP related events FILE_UPLOAD, FILE_DOWNLOAD, FILE_RENAME, FILE_DELETE

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
* if it is a EmailNotification an email is sent
* if it is a WebHook, the webhook url is called
* if it is a InstantMessagingNotification, a message is sent
* if it is a JobTrigger the job is executed

