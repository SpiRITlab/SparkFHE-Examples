## Running SparkFHE-Examples with HDFS support 

To test these examples, you will need to compile the libSparkFHE share library with HDFS module enabled.

From the SparkFHE source code, execute these commands:
```
$> bash bootstrap.bash HDFS
$> cmake -DUSE_HDFS=ON .
$> make
```

If you soft link your libSparkFHE directory to the one in the SparkFHE folder, then you are all set. If all your SparkFHE projects are at the same level, then exectue the following command within SparkFHE-Examples.
```
$> ln -s ../SparkFHE/libSparkFHE .
``` 

Otherwise, you need to update the libSparkFHE share library within the libSparkFHE/lib folder in your SparkFHE-Example project.

You will need to update the SparkFHE api (as .jar file) by executing this command within SparkFHE-Maven-Repo.
```
$> bash deploy.bash api
```
Once the new jar file is placed within your local maven repo, maven build tool should be able to grab it automatically.