#!/bin/bash

export LANG=fr_FR.UTF-8
export OUDAYACLASSPATH=log4j.properties:lib/*
nohup java -Duser.country=FR -Duser.language=fr -Xms128m -Xmx768m -cp $OUDAYACLASSPATH:. -Doudaya.properties=oudaya.properties org.meveo.oudaya.Oudaya  > oudaya.log &
echo $! > oudaya.pid
