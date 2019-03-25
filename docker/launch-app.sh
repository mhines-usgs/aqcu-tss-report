#!/bin/sh
set -e

if [ -n "${AQUARIUS_SERVICE_PASSWORD_PATH}" ]; then
  aquariusServicePassword=$(cat ${AQUARIUS_SERVICE_PASSWORD_PATH})
fi

java $JAVA_OPTIONS \
  -Djava.security.egd=file:/dev/./urandom \
  -Djava.security.properties="${HOME}/java.security.properties" \
  -Djavax.net.ssl.trustStore="${JAVA_TRUSTSTORE}" \
  -Djavax.net.ssl.trustStorePassword="${JAVA_TRUSTSTORE_PASS}" \
  -DaquariusServicePassword=$aquariusServicePassword \
  -jar app.jar \
  "$@"

exec env "$@"
