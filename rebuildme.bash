#!/usr/bin/env bash
  
mvn -DuniqueVersion=false package
mvn -DuniqueVersion=false install
