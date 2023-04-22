echo Enter java keystore \(jks\) name
read keystore

echo Enter key store pass
read -s keypass

keytool -importkeystore \
  -srckeystore "$keystore" -srcstorepass "$keypass" \
  -destkeystore key.p12 -deststoretype PKCS12 -deststorepass "$keypass"

openssl pkcs12 -in key.p12 -nodes -nocerts -out key.key

