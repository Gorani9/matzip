#!/usr/bin/env bash

function find_idle_profile()
{
    CURRENT_URL=$(cat /etc/nginx/conf.d/service-url.inc | grep -c 8081);

    if [ "${CURRENT_URL}" -eq 1 ]
    then
        CURRENT_PROFILE=prod1
    else
        CURRENT_PROFILE=prod2
    fi

    if [ "${CURRENT_PROFILE}" == prod1 ]
    then
      IDLE_PROFILE=prod2
    else
      IDLE_PROFILE=prod1
    fi

    echo "${IDLE_PROFILE}"
}

function find_idle_port()
{
    IDLE_PROFILE=$(find_idle_profile)

    if [ "${IDLE_PROFILE}" == prod1 ]
    then
      echo "8081"
    else
      echo "8082"
    fi
}
