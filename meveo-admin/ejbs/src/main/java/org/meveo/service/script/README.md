# Function

A function (or script)  is a piece source code that can be written in Java or Javascript and can be used to perform some business logic.

A function can be executed manually, from an [endpoint](https://github.com/meveo-org/meveo/tree/master/meveo-admin/ejbs/src/main/java/org/meveo/service/technicalservice/endpoint),
from a [notification]((https://github.com/meveo-org/meveo/tree/master/meveo-admin/ejbs/src/main/java/org/meveo/service/notification)) or from another function.

# Java function

This is a class that extends `org.meveo.service.script.Script`

## Logging
in order to be able to log commands, use sl4J 

```

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MyScript extends Script {
    private static final Logger log = LoggerFactory.getLogger(MyScript.class);
    ...
}
```
