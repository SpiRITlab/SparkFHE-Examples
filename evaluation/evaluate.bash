#!/bin/bash



function setup() {
project_dir=/users/aaloufi/SparkFHE-Examples
cd $project_dir

# generate example ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.EncDecExample -Dexec.args="local SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt $1" >> setup_log.txt 2>&1

#./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.EncDecExample -Dexec.args="local SEAL BFV gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/keys/my_relin_keys.txt gen/keys/my_galois_keys.txt $1 1" >> setup_log.txt 2>&1

rm gen/records/ctxt_long_*
cd $project_dir/evaluation
}

function cleanup() {
project_dir=/users/aaloufi/SparkFHE-Examples
rm $project_dir/gen/records/*
cd $project_dir/evaluation
}


### Strong scaling
function run_benchmark() {
### SEAL-CKKS ##
python3 testMultipleRuns.py -n 10 -s SEAL-CKKS -r 10 -c 1 -t 8

#### HELIB-CKKS ##
python3 testMultipleRuns.py -n 10 -s HELIB-CKKS -r 10 -c 1 -t 8

# SEAL-BFV ##
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10 -c 1 -t 2
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 100 -c 1 -t 2
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000 -c 1 -t 2

python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10 -c 1 -t 4
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 100 -c 1 -t 4
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000 -c 1 -t 4

python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10 -c 1 -t 8
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 100 -c 1 -t 8
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000 -c 1 -t 8

python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10 -c 1 -t 16
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 100 -c 1 -t 16
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000 -c 1 -t 16

### HELIB-BGV ##
python3 testMultipleRuns.py -n 10 -s HELIB-BGV -r 10 -c 1 -t 2
python3 testMultipleRuns.py -n 10 -s HELIB-BGV -r 100 -c 1 -t 2
python3 testMultipleRuns.py -n 10 -s HELIB-BGV -r 1000 -c 1 -t 2

python3 testMultipleRuns.py -n 10 -s HELIB-BGV -r 10 -c 1 -t 4
python3 testMultipleRuns.py -n 10 -s HELIB-BGV -r 100 -c 1 -t 4
python3 testMultipleRuns.py -n 10 -s HELIB-BGV -r 1000 -c 1 -t 4

python3 testMultipleRuns.py -n 10 -s HELIB-BGV -r 10 -c 1 -t 8
python3 testMultipleRuns.py -n 10 -s HELIB-BGV -r 100 -c 1 -t 8
python3 testMultipleRuns.py -n 10 -s HELIB-BGV -r 1000 -c 1 -t 8

python3 testMultipleRuns.py -n 10 -s HELIB-BGV -r 10 -c 1 -t 16
python3 testMultipleRuns.py -n 10 -s HELIB-BGV -r 100 -c 1 -t 16
python3 testMultipleRuns.py -n 10 -s HELIB-BGV -r 1000 -c 1 -t 16
}


### Strong scaling
function run_strong_scale() {

cd ..
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.KeyGenExample -Dexec.args="local SEAL BFV" >> setup_log.txt 2>&1

setup 10000

python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 1
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 2
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 4
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 6
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 8
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 10
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 12
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 14
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 16
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 18
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 20
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 22
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 24
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 26
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 28
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 30

cleanup
}


#### weak scaling
function run_weak_scale_nb() {

cd ..
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.KeyGenExample -Dexec.args="local SEAL BFV" >> setup_log.txt 2>&1

setup 100
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 100 -c 1 -t 1
cleanup

setup 200
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 200 -c 1 -t 2
cleanup

setup 400
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 400 -c 1 -t 4
cleanup

setup 600
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 600 -c 1 -t 6
cleanup

setup 800
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 800 -c 1 -t 8
cleanup

setup 1000
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000 -c 1 -t 10
cleanup

setup 1200
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1200 -c 1 -t 12
cleanup

setup 1400
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1400 -c 1 -t 14
cleanup

setup 1600
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1600 -c 1 -t 16
cleanup

setup 1800
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1800 -c 1 -t 18
cleanup

setup 2000
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 2000 -c 1 -t 20
cleanup

setup 2200
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 2200 -c 1 -t 22
cleanup

setup 2400
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 2400 -c 1 -t 24
cleanup

setup 2600
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 2600 -c 1 -t 26
cleanup

setup 2800
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 2800 -c 1 -t 28
cleanup

setup 3000
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 3000 -c 1 -t 30
cleanup
}


function run_weak_scale_b() {

cd ..
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.packing.KeyGenExample -Dexec.args="local SEAL BFV" >> setup_log.txt 2>&1

setup 100
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 100 -c 1 -t 1
cleanup

setup 8194
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 8194 -c 1 -t 2
cleanup

setup 24578
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 24578 -c 1 -t 4
cleanup

setup 40962
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 40962 -c 1 -t 6
cleanup

setup 57346
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 57346 -c 1 -t 8
cleanup

setup 73730
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 73730 -c 1 -t 10
cleanup

setup 90114
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 90114 -c 1 -t 12
cleanup

setup 106498
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 106498 -c 1 -t 14
cleanup

setup 122882
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 122882 -c 1 -t 16
cleanup

setup 139266
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 139266 -c 1 -t 18
cleanup

setup 155650
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 155650 -c 1 -t 20
cleanup

setup 172034
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 172034 -c 1 -t 22
cleanup

setup 188418
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 188418 -c 1 -t 24
cleanup

setup 204802
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 204802 -c 1 -t 26
cleanup

setup 221186
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 221186 -c 1 -t 28
cleanup

setup 237570
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 237570 -c 1 -t 30
cleanup
}

### speedup +
function run_speedup_million() {
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000000 -c 1 -t 1
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000000 -c 1 -t 2
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000000 -c 1 -t 4
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000000 -c 1 -t 6
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000000 -c 1 -t 8
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000000 -c 1 -t 10
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000000 -c 1 -t 12
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000000 -c 1 -t 14
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000000 -c 1 -t 16
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000000 -c 1 -t 18
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000000 -c 1 -t 20
}

function run_speedup_ten_million() {
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000000 -c 1 -t 1
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000000 -c 1 -t 2
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000000 -c 1 -t 4
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000000 -c 1 -t 6
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000000 -c 1 -t 8
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000000 -c 1 -t 10
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000000 -c 1 -t 12
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000000 -c 1 -t 14
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000000 -c 1 -t 16
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000000 -c 1 -t 18
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000000 -c 1 -t 20
}


function run_levels() {
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 10000 -c 1 -t 20
}

tests=$1

if [[ "$tests" == "benchmark" ]]; then
  run_benchmark
elif [[ "$tests" == "strong" ]]; then
  run_strong_scale
elif [[ "$tests" == "weak_nb" ]]; then
  run_weak_scale_nb
elif [[ "$tests" == "weak_b" ]]; then
  run_weak_scale_b
elif [[ "$tests" == "speedup" ]]; then
  run_speedup_million
elif [[ "$tests" == "speedup2" ]]; then
  run_speedup_ten_million
elif [[ "$tests" == "levels" ]]; then
  run_levels
fi



