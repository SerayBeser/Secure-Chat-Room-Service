#!/bin/bash

# Seray Beser
# command line utility keytool (command line utility) to generate and manage keys and certificates

echo -e "\n[INFO]: Default passwords are 123456.\n[INFO]: All certificates are valid for 180 days. "

echo -e "\n[PROCESS] --------- Creating Self-Signed CA Certificate ---------"
keytool -genkeypair -keyalg RSA -keysize 2048 -validity 180 -alias ca -dname "CN=CA, O=TOBB ETU, C=TR" -keystore KeyStoreCA  -storepass 123456  -keypass 123456
keytool -exportcert -rfc -alias ca -keystore KeyStoreCA -storepass 123456 > ca.cer
echo -e "\n[DONE]: Created Self-Signed CA Certificate."

echo -e "\n[PROCESS] --------- Creating Server's Certificate  ---------"
keytool -genkeypair -keyalg RSA -keysize 2048 -validity 180 -alias server -dname "CN=SERVER, O=TOBB ETU, C=TR" -keystore KeyStoreServer -storepass 123456  -keypass 123456
keytool -certreq -alias server -storepass 123456 -keystore KeyStoreServer | keytool  -gencert -alias ca -rfc -keystore KeyStoreCA -storepass 123456 > server.cer
cat ca.cer | keytool -importcert -alias ca -noprompt -keystore KeyStoreServer -storepass 123456
cat ca.cer server.cer | keytool -importcert -alias server -keystore KeyStoreServer -storepass 123456
echo -e "[DONE]: Created  Server's Certificate. "

echo -e "\n[PROCESS] --------- Creating Clients's Certificate ---------"
keytool -genkeypair -keyalg RSA -keysize 2048 -validity 180 -alias client -dname "CN=CLIENT, O=TOBB ETU, C=TR" -keystore KeyStoreClient -storepass 123456  -keypass 123456
keytool -certreq -alias client -keystore KeyStoreClient -storepass 123456 | keytool -gencert -alias ca -rfc -keystore KeyStoreCA -storepass 123456 > client.cer
cat ca.cer | keytool -importcert -alias ca -noprompt -keystore KeyStoreClient -storepass 123456
cat ca.cer client.cer | keytool -importcert -alias client -keystore KeyStoreClient -storepass 123456
echo -e "\n[DONE]: Created Clients's Certificate. "

echo -e "\n[FINISH]: All certificates and keystores are created and saved. "
