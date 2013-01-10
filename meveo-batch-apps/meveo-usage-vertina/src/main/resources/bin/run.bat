SET VERTINACLASSPATH="log4j.properties;lib/*"
java -Xms128m -Xmx768m -cp usage-rating.jar;%VERTINACLASSPATH%;. -Dvertina.properties=vertina.properties org.meveo.vertina.Vertina 