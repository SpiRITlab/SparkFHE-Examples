#!/usr/bin/env bash

Current_Dir=`pwd`
Scripts_Dir=$(dirname $Current_Dir)
source "$Scripts_Dir/cloudlab/include_functions.bash" "$Scripts_Dir"


function Usage() {
    echo "Usage: $0 secretKey"
    echo "Please login to Cloudlab.us and copy&paste the current 'Manifest.xml'"
    exit
}

ProvidedKey=$1
if [ "$ProvidedKey" == "" ]
then
  Usage
fi


get_nodes_info
echo "Configuring the following cluster nodes..."
print_list_of_nodes

authorize_access_between_nodes

git_pull_all $ProvidedKey



