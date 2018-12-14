#!/usr/bin/env bash

Current_Dir=`pwd`
Scripts_Dir=$(dirname $Current_Dir)
source "$Scripts_Dir/cloudlab/include_functions.bash" "$Scripts_Dir"

get_nodes_info
echo "Configuring the following cluster nodes..."
print_list_of_nodes

DEPS_PATH="/SparkFHE/deps"

GMP="GMP"
NTL="NTL"
HElib="HElib"
 for ((idx=0; idx<${#cluster_nodes[@]}; ++idx)); do
        ssh -n -f $MyUserName@${cluster_nodes[idx]} "cd $DEPS_PATH && \
            [[ -d $GMP ]] && \
            cd $GMP && \
            make clean && \
            ./configure --prefix $DEPS_PATH/$GMP --exec-prefix $DEPS_PATH/$GMP && \
            make && \
            make install && \
            cd $DEPS_PATH && \
            [[ -d $NTL ]] && \
            cd $NTL/src && \
            make clean && \
            ./configure NTL_THREADS=on NTL_THREAD_BOOST=on NTL_GMP_LIP=on GMP_PREFIX=$DEPS_PATH/$GMP && \
            make CXX=g++-8 CXXFLAGS=-fPIC && \
            cd $DEPS_PATH && \
            [[ -d $HElib ]] && \
            cd $HElib/src && \
            make clean && \
            make CC=g++-8 LD=g++-8 LDLIBS+=-L$DEPS_PATH/NTL/src CFLAGS+=-I$DEPS_PATH/NTL/include CFLAGS+=-fPIC && \
            cd /SparkFHE && \
            cmake . && \
            make"
done

echo "Recompiling SparkFHE... (DONE)"