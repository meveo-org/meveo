#!/bin/bash

export VERTINACLASSPATH=log4j.properties:lib/*
nohup java -Duser.country=FR -Duser.language=fr -Xms128m -Xmx768m -cp $VERTINACLASSPATH:. -Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y -Dvertina.properties=vertina.properties org.meveo.vertina.Vertina  > vertina.log &
echo $! > vertina.pid
