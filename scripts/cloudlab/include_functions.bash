#!/usr/bin/env bash

if [ "$0" == "include_functions.bash" ] ; then
    echo "This file contains internal functions. It should not be run directly."
    exit 2
fi


# debug code only
Current_Dir=`pwd`
echo "Path from include_functions.bash: $Current_Dir"
Scripts_Dir=$1
echo "Scripts path provided: $Scripts_Dir"
# debug code end

Cloudlab_Dir=$Scripts_Dir/cloudlab
Manifest_Filename="$Cloudlab_Dir/Manifest.xml";
MyUserName=`cat "$Cloudlab_Dir/myUserName.txt" | tr -d '\n'`

function get_nodes_info() {
    cluster_nodes=( `awk 'match($0, /<host name=\"*\"/) && sub(/name=/, "") && gsub(/\"/,"") {print $2}' $Manifest_Filename` )
    cluster_nodes_ip=( `awk 'match($0, /<host name=\"*\"/) && sub(/ipv4=/, "") && gsub(/\"/,"") && sub(/\/\>/,"") {print $3}' $Manifest_Filename` )
}

function get_concatenated_nodes_string() {
    concatenated_nodes_string="${cluster_nodes[0]}"
    for ((idx=1; idx<${#cluster_nodes[@]}; ++idx)); do
        concatenated_nodes_string="$concatenated_nodes_string,${cluster_nodes[idx]}"
    done
}

function print_list_of_nodes() {
    for ((idx=0; idx<${#cluster_nodes[@]}; ++idx)); do
        printf " Host: %s \t IP: %s\n" "${cluster_nodes[idx]}" "${cluster_nodes_ip[idx]}"
    done
}

function authorize_access_between_nodes() {
    echo "Authorizing access between nodes..."
    for ((idx=0; idx<${#cluster_nodes[@]}; ++idx)); do
        # Create the user SSH directory, just in case.
        # Retrieve the server-generated RSA private key.
        # Derive the corresponding public key portion.
        ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $MyUserName@${cluster_nodes[idx]} 'mkdir -p $HOME/.ssh && \
         chmod 700 $HOME/.ssh && \
         geni-get key > $HOME/.ssh/id_rsa && \
         chmod 600 $HOME/.ssh/id_rsa && \
         ssh-keygen -y -f $HOME/.ssh/id_rsa > $HOME/.ssh/id_rsa.pub'

        # If you want to permit login authenticated by the auto-generated key,
        # then append the public half to the authorized_keys file:
        ssh -o UserKnownHostsFile=/dev/null -o StrictHostKeyChecking=no $MyUserName@${cluster_nodes[idx]} 'grep -q -f $HOME/.ssh/id_rsa.pub $HOME/.ssh/authorized_keys || cat $HOME/.ssh/id_rsa.pub >> $HOME/.ssh/authorized_keys'
    done
}

function git_pull_all() {
    KeyName=$(basename $1)
    for ((idx=0; idx<${#cluster_nodes[@]}; ++idx)); do
        echo "Updating git on ${cluster_nodes[idx]}..."
        echo "yes" | scp $1 $MyUserName@${cluster_nodes[idx]}:~/.ssh/
        ssh -n -f $MyUserName@${cluster_nodes[idx]} "cd /SparkFHE && \
            git reset --hard && \
            git checkout master && \
            ssh-keyscan -H github.com >> ~/.ssh/known_hosts && \
            git pull && \
            rm ~/.ssh/$KeyName"
    done
}

