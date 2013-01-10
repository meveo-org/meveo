SET VERTINACLASSPATH="log4j.properties;lib/*"
java -Xms128m -Xmx768m -cp %VERTINACLASSPATH%;. -Dvertina.properties=vertina.properties org.meveo.vertina.Vertina