FROM docker.artifactory.apps.ecicd.dso.ncps.us-cert.gov/rhscl/mongodb-36-rhel7
ENV MONGODB_ADMIN_PASSWORD=QWASZX23wesdxc
ADD mongodb/scripts/init_replicaset.js init_replicaset.js
