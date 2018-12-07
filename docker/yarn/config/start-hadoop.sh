#!/bin/bash

echo -e "\n"
# cat $HADOOP_HOME/etc/hadoop/slaves
$HADOOP_HOME/sbin/start-dfs.sh

echo -e "\n"

$HADOOP_HOME/sbin/start-yarn.sh

echo -e "\n"

