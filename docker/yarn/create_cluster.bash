#!/bin/bash


# Setup the number of slaves based on user input
# N is the node number of hadoop cluster
N=$1

if [ $# = 0 ]
then
	echo "Please specify the number of nodes in cluster!!!"
	exit 1
fi

# change file that contains slave names
# Just lists all slaves in a file
i=1
rm config/slaves
while [ $i -lt $N ]
do
	echo "hadoop-slave$i" >> config/slaves
	((i++))
done 

image_name="spiritlab/sparkfhe-hadoop-cluster"

network_name="hadoop"

# Create Docker Network
docker network create --driver=bridge $network_name

# Generate hadoop master container
docker rm -f hadoop-master &> /dev/null
echo "Generate hadoop-master container"
docker run -itd \
                --net=$network_name \
                -p 50070:50070 \
                -p 8088:8088 \
                --name hadoop-master \
                --hostname hadoop-master \
                $image_name &> /dev/null              

# Move updates slaves file
docker cp config/slaves hadoop-master:/usr/local/hadoop/etc/hadoop/slaves

# get into hadoop master container
# docker exec -i hadoop-master bash -c "hadoop namenode -format"

# Generate hadoop slave container
i=1
while [ $i -lt $N ]
do
	docker rm -f hadoop-slave$i &> /dev/null
	echo "Generate hadoop-slave$i container"
	docker run -itd \
	                --net=$network_name \
	                --name hadoop-slave$i \
	                --hostname hadoop-slave$i \
	                $image_name &> /dev/null
	i=$(( $i + 1 ))
done 

# Move updates slaves file
i=1
while [ $i -lt $N ]
do
	docker cp config/slaves hadoop-slave$i:/usr/local/hadoop/etc/hadoop/slaves
	i=$(( $i + 1 ))
done 

echo "Entering Master Node on Docker"

# get into hadoop master container
docker exec -i hadoop-master bash -c "~/start_hadoop_cluster.bash"

echo "Exiting Master Node of Docker"

# URL="xdg-open http://"`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' hadoop-master`":8088"
URL="HADOOP DETAILS AT: http://"`docker inspect -f '{{range .NetworkSettings.Networks}}{{.IPAddress}}{{end}}' hadoop-master`":8088"
echo $URL