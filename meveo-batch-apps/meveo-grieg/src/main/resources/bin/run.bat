SET GRIEGCLASSPATH="log4j.properties;lib/*"
java -Xms128m -Xmx768m -cp grieg-meveo.jar;%GRIEGCLASSPATH%;. -Dgrieg.properties=grieg.properties org.meveo.grieg.Grieg