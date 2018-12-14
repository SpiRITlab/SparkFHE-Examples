#!/bin/bash

# This script will run four SparkFHE examples (Key Generation, Encryption/Decryption, Basic Arithmetics, Dot-Product).


# uncomment the following if verbose mode is needed
#verbose="--verbose"

SparkDistributionRoot=..
cd $SparkDistributionRoot

master=local
deploy_mode=client

ivysettings_file=resources/ivysettings.xml
jar_sparkfhe_examples=jars/$(ls jars | grep sparkfhe-examples-)
jar_sparkfhe_api=jars/$(ls jars | grep sparkfhe-api-)
jar_sparkfhe_plugin=jars/$(ls jars | grep spark-fhe)
libSparkFHE_path=libSparkFHE/lib
java_class_path=.:jars



# run the actual spark submit job
# inputs: (spark_job_name, main_class_to_run)
function run_spark_submit_command() {
	spark_job_name=$1 
	main_class_to_run=$2 
	rm -rf  ~/.ivy2
	bin/spark-submit $verbose \
		--name $spark_job_name \
		--master $master \
		--deploy-mode $deploy_mode \
		--class $main_class_to_run \
		--jars $jar_sparkfhe_api,$jar_sparkfhe_plugin \
		--conf spark.jars.ivySettings="$ivysettings_file" \
		--driver-class-path $java_class_path \
		--conf spark.driver.userClassPathFirst=true \
		--conf spark.driver.extraClassPath="$java_class_path" \
		--conf spark.driver.extraLibraryPath="$libSparkFHE_path" \
		--conf driverExtraJavaOptions="-Djava.library.path=$libSparkFHE_path"  \
		--conf spark.executor.extraClassPath="$java_class_path" \
		--conf spark.executor.extraLibraryPath="$libSparkFHE_path" \
		$jar_sparkfhe_examples local $3 $4 $5 $6
}


export MAVEN_OPTS="-Xmx2g -XX:ReservedCodeCacheSize=512m"

# generate example key pairs
run_spark_submit_command  sparkfhe_keygen  spiritlab.sparkfhe.example.basic.KeyGenExample

# generate example ciphertexts
run_spark_submit_command  sparkfhe_encryption_decryption  spiritlab.sparkfhe.example.basic.EncDecExample

# run basic FHE arithmetic operation over encrypted data
run_spark_submit_command  sparkfhe_basic_examples  spiritlab.sparkfhe.example.basic.BasicOPsExample "gen/keys/my_public_key.txt" "gen/keys/my_secret_key.txt"   "gen/records/$(ls gen/records | grep ptxt_long_0)" "gen/records/$(ls gen/records | grep ptxt_long_1)"

# run FHE dot product over two encrypted vectors
run_spark_submit_command  sparkfhe_dot_product_examples  spiritlab.sparkfhe.example.basic.DotProductExample  "gen/keys/my_public_key.txt" "gen/keys/my_secret_key.txt"   "gen/records/$(ls gen/records | grep vec_a)" "gen/records/$(ls gen/records | grep vec_b)"









