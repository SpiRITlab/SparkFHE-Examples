#!/usr/bin/env bash

Current_Dir=`pwd`
Scripts_Dir=$(dirname $Current_Dir)
source "$Scripts_Dir/cloudlab/include_functions.bash" "$Scripts_Dir"


get_nodes_info
echo "Configuring the following cluster nodes..."
print_list_of_nodes

authorize_access_between_nodes