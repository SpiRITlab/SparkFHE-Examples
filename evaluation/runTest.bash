#!/usr/bin/env bash

ProjectRoot=..
cd $ProjectRoot

## test connection between Java and C++ using basic example
#./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.BasicExample -Dexec.args="local"


### Generating keys and ctxts for examples ###

function set_up_SEAL_BFV(){
# nonbatching
# generate example key pairs
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.KeyGenExample -Dexec.args="local SEAL BFV"

# generate example ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.EncDecExample -Dexec.args="local SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt $1"

#batching
# generate example key pairs
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.KeyGenExample -Dexec.args="local SEAL BFV"

# generate example ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.EncDecExample -Dexec.args="local SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt $1 $2"
}

function set_up_SEAL_CKKS(){
# generate example key pairs
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.KeyGenExample -Dexec.args="local SEAL CKKS"

# generate example ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.EncDecExample -Dexec.args="local SEAL CKKS gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt $1 $2"
}

function set_up_HELIB_BGV(){
## nonbatching ##
# generate example key pairs
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.KeyGenExample -Dexec.args="local HELIB BGV"

# generate example ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.EncDecExample -Dexec.args="local HELIB BGV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt $1"

## batching ##
# generate example key pairs
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.KeyGenExample -Dexec.args="local HELIB BGV"

# generate example ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.EncDecExample -Dexec.args="local HELIB BGV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt $1 $2"
}


function set_up_HELIB_CKKS(){
# generate example key pairs
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.KeyGenExample -Dexec.args="local HELIB CKKS"

# generate example ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.EncDecExample -Dexec.args="local HELIB CKKS gen/keys/my_public_key.txt gen/keys/my_secret_key.txt $1 $2"
}


### Running HE examples (algorithms) ###

function run_SEAL_BFV() {

### nonbatching ##

# run basic FHE arithmetic operation over encrypted data
#./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.BasicOPsExample -Dexec.args="local $3 SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt"

# run FHE dot product over two encrypted vectors
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.DotProductExample -Dexec.args="local $3 SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt"

# run FHE total sum over encrypted vector elements
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.TotalSumExample -Dexec.args="local $3 SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt"


## batching ##

## run basic FHE arithmetic operation over encrypted data
#./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.BasicOPsExample -Dexec.args="local $3 SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt $1 $2"
#
## run FHE dot product over two encrypted vectors
#./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.DotProductExample -Dexec.args="local $3 SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt"
#
## run FHE total sum over encrypted vector elements
#./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.TotalSumExample -Dexec.args="local $3 SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt"

}

function run_SEAL_CKKS() {
# run basic FHE arithmetic operation over encrypted data
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.BasicOPsExample -Dexec.args="local $3 SEAL CKKS gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt $1 $2"

# run FHE dot product over two encrypted vectors
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.DotProductExample -Dexec.args="local $3 SEAL CKKS gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt"

# run FHE total sum over encrypted vector elements
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.TotalSumExample -Dexec.args="local $3 SEAL CKKS gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt"
}

function run_HELIB_BGV() {

## nonbatching ##

# run basic FHE arithmetic operation over encrypted data
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.BasicOPsExample -Dexec.args="local $3 HELIB BGV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt"

# run FHE dot product over two encrypted vectors
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.DotProductExample -Dexec.args="local $3 HELIB BGV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt"

# run FHE total sum over encrypted vector elements
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.TotalSumExample -Dexec.args="local $3 HELIB BGV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt"


## batching ##

# run basic FHE arithmetic operation over encrypted data
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.BasicOPsExample -Dexec.args="local $3 HELIB BGV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt $1 $2"

# run FHE dot product over two encrypted vectors
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.DotProductExample -Dexec.args="local $3 HELIB BGV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt"

# run FHE total sum over encrypted vector elements
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.TotalSumExample -Dexec.args="local $3 HELIB BGV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt"

}


function run_HELIB_CKKS() {
# run basic FHE arithmetic operation over encrypted data
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.BasicOPsExample -Dexec.args="local $3 HELIB CKKS gen/keys/my_public_key.txt gen/keys/my_secret_key.txt $1 $2"

# run FHE dot product over two encrypted vectors
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.DotProductExample -Dexec.args="local $3 HELIB CKKS gen/keys/my_public_key.txt gen/keys/my_secret_key.txt"

# run FHE total sum over encrypted vector elements
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.TotalSumExample -Dexec.args="local $3 HELIB CKKS gen/keys/my_public_key.txt gen/keys/my_secret_key.txt"

}


#### deleting all keys and ctxts ###
#
#function clean_up() {
## delete all saved ctxt and key files for this run
#Records=gen/records
#rm $Records/*
#
#Keys=gen/keys
#rm $Keys/*
#}

function Usage() {
    echo "Usage: $0 libraryScheme rowSize colSize threadNum"
    echo "Which library-scheme do you want to test? Enter vector and matrix dimensions (row size, column size) and number of threads (threadNum)."
    echo "SEAL-BFV      test on data encrypted using BFV scheme in SEAL library."
    echo "SEAL-CKKS     test on data encrypted using CKKS scheme in SEAL library."
    echo "HELIB-BGV     test on data encrypted using BGV scheme in HElib library."
    echo "HELIB-CKKS    test on data encrypted using CKKS scheme in HElib library."
    exit
}

#mode=$1
libraryScheme=$1
rowSize=$2
colSize=$3
threadNum=$4

if [[ "$rowSize" == "" ]]; then
    rowSize=10
fi

if [[ "$colSize" == "" ]]; then
    colSize=1
fi

if [[ "$threadNum" == "" ]]; then
    threadNum=1
fi


if [[ "$libraryScheme" == "" ]]; then
      Usage
elif [[ "$libraryScheme" == "SEAL-BFV" ]]; then
  set_up_SEAL_BFV $rowSize $colSize
  run_SEAL_BFV $rowSize $colSize $threadNum
elif [[ "$libraryScheme" == "SEAL-CKKS" ]]; then
  set_up_SEAL_CKKS $rowSize $colSize
  run_SEAL_CKKS $rowSize $colSize $threadNum
elif [[ "$libraryScheme" == "HELIB-BGV" ]]; then
  set_up_HELIB_BGV $rowSize $colSize
  run_HELIB_BGV $rowSize $colSize $threadNum
elif [[ "$libraryScheme" == "HELIB-CKKS" ]]; then
  set_up_HELIB_CKKS $rowSize $colSize
  run_HELIB_CKKS $rowSize $colSize $threadNum
fi

#if [[ "$mode" == "setup" && "$libraryScheme" == "SEAL-BFV" ]]; then
#  set_up_SEAL_BFV $rowSize $colSize
#elif [[ "$mode" == "run" && "$libraryScheme" == "SEAL-BFV" ]]; then
#  run_SEAL_BFV $rowSize $colSize $threadNum
#elif [[ "$mode" == "setup" && "$libraryScheme" == "SEAL-CKKS" ]]; then
#  set_up_SEAL_CKKS $rowSize $colSize
#elif [[ "$mode" == "run" && "$libraryScheme" == "SEAL-CKKS" ]]; then
#  run_SEAL_CKKS $rowSize $colSize $threadNum
#elif [[ "$mode" == "setup" && "$libraryScheme" == "HELIB-BGV" ]]; then
#  set_up_HELIB_BGV $rowSize $colSize
#elif [[ "$mode" == "run" && "$libraryScheme" == "HELIB-BGV" ]]; then
#  run_HELIB_BGV $rowSize $colSize $threadNum
#elif [[ "$mode" == "setup" && "$libraryScheme" == "HELIB-CKKS" ]]; then
#  set_up_HELIB_CKKS $rowSize $colSize
#elif [[ "$mode" == "run" && "$libraryScheme" == "HELIB-CKKS" ]]; then
#  run_HELIB_CKKS $rowSize $colSize $threadNum
#elif [[ "$mode" == "cleanup" ]]; then
#  clean_up
#fi

#Records=gen/records
#rm $Records/*
#
#Keys=gen/keys
#rm $Keys/*


TestDir=evaluation
cd $TestDir