#!/bin/bash

export BAYADCLASSPATH=log4j.properties:lib/*
nohup java -Duser.country=FR -Duser.language=fr -Xms128m -Xmx768m -cp $BAYADCLASSPATH:. -Dbayad.properties=bayad.properties  -Xdebug -Xrunjdwp:transport=dt_socket,address=8998,server=y  org.meveo.bayad.Bayad  > bayad.log &
echo $! > bayad.pid
