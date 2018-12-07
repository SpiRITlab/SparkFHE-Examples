# SparkFHE-Examples

Note, we provide the mvn wrapper script to export necessary library path.

Compile
```bash
./mvn -U clean comppile
```

Run JUnit5 Tests
```bash
./mvn test
```

Run examples
```bash
./mvn exec:java -Dexec.mainClass="spiritlab.sparkfhe.example.basic.BasicOPsExample"
```


Package into .jar
```bash
./mvn -U -DskipTests clean package
```


For developer, you can update the shared libraries manually and recompile as below.
```bash
./mvn -f resources/pom-devel.xml compile
```

