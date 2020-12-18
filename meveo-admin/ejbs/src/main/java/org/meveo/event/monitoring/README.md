# Cluster Events

When running in a wildfly cluster, a JMS topic `CLUSTEREVENTTOPIC` is used to publish an event when

* when a role is created/updated/removed
* when jobInstance is created/updated/removed/enabled/disabled or if its timer is updated
* when a Function(Script) is created/updated/removed/enabled/disabled
