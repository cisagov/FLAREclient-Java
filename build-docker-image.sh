#!/bin/sh

mvn clean package -DskipTests -Dmaven.wagon.http.ssl.insecure=true | tee .out
if [ -z "$(grep 'BUILD SUCCESS' .out )" ]; then echo "Error with maven build"; exit; fi

docker build --build-arg USE_DEV_CERTS=true -t flareclient . | tee .out
if [ -z "$(grep 'Successfully built' .out )" ]; then echo "Error with docker build"; exit; fi
