#!/bin/bash

echo "Hadoop Web-page will not work anymore"

echo "Entering Master Node on Docker"

# Stop all hadoop clusters
docker exec -i hadoop-master bash -c "./stop-hadoop.sh"

echo "Hadoop Cluster Removed"

echo "Exiting Master Node of Docker"

bash clean_environment.bash