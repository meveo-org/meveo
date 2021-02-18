#!/bin/bash -e
#publish the jars to maven repository
mvn deploy  -DskipTests --settings settings.xml