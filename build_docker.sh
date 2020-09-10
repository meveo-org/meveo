#!/bin/bash -e

mvn -version

SCM="scm:git:ssh://git@github.com:meveo-org/meveo.git"

DCK_IMAGE_REPO="manaty"
DCK_IMAGE_TAG="dev-latest"

# build meveo project
echo "Packaging meveo.war file"
mvn clean package \
  -Dscm.url=${SCM} \
  -DskipTests

# remove the unused database volume of postgres
docker volume prune -f

# copy dbchangelog and meveo.war files into meveo docker image
cp -r meveo-model/src/main/db_resources/changelog docker/configs/db_resources/
cp meveo-admin/web/target/meveo.war docker/

echo "Build the neo4j docker image for meveo environment"
docker build -t ${DCK_IMAGE_REPO}/neo4j-meveo:${DCK_IMAGE_TAG} -f docker/Dockerfile.neo4j ./docker

echo "Build the meveo docker image"
docker build -t ${DCK_IMAGE_REPO}/wildfly-meveo:${DCK_IMAGE_TAG} -f docker/Dockerfile ./docker

rm -rf docker/configs/db_resources/changelog
rm -rf docker/meveo.war

if [[ $(docker images -aq --filter dangling=true | wc -c) -ne 0 ]]; then
  echo "Remove the unused image"
  docker rmi $(docker images -aq --filter dangling=true)
fi
