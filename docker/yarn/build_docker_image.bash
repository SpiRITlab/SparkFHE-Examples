#!/bin/bash

image_name="cluster/hadoop"
image_name="$image_name:new"

network_name="hadoop"

echo -e "Building docker hadoop image\n"
# Create Docker Image
docker build -t $image_name .