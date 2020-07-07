#!/bin/bash -e

mvn -version

SCM="scm:git:ssh://git@github.com:meveo-org/meveo.git"

DCK_IMAGE_REPO="manaty"
DCK_IMAGE_TAG="local"


POSTGRES_ADDR="localhost"
POSTGRES_DB="meveo"
POSTGRES_USER="meveo"
POSTGRES_PASSWORD="meveo"
POSTGRES_TMP_CONTAINER_NAME="postgres_tmp_meveo"

# if container already exist, remove it
postgres_container=$(docker ps -aqf "name=${POSTGRES_TMP_CONTAINER_NAME}");
if [ ! -z "${postgres_container}" ]; then
  echo "postgres container is already running. it will be removed."
  docker rm -f ${postgres_container};
fi

echo "Start the postgres container"
docker run -d -p 5432:5432 \
  --name ${POSTGRES_TMP_CONTAINER_NAME} \
  -e POSTGRES_DB=${POSTGRES_DB} \
  -e POSTGRES_USER=${POSTGRES_USER} \
  -e POSTGRES_PASSWORD=${POSTGRES_PASSWORD} \
  postgres:9.5.21

# wait with timeout 30s until postgres is up
timeout=30
counter=0
until docker exec -i ${POSTGRES_TMP_CONTAINER_NAME} /usr/bin/pg_isready -h localhost -p 5432
do
  if [ $counter -gt $timeout ]; then
    echo "ERROR: timeout occurred after waiting $timeout seconds for postgres"
    exit 1
  else
    echo "Waiting for postgres ..."
    counter=$((counter+1))
    sleep 1
  fi
done
echo "postgres is up"

echo "Creating the database schema"
cd meveo-model
mvn liquibase:dropAll liquibase:update \
  -Ddb.url=jdbc:postgresql://${POSTGRES_ADDR}/${POSTGRES_DB} \
  -Ddb.username=${POSTGRES_USER} \
  -Ddb.password=${POSTGRES_PASSWORD} \
  -Prebuild

echo "Packaging meveo.war file"
cd ..
mvn clean package \
  -Dscm.url=${SCM} \
  -DskipTests

echo "Dump the database schema"
docker exec -i ${POSTGRES_TMP_CONTAINER_NAME} pg_dump --no-owner -U ${POSTGRES_USER} ${POSTGRES_DB} > meveo.sql

echo "Stop the postgres container"
postgres_container=$(docker ps -aqf "name=${POSTGRES_TMP_CONTAINER_NAME}")
if [ ! -z "${postgres_container}" ]; then
  docker rm -f ${postgres_container}
fi

# remove the unused database volume of postgres
docker volume prune -f

# copy meveo.sql and meveo.war files into the docker image folder for meveo
mv meveo.sql docker/
cp meveo-admin/web/target/meveo.war docker/

echo "Build the postgres docker image for meveo environment"
docker build -t ${DCK_IMAGE_REPO}/postgres-meveo:${DCK_IMAGE_TAG} -f docker/Dockerfile.postgres ./docker

echo "Build the meveo docker image"
docker build -t ${DCK_IMAGE_REPO}/wildfly-meveo:${DCK_IMAGE_TAG} -f docker/Dockerfile ./docker

rm -rf docker/meveo.sql
rm -rf docker/meveo.war

if [[ $(docker images -aq --filter dangling=true | wc -c) -ne 0 ]]; then
  echo "Remove the unused image"
  docker rmi $(docker images -aq --filter dangling=true)
fi
