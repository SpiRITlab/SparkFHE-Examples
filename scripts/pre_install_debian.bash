#!/usr/bin/env bash

sudo apt-get install -y software-properties-common
sudo add-apt-repository ppa:webupd8team/java -y
sudo apt-get update
echo oracle-java8-installer shared/accepted-oracle-license-v1-1 select true | /usr/bin/debconf-set-selections
sudo apt-get install -y openjdk-8-jdk unzip libz-dev git vim build-essential m4 libpcre3-dev gcc-8 g++-8 cmake gradle python-dev oracle-java8-installer
sudo apt-get autoremove -y
sudo apt-get clean
