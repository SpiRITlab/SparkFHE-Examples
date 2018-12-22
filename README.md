# For Testers
If you want to go for a quick test drive of the SparkFHE code within the Apache Spark environment, please visit and follow these instructions, https://github.com/SpiRITlab/SparkFHE-Examples/wiki



# For developers
If you are a developer working on the SparkFHE-Examples code, you can use the following instructions to develop new example code. You will also need to setup the Apache Spark environment, because it will download and install all dependencies. If you haven't done so, please visit and follow these instructions, https://github.com/SpiRITlab/SparkFHE-Examples/wiki

Note, there are two pom files which you can use to compile or package:
```bash
pom-devel.xml                   # will use the existing shared lib within ./libSparkFHE/lib
pom.xml                         # will download and refresh the C++ shared lib from our repo
```

## 1. Soft-link the libSparkFHE folder
```bash
cd SparkFHE-Examples
ln -s PATH_TO_spark-3.0.0-SNAPSHOT-bin-SparkFHE ./
```

## 2. Get SparkFHE-Addon
```bash
./mvn -f pom-devel.xml scm:checkout
```

## 3. Compile the example code
The example code will use the shared libraries downloaded while setting up the Apache Spark environment.
```bash
./mvn -f pom-devel.xml -U clean compile
```
Note, without '-f pom-devel.xml', maven will download a new shared library from our repo and overwrite the existing one in libSparkFHE/lib.


## 4. Run the demo code
### Run it with a script 
```bash
bash myMavenExampleRun.bash
```

OR run it step-by-step
### Step 1. Generate necessary key pair and example ciphertexts (only needed to run once)
```bash
# this will generate the example key pair
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.KeyGenExample -Dexec.args="local" 

# this will generate some ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.EncDecExample -Dexec.args="local"      
```
### Step 2. Run examples: Test different FHE operations on example ciphertexts and vectors of ciphertexts
```bash
# this will perform some basic FHE operations
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.BasicOPsExample -Dexec.args="local  gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/records/ptxt_long_0_PlaintextModule73CiphertextModule9791MultiplicativeDepth10SecurityParameter80.json gen/records/ptxt_long_1_PlaintextModule73CiphertextModule9791MultiplicativeDepth10SecurityParameter80.json"

# this will perform dot product calculation on vectors of encrypted numbers 
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.DotProductExample -Dexec.args="local gen/keys/my_public_key.txt gen/keys/my_secret_key.txt gen/records/vec_a_5_PlaintextModule73CiphertextModule9791MultiplicativeDepth10SecurityParameter80.json gen/records/vec_b_5_PlaintextModule73CiphertextModule9791MultiplicativeDepth10SecurityParameter80.json"
```


## Run JUnit5 tests
```bash
./mvn -f pom-devel.xml test
```

## Package into .jar
```bash
./mvn -f pom-devel.xml -U -DskipTests clean package
```


# Known issues
You may see the these [warnings](https://github.com/SpiRITlab/SparkFHE-Examples/issues/7). It maybe due to Spark's internal bugs. 




