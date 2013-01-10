#!/bin/bash

export LANG=fr_FR.UTF-8
export VERTINACLASSPATH=log4j.properties:lib/*
nohup java -Duser.country=FR -Duser.language=fr -Xms128m -Xmx768m -cp $VERTINACLASSPATH:. -Dvertina.properties=vertina.properties org.meveo.vertina.Vertina  > vertina.log &
echo $! > vertina.pid