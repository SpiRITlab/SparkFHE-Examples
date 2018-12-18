# For Testers
If you want to go for a quick test drive of the SparkFHE code within the Apache Spark environment, please visit and follow these instructions, https://github.com/SpiRITlab/SparkFHE-Examples/wiki



# For developers
If you are a developer working on the SparkFHE-Examples code, you can use the following instructions to develop new example code. You will also need to setup the Apache Spark environment, because it will download and install all dependencies. If you haven't done so, please visit and follow these instructions, https://github.com/SpiRITlab/SparkFHE-Examples/wiki

Note, there are two pom files which you can use to compile or package:
```bash
pom-devel.xml                   # will use the existing shared lib within ./libSparkFHE/lib
pom.xml                         # will download and refresh the C++ shared lib from our repo
```

## Soft-link the libSparkFHE folder
```bash
ln -s PATH_TO_spark-3.0.0-SNAPSHOT-bin-SparkFHE ./
```

## Compile the example code
The example code will use the shared libraries downloaded while setting up the Apache Spark environment.
```bash
./mvn -f pom-devel.xml -U clean compile
```
Note, without '-f pom-devel.xml', maven will download a new shared library from our repo and overwrite the existing one in libSparkFHE/lib.


## Run the demo code
### Run it with a script 
```bash
cd scripts
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
You may see the following warnings. It maybe due to Spark's internal bugs. 
```bash
18/12/17 23:44:58 WARN FileSystem: exception in the cleaner thread but it will continue to run
java.lang.InterruptedException
	at java.lang.Object.wait(Native Method)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:143)
	at java.lang.ref.ReferenceQueue.remove(ReferenceQueue.java:164)
	at org.apache.hadoop.fs.FileSystem$Statistics$StatisticsDataReferenceCleaner.run(FileSystem.java:3063)
	at java.lang.Thread.run(Thread.java:745)
[WARNING] thread Thread[org.apache.hadoop.fs.FileSystem$Statistics$StatisticsDataReferenceCleaner,5,spiritlab.sparkfhe.example.basic.DotProductExample] was interrupted but is still alive after waiting at least 12884msecs
[WARNING] thread Thread[org.apache.hadoop.fs.FileSystem$Statistics$StatisticsDataReferenceCleaner,5,spiritlab.sparkfhe.example.basic.DotProductExample] will linger despite being asked to die via interruption
[WARNING] thread Thread[shuffle-chunk-fetch-handler-4-1,5,spiritlab.sparkfhe.example.basic.DotProductExample] will linger despite being asked to die via interruption
[WARNING] thread Thread[broadcast-exchange-0,5,spiritlab.sparkfhe.example.basic.DotProductExample] will linger despite being asked to die via interruption
[WARNING] NOTE: 3 thread(s) did not finish despite being asked to  via interruption. This is not a problem with exec:java, it is a problem with the running code. Although not serious, it should be remedied.
[WARNING] Couldn't destroy threadgroup org.codehaus.mojo.exec.ExecJavaMojo$IsolatedThreadGroup[name=spiritlab.sparkfhe.example.basic.DotProductExample,maxpri=10]
java.lang.IllegalThreadStateException
	at java.lang.ThreadGroup.destroy(ThreadGroup.java:778)
	at org.codehaus.mojo.exec.ExecJavaMojo.execute(ExecJavaMojo.java:321)
	at org.apache.maven.plugin.DefaultBuildPluginManager.executeMojo(DefaultBuildPluginManager.java:134)
	at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:207)
	at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:153)
	at org.apache.maven.lifecycle.internal.MojoExecutor.execute(MojoExecutor.java:145)
	at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject(LifecycleModuleBuilder.java:116)
	at org.apache.maven.lifecycle.internal.LifecycleModuleBuilder.buildProject(LifecycleModuleBuilder.java:80)
	at org.apache.maven.lifecycle.internal.builder.singlethreaded.SingleThreadedBuilder.build(SingleThreadedBuilder.java:51)
	at org.apache.maven.lifecycle.internal.LifecycleStarter.execute(LifecycleStarter.java:128)
	at org.apache.maven.DefaultMaven.doExecute(DefaultMaven.java:307)
	at org.apache.maven.DefaultMaven.doExecute(DefaultMaven.java:193)
	at org.apache.maven.DefaultMaven.execute(DefaultMaven.java:106)
	at org.apache.maven.cli.MavenCli.execute(MavenCli.java:863)
	at org.apache.maven.cli.MavenCli.doMain(MavenCli.java:288)
	at org.apache.maven.cli.MavenCli.main(MavenCli.java:199)
	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
	at java.lang.reflect.Method.invoke(Method.java:498)
	at org.codehaus.plexus.classworlds.launcher.Launcher.launchEnhanced(Launcher.java:289)
	at org.codehaus.plexus.classworlds.launcher.Launcher.launch(Launcher.java:229)
	at org.codehaus.plexus.classworlds.launcher.Launcher.mainWithExitCode(Launcher.java:415)
	at org.codehaus.plexus.classworlds.launcher.Launcher.main(Launcher.java:356)
```





