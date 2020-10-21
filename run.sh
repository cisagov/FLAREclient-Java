#!/usr/bin/env bash

docker rm -f flareclient
docker run \
    --network ais20 \
    -p 8081:8081 \
    --name flareclient \
    flareclient
