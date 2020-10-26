#!/usr/bin/env bash

docker rm -f flareclient
docker run \
    --network ais20 \
    -e FLARECLIENT_DB_USER="admin" \
    -e FLARECLIENT_DB_PASS="QWASZX23wesdxc" \
    -p 8083:8083 \
    --name flareclient \
    flareclient
