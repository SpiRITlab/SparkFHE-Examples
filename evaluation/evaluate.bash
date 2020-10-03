#!/bin/bash

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
}


#### weak scaling
function run_weak_scale_nb() {
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 100 -c 1 -t 1
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 200 -c 1 -t 2
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 400 -c 1 -t 4
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 600 -c 1 -t 6
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 800 -c 1 -t 8
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1000 -c 1 -t 10
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1200 -c 1 -t 12
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1400 -c 1 -t 14
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1600 -c 1 -t 16
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 1800 -c 1 -t 18
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 2000 -c 1 -t 20
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 2200 -c 1 -t 22
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 2400 -c 1 -t 24
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 2600 -c 1 -t 26
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 2800 -c 1 -t 28
python3 testMultipleRuns.py -n 10 -s SEAL-BFV -r 3000 -c 1 -t 30
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