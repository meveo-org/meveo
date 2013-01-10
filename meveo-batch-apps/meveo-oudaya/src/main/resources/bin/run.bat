SET OUDAYACLASSPATH="log4j.properties;lib/*"
java -Xms128m -Xmx768m -cp %OUDAYACLASSPATH%;. -Doudaya.properties=oudaya.properties org.meveo.oudaya.Oudaya