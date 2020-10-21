#!/usr/bin/env bash

docker rm -f flareclient
docker run \
    --network ais2 \
    -p 8082:8082 \
    --name flareclient \
    flareclient
