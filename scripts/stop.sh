#!/usr/bin/env bash

ABS_PATH=$(readlink -f "$0")
ABS_DIR=$(dirname "$ABS_PATH")
source "${ABS_DIR}"/profile.sh

IDLE_PROFILE=$(find_idle_profile)

CONTAINER_ID=$(docker container ls -f "name=${IDLE_PROFILE}" -q -all)

echo "> Current container id: ${CONTAINER_ID}"
echo "> Idle profile: ${IDLE_PROFILE}"

if [ -z "${CONTAINER_ID}" ]
then
  echo "> No running container."
else
  echo "> docker stop ${IDLE_PROFILE}"
  sudo docker stop "${IDLE_PROFILE}"
  echo "> docker rm ${IDLE_PROFILE}"
  sudo docker rm "${IDLE_PROFILE}"
  sleep 5
fi
