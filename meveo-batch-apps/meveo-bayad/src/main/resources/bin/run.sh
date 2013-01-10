#!/bin/bash

export LANG=fr_FR.UTF-8
export BAYADCLASSPATH=log4j.properties:lib/*
nohup java -Duser.country=FR -Duser.language=fr -Xms128m -Xmx768m -cp $BAYADCLASSPATH:. -Dbayad.properties=bayad.properties org.meveo.bayad.Bayad  > bayad.log &
echo $! > bayad.pid

