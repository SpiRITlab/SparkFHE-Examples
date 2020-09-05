#!/usr/bin/env bash

ProjectRoot=..
cd $ProjectRoot

# test connection between Java and C++ shared library
./mvn -f pom-devel.xml exec:java -Dexec.mainClass=spiritlab.sparkfhe.example.TestConnectionToSharedLibrary
