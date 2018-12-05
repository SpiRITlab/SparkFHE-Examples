# sparkfhe-examples

Download the C++ SparkFHE shared library
```
mvn process-resources
```


Install to local maven repository with
```bash
mvn install
```

Make sure the local maven repository is in the build.gradle settings
```groovy
repositories {
    mavenLocal()
}
```

Add this maven artifact as a dependency
```groovy
compile group: 'spiritlab.sparkfhe', name: 'sparkfhe-examples', version: '1.0-SNAPSHOT'
```

# Use remote maven repo
More information, https://github.com/SpiRITlab/SparkFHEMavenRepo
