#!/usr/bin/env bash

docker rm -f flareclient
docker run \
    --network ais20 \
    -e FLARECLIENT_DB_USER="admin" \
    -e FLARECLIENT_DB_PASS="QWASZX23wesdxc" \
    -e CERT_ALIAS="localhost" \
    -e CERT_KEYSTORE_PASS="12qwaszx@#WESDXC" \
    -e CERT_TRUSTSTORE_PASS="12qwaszx@#WESDXC" \
    -e MAX_REQUEST_SIZE="150M" \
    -e TIMEOUT_API="680000" \
    -e TIMEOUT_DEFAULT="60000" \
    -e TIMEOUT_TAXII21_DOWNLOAD="660000" \
    -e TIMEOUT_TAXII21_MANIFEST="660000" \
    -e TIMEOUT_TAXII21_UPLOAD="660000" \
    -p 8083:8083 \
    --name flareclient \
    flareclient