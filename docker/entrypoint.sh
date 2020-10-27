#!/usr/bin/env bash

echo "[ ]  Copying secrets files  ... "
cp ${SECRETS_DIR}* . 2>/dev/null
echo "[+]  Done copying secrets files  ... "

if [ "${TARGET_ENV}" = "dev" ] || [ "${TARGET_ENV}" = "test" ];
then
   export JAVA_TOOL_OPTIONS="-Dcx.log.level=DEBUG -Dcx.aj.logClassLoading -javaagent:/opt/cxiast-java-agent/cx-launcher.jar -DcxAppTag=FlareCloud_IM_Dashboard -DcxScanTag=1_0 -Xverify:none";
fi

APPLICATION_SETTING_FILEPATH=/opt/app/application.yml
echo "[ ] Adjusting ${APPLICATION_SETTING_FILEPATH}"
sed -i "s/FLARECLIENT_DB_USER/${FLARECLIENT_DB_USER}/g" $APPLICATION_SETTING_FILEPATH
sed -i "s/FLARECLIENT_DB_PASS/${FLARECLIENT_DB_PASS}/g" $APPLICATION_SETTING_FILEPATH
sed -i "s/FLARECLIENT_DB_SVC_HOST/${FLARECLIENT_DB_SVC_HOST}/g" $APPLICATION_SETTING_FILEPATH
sed -i "s/FLARECLIENT_DB_SVC_PORT/${FLARECLIENT_DB_SVC_PORT}/g" $APPLICATION_SETTING_FILEPATH
#cat $APPLICATION_SETTING_FILEPATH

echo "[ ]  Starting service  ... "
java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar /opt/app/app.jar
