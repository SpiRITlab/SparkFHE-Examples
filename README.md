# SparkFHEJava

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
compile group: 'sparkfhe', name: 'sparkfhe-utils', version: '1.0-SNAPSHOT'
```

# Use remote maven repo
More information, https://github.com/SpiRITlab/SparkMavenRepo
