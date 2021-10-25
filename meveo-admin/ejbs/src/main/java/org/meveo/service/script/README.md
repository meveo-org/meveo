# Function

A function (or script)  is a piece source code that can be written in Java or Javascript and can be used to perform some business logic.

A function can be executed manually, from an [endpoint](https://github.com/meveo-org/meveo/tree/master/meveo-admin/ejbs/src/main/java/org/meveo/service/technicalservice/endpoint),
from a [notification]((https://github.com/meveo-org/meveo/tree/master/meveo-admin/ejbs/src/main/java/org/meveo/service/notification)) or from another function.

## Java function

This is a class that extends `org.meveo.service.script.Script`

## Logging

in order to be able to log commands, use sl4J 

```java

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyScript extends Script {
    private static final Logger log = LoggerFactory.getLogger(MyScript.class);
    ...
}
```

## Transaction management

When being executed by an endpoint, a job or a notification, it is possible to choose the transaction context for the script execution. This is represented by the attribute `CustomScriptInstance#transactionType` which can have the following values: 

- MANUAL: the variable `userTx : UserTransaction` is injected in the script execution context
- NEW: the script execution will be wrapped in a new transaction
- SAME (default): the script will be executed in the caller's transaction
- NONE: the script will not be executed in a transaction

## Debugging

you can easily debug your scripts from your IDE [using jdb](../../../../../../../../../documentation/DEBUGGING.md).
