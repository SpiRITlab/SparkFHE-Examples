#!/bin/bash

image_name="spiritlab/sparkfhe-hadoop-cluster"

network_name="hadoop"

echo -e "Building docker hadoop image\n"

# Remove the relevant image to prevent dangling
[[ -z `docker image ls | grep $image_name` ]] && echo "$image_name does not exist" || docker rmi $image_name

# Create Docker Image
docker build -t $image_name .