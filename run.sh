#!/usr/bin/env bash

docker rm -f flareclient
docker run \
    --network ais20 \
    -p 8082:8082 \
    --name flareclient \
    flareclient
