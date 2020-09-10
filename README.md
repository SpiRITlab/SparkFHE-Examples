# SparkFHE: Distributed Dataflow Framework with Fully Homomorphic Encryption

Cloud computing is becoming indispensable for performing big data analysis and building machine learning pipelines at scale through massive parallelization of tasks across large clusters of computing nodes. In datacenters the large number of computing resources and tasks are managed by distributed data processing frameworks, such as Apache Spark. These frameworks offer data and programming abstractions that ease the development of data analytics algorithms, and provide cluster management and scheduling algorithms that facilitate efficient resource orchestration and task allocation. 

While efficiency in data processing is achieved, outsourcing computation on private data to the Cloud often leads to privacy concerns when the data is sensitive. Even if data is protected with encryption while in transmission or in storage, it has to be decrypted before being processed by Spark.  

## Want to learn more about Spark and FHE?

### Apache Spark, a highly efficient data processing framework for the cloud. 
- Apache Spark: A Unified Engine For Big Data Processing. Matei Zaharia, Reynold S. Xin, Patrick Wendell, Tathagata Das, Michael Armbrust, Ankur Dave, Xiangrui Meng, Josh Rosen, Shivaram Venkataraman, Michael J. Franklin, Ali Ghodsi, Joseph Gonzalez, Scott Shenker, Ion Stoica. Communications of the ACM, November 2016, Vol. 59 No. 11, Pages 56-65. DOI 10.1145/2934664

### FHE, a cryptography technique that enables arithmetic computations on encrypted data without decrypting it first.
- "Computing arbitrary functions of encrypted data". Craig Gentry. Communications of the ACM. March 2010. DOI 1666420.1666444.
- "Technical Perspective: A First Glimpse of Cryptography's Holy Grail". Daniele Micciancio.  Communications of the ACM, March 2010, Vol. 53 No. 3, Page 96. DOI 10.1145/1666420.1666445

## What is SparkFHE?
![SparkFHE Architecture](https://www.cs.rit.edu/~ph/sites/default/files/inline-images/SparkFHEArchitecture-crop.png)

SparkFHE integrates Apache Spark with fully homomorphic encryption – an encryption technology that allows computing directly on encrypted data without requiring a secret key. This integration makes two novel contributions to  large-scale secure data analytics in the Cloud: (1) enabling Spark to perform efficient computation on large datasets while preserving user privacy, and (2) accelerating intensive homomorphic computation through parallelization of tasks across clusters of computing nodes. To our best knowledge, SparkFHE is the first addressing these two needs simultaneously. This project is in collaboration with Microsoft Research, Redmond. 

[Learn more about this project in our research group page.](https://www.cs.rit.edu/~ph/PrivateComputation)

## Give it a try

### For Testers
If you want to go for a quick test drive of the SparkFHE code within the Apache Spark environment, please visit and follow these instructions, [https://github.com/SpiRITlab/SparkFHE-Examples/wiki](https://github.com/SpiRITlab/SparkFHE-Examples/wiki)



### For Developers
If you are a developer working on the SparkFHE-Examples code, you can use the following instructions to develop new example code. You will also need to setup the Apache Spark environment, because it will download and install all dependencies. If you haven't done so, please visit and follow these instructions, [https://github.com/SpiRITlab/SparkFHE-Examples/wiki](https://github.com/SpiRITlab/SparkFHE-Examples/wiki)

Note, there are two pom files which you can use to compile or package:
```bash
pom-devel.xml                   # will use the existing shared lib within ./libSparkFHE/lib
pom.xml                         # will download and refresh the C++ shared lib from our repo
```

#### 1. Soft-link the libSparkFHE folder
Note, change ${PATH_TO_spark-3.1.0-SNAPSHOT-bin-SparkFHE} to where you install "spark-3.1.0-SNAPSHOT-bin-SparkFHE"
```bash
cd SparkFHE-Examples
ln -s ${PATH_TO_spark-3.1.0-SNAPSHOT-bin-SparkFHE}/deps/ ./
```

#### 2. Get SparkFHE-Addon
```bash
./mvn -f pom-devel.xml scm:checkout
```
or
```bash
git submodule update
```

#### 3. Compile the example code
The example code will use the shared libraries downloaded while setting up the Apache Spark environment.
```bash
./mvn -f pom-devel.xml -U clean compile
```
Note, without '-f pom-devel.xml', maven will download a new shared library from our repo and overwrite the existing one in libSparkFHE/lib.


#### 4.1 Run the demo code with a script 
```bash
cd scripts
bash myMavenExampleRun.bash
```

#### Or, 4.2 Run the demo code step-by-step
##### Step 1. Generate necessary key pair and example ciphertexts (only needed to run once)
```bash
# this will generate the example key pair
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.KeyGenExample -Dexec.args="local" 

# this will generate some ciphertexts
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.EncDecExample -Dexec.args="local gen/keys/my_public_key.txt gen/keys/my_secret_key.txt"      
```
##### Step 2. Run examples: Test different FHE operations on example ciphertexts and vectors of ciphertexts
```bash
# this will perform some basic FHE operations
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.BasicOPsExample -Dexec.args="local  gen/keys/my_public_key.txt gen/keys/my_secret_key.txt"

# this will perform dot product calculation on vectors of encrypted numbers 
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.basic.DotProductExample -Dexec.args="local gen/keys/my_public_key.txt gen/keys/my_secret_key.txt"
```


### Run JUnit5 tests
```bash
./mvn -f pom-devel.xml test
```

### Package into .jar
```bash
./mvn -f pom-devel.xml -U -DskipTests clean package
```


## Known issues

1. `Symbol not found` and `Expected in: /usr/lib/libstdc++.6.dylib`
Reason: This is due to the misalignment of the C++ compiler in your runtime and our libSparkFHE shared library.
Resolution: Recompile the C++ ibSparkFHE shared library.


You may see the these [warnings](https://github.com/SpiRITlab/SparkFHE-Examples/issues/7). It maybe due to Spark's internal bugs. 




