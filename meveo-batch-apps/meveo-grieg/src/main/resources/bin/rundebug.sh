#!/bin/bash

export GRIEGCLASSPATH=log4j.properties:lib/*
nohup java -Duser.country=FR -Duser.language=fr -Xms128m -Xmx768m -cp $GRIEGCLASSPATH:. -Dgrieg.properties=grieg.properties  -Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y  org.meveo.grieg.Grieg  > grieg.log &
echo $! > grieg.pid
