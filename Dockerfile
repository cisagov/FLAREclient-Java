FROM docker.artifactory.apps.ecicd.dso.ncps.us-cert.gov/openjdk/openjdk-11-rhel7:latest

# USE_DEV_CERTS - when set to true, it is used to
# load certificates for images created locally,
# otherwise no certs / keys will be bundled into the image
ARG USE_DEV_CERTS="false"

# This gets set to false locally with the AIS-run script
ARG IS_DSO="true"

# Add application
ADD target/*SNAPSHOT.jar            /opt/app/app.jar
ADD src/main/resources/schemas/     /opt/app/schemas/
ADD src/main/resources/i18n/        /opt/app/i18n/
ADD src/main/resources/templates/   /opt/app/templates/
ADD src/main/resources/*            /opt/app/
ADD docker/*                        /opt/app/

USER root

# Removing vulnerable unneeded files
RUN yum -y remove xorg-x11-fonts-Type1 libXinerama libXext libSM libXft libxshmfence libXrender libXxf86vm libfontenc libXrandr libXi libXtst libICE libxcb xorg-x11-font-utils libX11-common libX11
RUN rm -Rf /usr/share/java/prometheus-jmx-exporter/jmx_prometheus_javaagent.jar \
           /opt/app/client-user-rsa.key /opt/app/demoUser.key

# Remove dev certs if not running locally
#RUN if [ "${USE_DEV_CERTS}" = "false" ] ; then rm /opt/app/wso2carbon.jks-unique_file_must_exist_for_icam; rm /opt/app/*.jks; fi

# Upgrade 'dbus' and 'freetype' library
RUN if [ "${IS_DSO}" = "true" ] ; then yum install -y --setopt=tsflags=nodocs \
    --disablerepo=* \
    --enablerepo=rhel-server-rhscl-7-rpms \
    --enablerepo=rhel-7-server-extras-rpms \
    --enablerepo=rhel-7-server-optional-rpms \
    --enablerepo=rhel-7-server-rpms \
    freetype; yum clean all; rm -rf /var/cache/yum; fi

WORKDIR /opt/app/
RUN chmod g+w /opt/app/
RUN chown jboss:jboss /opt/app/*
USER jboss
ENTRYPOINT [ "./entrypoint.sh" ]

