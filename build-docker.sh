#!/bin/bash -e

info() {
  echo "[$(date '+%Y-%m-%d %H:%M:%S')] [INFO] $@"
}

DCK_IMAGE_REPO="manaty"
DCK_IMAGE_TAG="dev-latest"

info "------------------------------------------------"
info "          Building meveo docker image           "
info "------------------------------------------------"

# Enable docker buildkit,
# Increase the building log limit to 50MB. (Default limit is 1MB)
DOCKER_BUILDKIT=1 BUILDKIT_STEP_LOG_MAX_SIZE=50000000 \
  docker build -t ${DCK_IMAGE_REPO}/wildfly-meveo:${DCK_IMAGE_TAG} .

info "------------------------------------------------"
info "          Meveo docker - BUILD SUCCESS          "
info "------------------------------------------------"
