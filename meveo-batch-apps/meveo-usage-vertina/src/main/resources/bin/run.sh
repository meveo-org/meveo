export VERTINACLASSPATH="log4j.properties:lib/*"
nohup java -Xms128m -Xmx768m -cp usage-rating.jar:$VERTINACLASSPATH:. -Dvertina.properties=vertina.properties org.meveo.vertina.Vertina > usage-rating.log &
echo $! > usage-rating.pid
