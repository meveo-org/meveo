# Module development

Every item (entity, function, job,..) you create on meveo are part of a module

A module is essentially a git repository that contains all the semantic of your items (serialized as json files) and 
some facets that represent them in some specific technology (java, js, python,...)

You create a module in meveo UI or by API then meveo serve as a git server from which you can clone the module locally and/or push it on its origin.

There are several ways to develop a module:
* Create and update the module on meveo UI (local or remote)
* modify code in local IDE, commit push and test on remote meveo
* modify code, test and debug in local IDE

## Remote dev

the first case is the simplest one in terms of  setup but the code editor we currently use is missing auto completion, and realtime validation.
Note that this is a planned enhancement

## Mix dev

local code writing and remote testing is easy too : 
- clone the module repo from the remote meveo (acting as a git server)
- open the project in the IDE, modify the code
- commit and push on meveo

If you need new CET, script,... Create them on the remote meveo then pull in the IDE

## Local dev
This one is needed for debugging the code in the IDE. To work with remote meveo we would need to implement the transfer of java file when saved in the IDE.

you should be able to it do like in Mix dev option except that you dont need to commit to see the change in script,
 you can just save the file locally then meveo will detect it and compile the new java file
also in the meveo dev image we use a special JVM that allow hot reload and debugging you can debug the scripts in the IDE

An alternative to use meveo dev image is to simply run doker image of keycloak and postgres then have wildfly deployed locally



