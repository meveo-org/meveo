#!/bin/bash
if [ -f .env ]; then
  docker-compose up -d
  rm .env
else 
 echo ".env file not found"
 exit 1
fi
