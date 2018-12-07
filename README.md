# SparkFHE-Examples

Compile
```bash
./mvn comppile
```

Package into .jar
```bash
./mvn -DskipTests package
```

Run examples
```bash
./mvn exec:java -Dexec.mainClass="spiritlab.sparkfhe.example.basic.BasicOPsExample"
```


For developer, you can update the shared libraries manually and recompile as below.
```bash
./mvn -f resources/pom-devel.xml compile
```

