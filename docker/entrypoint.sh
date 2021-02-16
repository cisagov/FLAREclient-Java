#!/usr/bin/env bash

urlencodepipe() {
  local LANG=C; local c; while IFS= read -r c; do
    case $c in [a-zA-Z0-9.~_-]) printf "$c"; continue ;; esac
    printf "$c" | od -An -tx1 | tr ' ' % | tr -d '\n'
  done <<EOF
$(fold -w1)
EOF
  echo
}

urlencode() {
    printf "$1" | urlencodepipe ;
}
java ${JAVA_OPTS} -jar /opt/app/app.jar

# Copy keystores stored in openshift secrets here
echo "[ ]  Copying secrets files  ... "
if [ -d "${SECRETS_DIR}" ]
then
    echo "Directory ${SECRETS_DIR} exists."
    cp ${SECRETS_DIR}* /opt/app/ 2>/dev/null
    echo "[+]  Done copying secrets files  ... "
else
    echo "Warning: Directory ${SECRETS_DIR} does not exists. This is ok if you are running this locally."
fi