#!/usr/bin/env bash

ABS_PATH=$(readlink -f "$0")
ABS_DIR=$(dirname "$ABS_PATH")
source "${ABS_DIR}"/profile.sh
source "${ABS_DIR}"/switch.sh

IDLE_PORT=$(find_idle_port)

echo "> Health Check Start!"
echo "> IDLE_PORT: $IDLE_PORT"
echo "> curl -s http://localhost:$IDLE_PORT/ping/"
sleep 10

for RETRY_COUNT in {1..10}
do
  RESPONSE=$(curl -s http://localhost:"${IDLE_PORT}"/ping/)
  UP_COUNT=$(echo "${RESPONSE}" | grep -c 'pong')

  if [ "${UP_COUNT}" -ge 1 ]
  then
      echo "> Health check success"
      switch_proxy
      break
  else
      echo "> Health check response: ${RESPONSE}"
  fi

  if [ "${RETRY_COUNT}" -eq 10 ]
  then
    echo "> Health check failed. Quit."
    exit 1
  fi

  echo "> Health check connection failed. Retrying..."
  sleep 10
done
