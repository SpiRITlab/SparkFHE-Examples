#!/bin/bash

image_name="spiritlab/sparkfhe-hadoop-cluster"

network_name="hadoop"

echo -e "Deleting older containers and network\n"

# Stop all containers related to the image
docker ps -a | awk '{ print $1,$2 }' | grep $image_name | awk '{print $1 }' | xargs -I {} docker stop {}

# Remove all containers related to the image
docker ps -a | awk '{ print $1,$2 }' | grep $image_name | awk '{print $1 }' | xargs -I {} docker rm {}

# Delete previous network
docker network rm $network_name