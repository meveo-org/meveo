#!/bin/bash -e

info() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] [INFO] $@"
}

DCK_IMAGE_REPO="manaty"
DCK_IMAGE_TAG="dev-latest"

SCM="scm:git:ssh://git@github.com:meveo-org/meveo.git"


info "-----------------------------------------"
info "          Building Meveo source          "
info "-----------------------------------------"

docker pull maven:3.6-jdk-11-slim

# Create a volume which will be used for Maven repository
docker volume create --name meveo-maven-repo

# Build meveo source using maven docker image
docker run -it --rm --name meveo-project \
  --volume meveo-maven-repo:/root/.m2 \
  --volume "$(pwd)":/usr/src/meveo \
  --workdir /usr/src/meveo \
  maven:3.6-jdk-11-slim \
  mvn clean package -Dscm.url=${SCM} -DskipTests


info "-----------------------------------------------"
info "          Building Meveo docker image          "
info "-----------------------------------------------"

# Copy dbchangelog and meveo.war into docker image folder
cp -r meveo-model/src/main/db_resources/changelog docker/configs/db_resources/
cp meveo-admin/web/target/meveo.war docker/

info "Building meveo docker image"
docker build -t ${DCK_IMAGE_REPO}/wildfly-meveo:${DCK_IMAGE_TAG} -f docker/Dockerfile ./docker

# Clean up
rm -rf docker/configs/db_resources/changelog
rm -rf docker/meveo.war

info "------------------------------------------------"
info "          Meveo docker - BUILD SUCCESS          "
info "------------------------------------------------"

info "Remove the useless image"
docker system prune -f
