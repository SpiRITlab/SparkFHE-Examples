#!/usr/bin/env bash

mirrorServer=https://www-us.apache.org/dist/
hadoopVersion=2.8.5
sparkVersion=2.3.2

cluster=$1
eval $(echo $cluster | awk '{split($0, array, ",");for(i in array)print "host_array["i"]="array[i]}')

## local_ip replace localhost in config file
local_host="`hostname`"
local_ip=`host $local_host 2>/dev/null | awk '{print $NF}'`

## current_path replace data_path in config file
current_path=`pwd`
project_root_path=`cd ${current_path}/../.. && pwd`
#cd ${current_path}
#project_root_path=${current_path}/../..

echo $project_root_path

cd $project_root_path/deps


echo =========================================================
echo "Install Hadoop ($hadoopVersion) and Spark ($sparkVersion)"
echo =========================================================
hadoop_name=hadoop
spark_name=spark-hadoop-bin
#if [ -d "$hadoop_name" ] && [ -d "$spark_name" ]; then
#    echo "hadoop and spark exist!"
#    exit 2
#fi

hadoop_home=$project_root_path/deps/${hadoop_name}
if [ ! -d "$hadoop_name" ]; then
    if [ ! -f "hadoop-$hadoopVersion.tar.gz" ]; then
        #wget http://128.110.154.168:10086/hadoop-2.8.5.tar.gz
        wget $mirrorServer/hadoop/common/hadoop-$hadoopVersion/hadoop-$hadoopVersion.tar.gz
    fi
    tar -zxvf hadoop-$hadoopVersion.tar.gz
    mv hadoop-$hadoopVersion ${hadoop_name}
    cp -r ${current_path}/hadoop/* ${hadoop_home}/etc/hadoop/
    echo "Changing hadoop config files..."
    cd ${hadoop_name}/etc/hadoop
    find . -type f -print0 | xargs -0 sed -i "s/localhost/$local_ip/g"
    find . -type f -print0 | xargs -0 sed -i "s#data_path#$hadoop_home#g"
    for item in ${host_array[@]}; do
        echo $item > slaves
    done
    cd ../../..
    echo "export HADOOP_HOME=${hadoop_home}" > ${hadoop_home}/bashrc
    echo "export JAVA_HOME=$(dirname $(dirname $(readlink -f $(which javac))))" >> ${hadoop_home}/bashrc
    cat ${current_path}/bashrc/hadoop_bashrc >> ${hadoop_home}/bashrc
fi
source ${hadoop_home}/bashrc

spark_home=$project_root_path/deps/${spark_name}
if [ ! -d "$spark_name" ]; then
    if [ ! -f "spark-$sparkVersion-bin-hadoop2.7.tgz" ]; then
        wget $mirrorServer/spark/spark-$sparkVersion/spark-$sparkVersion-bin-hadoop2.7.tgz
    fi
    tar -zxvf spark-$sparkVersion-bin-hadoop2.7.tgz
    mv spark-$sparkVersion-bin-hadoop2.7 ${spark_name}
    cp -r ${current_path}/spark/* ${spark_home}/conf/
    echo "Changing spark config file..."
    cd ${spark_name}/conf
    find . -type f -print0 | xargs -0 sed -i "s/localhost/$local_ip/g"
    cd ../..
    echo "export SPARK_HOME=${spark_home}" > ${spark_home}/bashrc
    cat ${current_path}/bashrc/spark_bashrc >> ${spark_home}/bashrc
fi
source ${spark_home}/bashrc

echo ========================
echo Run Spark Example PI
echo ========================
hdfs namenode -format
hadoop-daemon.sh start namenode
hadoop-daemon.sh start datanode
yarn-daemon.sh start resourcemanager
yarn-daemon.sh start nodemanager
mr-jobhistory-daemon.sh start historyserver
hadoop dfs -mkdir -p /user/spark/eventLog/2.2.1
spark-submit --class org.apache.spark.examples.SparkPi     --master yarn     --deploy-mode client     --driver-memory 4g     --executor-memory 2g     --executor-cores 1  ${spark_home}/examples/jars/spark-examples_2.11-2.3.2.jar 10

if [ $? -ne 0 ]; then
    echo "Install spark on yarn error!"
    exit 2
fi


echo ========================
echo Install cluster
echo ========================

#echo "Make sure the machines in the cluster can ssh each other"
#echo "Yes(y) or No(n)"
#read flag
#case $flag in
#	Y|y) echo "you input  $flag !"
#	    echo scp hadoop and spark to other machines
#	;;
#	N|n) echo "you input  $flag !"
#	    exit 0
#	;;
#esac

for(( i=2;i<=${#host_array[@]};i++)) ; do
    # echo ssh ${host_array[i]} "mkdir -p ${current_path}"
    ssh ${host_array[i]} "mkdir -p $project_root_path/deps"
    echo "rsync -r ${hadoop_home} ${host_array[i]}:$project_root_path/deps/"
    echo "yes" | rsync -r ${hadoop_home} ${host_array[i]}:$project_root_path/deps/
    rsync -r ${spark_home} ${host_array[i]}:$project_root_path/deps/
    ssh ${host_array[i]} "source ${hadoop_home}/bashrc && ${hadoop_home}/sbin/hadoop-daemon.sh start datanode"
    ssh ${host_array[i]} "source ${hadoop_home}/bashrc && ${hadoop_home}/sbin/yarn-daemon.sh start nodemanager"
done

#for item in ${host_array[@]}
#do
#    scp -r ${hadoop_home} $item:${current_path}/
#    scp -r ${spark_home} $item:${current_path}/
#done

#hadoop-daemon.sh stop datanode
#hadoop-daemons.sh start datanode
#yarn-daemons.sh stop nodemanager
#yarn-daemons.sh start nodemanager

echo ========================== DONE =============================
