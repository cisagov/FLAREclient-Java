FROM docker.artifactory.apps.ecicd.dso.ncps.us-cert.gov/openjdk/openjdk-11-rhel7:1.1-6.1594893482

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
# Remove vulnerable files
RUN rm /opt/jboss/container/prometheus/jmx_prometheus_javaagent*

RUN chmod 755 /opt/app/entrypoint.sh

RUN yum -y remove xorg-x11-fonts-Type1 libXinerama libXext libSM libXft libxshmfence libXrender libXxf86vm libfontenc libXrandr libXi libXtst libICE libxcb xorg-x11-font-utils libX11-common libX11

# Upgrade 'dbus' library
RUN if [ "${IS_DSO}" = "true" ] ; then yum install -y --setopt=tsflags=nodocs \
    --disablerepo=* \
    --enablerepo=rhel-server-rhscl-7-rpms \
    --enablerepo=rhel-7-server-extras-rpms \
    --enablerepo=rhel-7-server-optional-rpms \
    --enablerepo=rhel-7-server-rpms \
    dbus-1.10.24-15.el7 dbus-libs-1.10.24-15.el7; fi

# Remove dev certs if not running locally
RUN if [ "${USE_DEV_CERTS}" = "false" ] ; then rm /opt/app/wso2carbon.jks-unique_file_must_exist_for_icam; rm /opt/app/*.jks; fi
WORKDIR /opt/app/
RUN chmod g+w /opt/app/

RUN curl -k -ucloudbees:AP6odshz4U5o6aD4pViKq9vsvReUHVPDSuamfb -o /opt/cxiast-java-agent.zip 'https://artifactory.apps.ecicd.dso.ncps.us-cert.gov/artifactory/generic-local/cxiast-java-agent-upgrade.zip'
RUN mkdir -p /opt/cxiast-java-agent/
RUN unzip /opt/cxiast-java-agent.zip -d /opt/cxiast-java-agent/
RUN chmod +x /opt/cxiast-java-agent/CxIAST.sh
RUN rm -rf /opt/cxiast-java-agent/iast_cache

RUN chgrp -R 0 /opt/cxiast-java-agent && \
    chmod -R g=u /opt/cxiast-java-agent

RUN chown -R jboss:jboss /opt/app/*
USER jboss
ENTRYPOINT [ "./entrypoint.sh" ]

