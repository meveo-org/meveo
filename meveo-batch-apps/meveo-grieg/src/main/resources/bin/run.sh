#!/bin/bash

export LANG=fr_FR.UTF-8
export GRIEGCLASSPATH=log4j.properties:lib/*
nohup java -Duser.country=FR -Duser.language=fr -Xms128m -Xmx768m -cp $GRIEGCLASSPATH:. -Dgrieg.properties=grieg.properties org.meveo.grieg.Grieg  > grieg.log &
echo $! > grieg.pid
