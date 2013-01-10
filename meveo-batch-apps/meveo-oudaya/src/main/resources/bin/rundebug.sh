#!/bin/bash

export OUDAYACLASSPATH=log4j.properties:lib/*
nohup java -Duser.country=FR -Duser.language=fr -Xms128m -Xmx768m -cp $OUDAYACLASSPATH:. -Doudaya.properties=oudaya.properties -Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y org.meveo.oudaya.Oudaya  > oudaya.log &
echo $! > oudaya.pid
