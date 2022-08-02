#!/usr/bin/env bash

ABS_PATH=$(readlink -f "$0")
ABS_DIR=$(dirname "$ABS_PATH")
source "${ABS_DIR}"/profile.sh

function switch_proxy() {
    IDLE_PORT=$(find_idle_port)

    echo "> Idle port: $IDLE_PORT"
    echo "> Change port"
    echo "set \$service_url http://172.17.0.1:${IDLE_PORT};" | sudo tee /etc/nginx/conf.d/service-url.inc

    sudo docker exec -d nginx nginx -s reload
    echo "> docker exec -d nginx nginx -s reload"
}
