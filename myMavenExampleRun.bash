#!/usr/bin/env bash

# generate example key pairs
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.KeyGenExample -Dexec.args="local"

# generate example ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.EncDecExample -Dexec.args="local"

# run basic FHE arithmetic operation over encrypted data
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.BasicOPsExample -Dexec.args="local  gen/keys/my_public_key.txt  gen/keys/my_secret_key.txt  gen/records/$(ls gen/records | grep ptxt_long_0)  gen/records/$(ls gen/records | grep ptxt_long_1)"

# run FHE dot product over two encrypted vectors
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.DotProductExample -Dexec.args="local  gen/keys/my_public_key.txt  gen/keys/my_secret_key.txt  gen/records/$(ls gen/records | grep vec_a)  gen/records/$(ls gen/records | grep vec_b)"