SET BAYADCLASSPATH="log4j.properties;lib/*"
java -Xms128m -Xmx768m -cp %BAYADCLASSPATH%;. -Dbayad.properties=bayad.properties org.meveo.bayad.Bayad