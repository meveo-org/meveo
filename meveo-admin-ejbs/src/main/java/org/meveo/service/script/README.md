# Function

A function (or script)  is a piece source code that can be written in Java or Javascript and can be used to perform some business logic.

A function can be executed manually, from an [endpoint](https://github.com/meveo-org/meveo/tree/master/meveo-admin/ejbs/src/main/java/org/meveo/service/technicalservice/endpoint),
from a [notification]((https://github.com/meveo-org/meveo/tree/master/meveo-admin/ejbs/src/main/java/org/meveo/service/notification)) or from another function.

## Java function

This is a class that extends `org.meveo.service.script.Script`


## Calling a script from another script

You can load a script as a standard java class or use the [ScriptInstanceService](./ScriptInstanceService.java) like in this example

```
package org.meveo.example;

import java.util.Map;
import java.util.HashMap;
import org.meveo.service.script.Script;
import org.meveo.admin.exception.BusinessException;
import org.meveo.service.script.ScriptInstanceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class ExecuteScriptByCode extends Script {

    private static final Logger LOG = LoggerFactory.getLogger(ExecuteScriptByCode.class);
    
    /* Service used to exeute a script knowing its code */
    private final ScriptInstanceService scriptInstanceService = getCDIBean(ScriptInstanceService.class);

    @Override
    public void execute(Map<String, Object> parameters) throws BusinessException {
        try {
           	// Create a context containing input and output parameters of the script to call
            // we put in this map the input parameters 
            Map<String, Object> context = new HashMap<>();
          	context.put("inputParam1","value1");
          
            // we call the script escute method whose code is known
          	// note that this method do not call the init or finilaze method of the script
            scriptInstanceService.execute("MyScriptCode", context);
          
          	// during the execution the script can change the value of some input parameters
            // or add output parameters to the context
          	LOG.info("Script execution result:{}",context.get("outputParam1"));
            
        } catch (BusinessException ex) {
            LOG.error("cannot execute Script", ex);
        }
    }

}
```

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

## JavaScript scripting

If you select the "ES5" script type, you will be allowed to code the script using an ES5 / ES6 syntax, thanks to graal vm.

When executing such a script, you have many functions available : 

- require(libName) : to load a NPM dependency
- requireService(className) : to load a meveo service (CDI bean)
- requireFunction(code) : to load an existing script (no matter the type)
- log.info / debug / error : the standard slf4j logger

### How to define a npm library ?

For this, we use webjars and maven. For exemple, if we need lodash, use the following declaration of maven dependency : 

`org.webjars.npm:lodash:4.17.21`

### Script example

```require("lodash");
var result = _.partition([1, 2, 3, 4], n => n % 2);
for (let subResult of result) {
  log.info("Result !");
}

const cetService = requireService("org.meveo.service.custom.CustomEntityTemplateService");
const cetList = cetService.list(true);

for (let cet of cetList) {
  log.info(cet.getCode());
}

const testFunction = requireFunction("org.meveo.script.DefaultScript");
testFunction.execute(methodContext);
```

### Defining outputs

Currently, every output has to be defined manually. 

To write an output there is two ways : 
* either write in the methodContext map : `methodContext.put('outputname', outputvalue)` 
* or just declare a variable with the same name than the output : `var outputname = outputvalue`

