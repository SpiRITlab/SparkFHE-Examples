#!/usr/bin/env bash

ProjectRoot=..
cd $ProjectRoot

# generate example key pairs
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.batching.KeyGenExample -Dexec.args="local SEAL BFV"

# generate example ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.batching.EncDecExample -Dexec.args="local SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt"

# run basic FHE arithmetic operation over encrypted data
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.batching.BasicOPsExample -Dexec.args="local SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/records/$(ls gen/records | grep ptxt_long_0) gen/records/$(ls gen/records | grep ptxt_long_1)"

# run FHE dot product over two encrypted vectors
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.batching.DotProductExample -Dexec.args="local SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/records/$(ls gen/records | grep vec_a) gen/records/$(ls gen/records | grep vec_b)"

# run FHE total sum over encrypted vector elements
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.batching.TotalSumExample -Dexec.args="local SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/records/$(ls gen/records | grep vec)"