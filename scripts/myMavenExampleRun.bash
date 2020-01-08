#!/usr/bin/env bash

ProjectRoot=..
cd $ProjectRoot

# test connection between Java and C++ using basic example
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.BasicExample -Dexec.args="local"

# generate example key pairs
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.KeyGenExample -Dexec.args="local"

# generate example ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.EncDecExample -Dexec.args="local gen/keys/my_public_key.txt  gen/keys/my_secret_key.txt"

# run basic FHE arithmetic operation over encrypted data
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.BasicOPsExample -Dexec.args="local  gen/keys/my_public_key.txt  gen/keys/my_secret_key.txt  gen/records/$(ls gen/records | grep ptxt_long_0)  gen/records/$(ls gen/records | grep ptxt_long_1)"

# run FHE dot product over two encrypted vectors
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.DotProductExample -Dexec.args="local  gen/keys/my_public_key.txt  gen/keys/my_secret_key.txt  gen/records/$(ls gen/records | grep vec_a)  gen/records/$(ls gen/records | grep vec_b)"



# SEAL tests
# generate example key pairs
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic_seal.KeyGenExample -Dexec.args="local"

# generate example ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic_seal.EncDecExample -Dexec.args="local gen/keys/my_public_key.txt  gen/keys/my_secret_key.txt"

# run basic FHE arithmetic operation over encrypted data
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic_seal.BasicOPsExample -Dexec.args="local  gen/keys/my_public_key.txt  gen/keys/my_secret_key.txt  gen/records/$(ls gen/records | grep ptxt_long_0)  gen/records/$(ls gen/records | grep ptxt_long_1)"

# run FHE dot product over two encrypted vectors
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic_seal.DotProductExample -Dexec.args="local  gen/keys/my_public_key.txt  gen/keys/my_secret_key.txt  gen/records/$(ls gen/records | grep vec_a)  gen/records/$(ls gen/records | grep vec_b)"

# run FHE total sum over encrypted vector elements
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic_seal.TotalSumExample -Dexec.args="local  gen/keys/my_public_key.txt  gen/keys/my_secret_key.txt  gen/records/$(ls gen/records | grep vec)"
