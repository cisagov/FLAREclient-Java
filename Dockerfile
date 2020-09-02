FROM openjdk:8-jre-alpine

ENV SPRING_OUTPUT_ANSI_ENABLED=ALWAYS \
    JHIPSTER_SLEEP=0 \
    JAVA_OPTS=""

# Add a user to run our application so that it doesn't need to run as root
RUN adduser -D -s /bin/sh flare
WORKDIR /home/flare

ADD docker/entrypoint.sh entrypoint.sh
RUN chmod 755 entrypoint.sh && chown flare:flare entrypoint.sh
USER flare

ADD target/*.jar app.jar
ADD src/main/resources/schemas/ schemas/
ADD src/main/resources/i18n/ i18n/
ADD src/main/resources/templates/ templates/
ADD src/main/resources/* /home/flare/
ADD docker/application.yml application.yml

ENTRYPOINT ["./entrypoint.sh"]

EXPOSE 8081

