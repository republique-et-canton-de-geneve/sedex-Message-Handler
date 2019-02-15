$Id: readme.txt 333 2015-05-07 09:27:10Z sasha $

-- examples how to create a key pair with RSA
keytool -genkeypair -keyalg RSA -alias anikiforov -storetype pkcs12 -keystore anikiforov.p12 -storepass 123456 \
  -validity 712 -keypass 123456 \
  -dname "CN=Alexander Nikiforov, OU=SE, O=Glue Software Engineering AG, L=Bern, ST=Bern, C=CH"

-- export the public key as certificate
keytool -export -alias anikiforov -keystore anikiforov.p12 -storetype pkcs12 -storepass 123456 -file anikiforov.cer

-- print the certificate content
keytool -printcert -file anikiforov.cer

-- print the content of the keystore
keytool -list -v -keystore anikiforov.p12 -storetype pkcs12 -storepass 123456 -keypass 123456

