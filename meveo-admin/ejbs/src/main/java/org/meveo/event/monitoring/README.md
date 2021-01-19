# Cluster Events

When running in a wildfly cluster, a JMS topic `CLUSTEREVENTTOPIC` is used to publish an event when

* when a role is created/updated/removed
* when jobInstance is created/updated/removed/enabled/disabled or if its timer is updated
* when a Function(Script) is created/updated/removed/enabled/disabled


A Message Driven Bean `ClusterEventMonitor` then listen to this topic and depending on event class (clazz field in the EventDTO)
* Clear the role and permissions of the currentProvider (TODO: doesnt look good)
* unschedule then reschedule the jobInstance
* Clear the compiled script : remove it from the local cache so it is recompiled form the storage when needed
