# SparkFHE-Examples

Note, there are two pom files which you can use to compile or package:
```bash
pom.xml                   # will use the existing shared lib within ./libSparkFHE/lib
pom-refresh-lib.xml       # will refresh the C++ shared lib
```

Compile for the first time (so that, maven will download the shared lib)
```bash
./mvn -f pom-refresh-lib.xml clean compile
```

Subsequent compilation as following
```bash
./mvn clean compile
```

Package into .jar
```bash
./mvn -DskipTests package
```

Run examples
```bash
./mvn exec:java -Dexec.mainClass="spiritlab.sparkfhe.example.basic.BasicOPsExample"
```


