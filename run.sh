#!/usr/bin/env bash

docker rm -f flareclient
docker run \
    --network ais20 \
    -e FLARECLIENT_DB_USER="admin" \
    -e FLARECLIENT_DB_PASS="QWASZX23wesdxc" \
    -e FLARECLIENT_DB_SVC_HOST="flareclient-db" \
    -e FLARECLIENT_DB_SVC_PORT="27017" \
    -p 8083:8083 \
    --name flareclient \
    flareclient
