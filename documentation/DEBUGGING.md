## Debugging

### Realtime script debuggin using visual code
It is possible to debug scripts executed in meveo using jdb, for instance in visual code.

first you must launch wildfly in mode debug, for this edit your `docker-compose.yml` and add those lines
in the meveo service

```
        environment:
          #KEYCLOAK_URL: http://localhost:8081/auth   # For Linux localhost system
          KEYCLOAK_URL: http://host.docker.internal:8081/auth   # For Windows & MacOS localhost system
          WILDFLY_DEBUG_ENABLE: "true"
          WILDFLY_DEBUG_PORT: 8787
        ports:
          - 8080:8080
          - 8787:8787
          - 9990:9990
```

Then in `vscode`, open the project and [configure the file](https://code.visualstudio.com/docs/java/java-debugging) `launch.json`

```
{
    // Use IntelliSense to learn about possible attributes.
    // Hover to view descriptions of existing attributes.
    // For more information, visit: https://go.microsoft.com/fwlink/?linkid=830387
    "version": "0.2.0",
    "configurations": [
        {
            "type": "java",
            "name": "Launch Current File",
            "request": "attach",
            "hostName": "localhost",
            "port": 8787
        }
    ]
}
```

### Check meveo logs

We have several methods to see meveo log. 

1. From meveo GUI, click on Execution / Logs

2. See the meveo docker container's log

Run this command: 

    $ sudo docker logs -f meveo 

This command will see the full log of meveo. So if the container is running for a long time, this will see huge log. This command is appropriate when meveo doesn't output much log.

3. Enter the meveo docker container, and tail the log file

Enter into the meveo container

    $ sudo docker exec -it meveo bash

Inside the container, navigate to the log folder, and then tail the log file

    $ cd /opt/jboss/wildfly/standalone/log
    $ tail -f server.log


4. Extract the log file from the container to the host system.

Developer can extract the log file from the container. Then can see it on host system. It's because developer might would like to see on the host system.

This command will extract the log file and then will put it under /tmp folder on the host system.

    $ sudo docker cp meveo:/opt/jboss/wildfly/standalone/log/server.log /tmp/server.log


5. Create a volume mapping for log files.

Developer might want to keep the log files permenantly on the host system. In this case, we can create a volume mapping rule for that.

In docker-compose.yml file, add a volume rule under the service "meveo"

      volumes:
        ......
        - <meveo_log_path_on_host>:/opt/jboss/wildfly/standalone/log

<meveo_log_path_on_host> : this path shold be relaced by the location the developer want.

Grant the good permission to the log location on host system.

    $ sudo chmod 777 <meveo_log_path_on_host>

On windows, opendocker desktop dashboard and Share your drive

Finally, apply the change of docker-compose file.

    $ sudo docker-compose up -d
This command will recreate the meveo container. and developer can see the log file in the specified location on host system.
