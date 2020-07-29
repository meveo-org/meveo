cd /home/SERVER_NAME/
git reset --hard
docker-compose pull
docker-compose -f monitoring/docker-compose.yml pull
chmod +x *.sh
./install.sh
