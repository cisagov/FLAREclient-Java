FROM docker.artifactory.apps.ecicd.dso.ncps.us-cert.gov/openjdk/openjdk-8-rhel8:1.3-2

# USE_DEV_CERTS - when set to true, it is used to
# load certificates for images created locally,
# otherwise no certs / keys will be bundled into the image
ARG USE_DEV_CERTS="false"

# Add application
ADD target/*SNAPSHOT.jar            /opt/app/app.jar
ADD src/main/resources/schemas/     /opt/app/schemas/
ADD src/main/resources/i18n/        /opt/app/i18n/
ADD src/main/resources/templates/   /opt/app/templates/
ADD src/main/resources/*            /opt/app/
ADD docker/*                        /opt/app/

USER root

RUN chmod 755 /opt/app/entrypoint.sh

# Remove dev certs if not running locally
RUN if [ "${USE_DEV_CERTS}" = "false" ] ; then rm /opt/app/wso2carbon.jks-unique_file_must_exist_for_icam; rm /opt/app/*.jks; fi
WORKDIR /opt/app/
RUN chmod g+w /opt/app/
RUN chown -R jboss:jboss /opt/app/*
USER jboss
ENTRYPOINT [ "./entrypoint.sh" ]

