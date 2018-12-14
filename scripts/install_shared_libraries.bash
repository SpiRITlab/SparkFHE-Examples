#!/usr/bin/env bash


GMP_Version=gmp-6.1.2
GF2X_Version=gf2x-1.2
NTL_Version=ntl-11.3.0
BOOST_Version=boost_1_68_0

# init
PROJECT_ROOT_PATH=`pwd`/".."
DEPS_PATH="$PROJECT_ROOT_PATH/shared_libraries"
libSparkFHE_root=$PROJECT_ROOT_PATH/libSparkFHE
libSparkFHE_include=$PROJECT_ROOT_PATH/libSparkFHE/include
libSparkFHE_lib=$PROJECT_ROOT_PATH/libSparkFHE/lib
libSparkFHE_share=$PROJECT_ROOT_PATH/libSparkFHE/share
mkdir -p $libSparkFHE_include $libSparkFHE_lib $libSparkFHE_share  $DEPS_PATH

cd $DEPS_PATH

# ========================
# Install dependencies
# ========================
BOOST="BOOST"
if [ ! -d $BOOST ]; then
    echo "Installing $BOOST..."
    wget https://dl.bintray.com/boostorg/release/1.68.0/source/$BOOST_Version.tar.bz2
    tar jxf "$BOOST_Version".tar.bz2
    rm "$BOOST_Version".tar.bz2
    mv $BOOST_Version $BOOST
    cd $BOOST
    ./bootstrap.sh --prefix=$libSparkFHE_root
    ./b2 install
    echo "Installing $BOOST... (DONE)"
    cd ..
fi

GoogleTEST="GoogleTEST"
if [ ! -d $GoogleTEST ]; then
    echo "Installing $GoogleTEST..."
    git clone https://github.com/google/googletest.git $GoogleTEST
    cd $GoogleTEST
    cmake -DCMAKE_CXX_COMPILER=g++-8 -DCMAKE_INSTALL_PREFIX=$libSparkFHE_root .
    make CXX=g++-8 LD=g++-8; make install
    echo "Installing $GoogleTEST... (DONE)"
    cd ..
fi

SWIG="SWIG"
if [ ! -d $SWIG ]; then
    echo "Installing $SWIG..."
    wget http://prdownloads.sourceforge.net/swig/$SWIG_Version.tar.gz
    tar xzf $SWIG_Version.tar.gz
    rm $SWIG_Version.tar.gz
    mv $SWIG_Version $SWIG
    cd $SWIG
    ./configure --prefix=$libSparkFHE_root --exec-prefix=$libSparkFHE_root
    make; make install
    echo "Installing $SWIG... (DONE)"
    cd ..
fi

JANSSON="JANSSON"
if [ ! -d $JANSSON ]; then
    echo "Installing $JANSSON..."
    git clone https://github.com/akheron/jansson.git $JANSSON
    cd $JANSSON
    cmake -DCMAKE_INSTALL_PREFIX=$libSparkFHE_root .
    make; make install
    echo "Installing $JANSSON... (DONE)"
    cd ..
fi

GMP="GMP"
if [ ! -d $GMP ]; then
    echo "Installing $GMP..."
    wget https://ftp.gnu.org/gnu/gmp/$GMP_Version.tar.bz2
    tar jxf $GMP_Version.tar.bz2
    rm $GMP_Version.tar.bz2
    mv $GMP_Version $GMP
    cd $GMP
    ./configure --prefix=$libSparkFHE_root --exec-prefix=$libSparkFHE_root
    make; make install
    echo "Installing $GMP... (DONE)"
    cd ..
fi

# download and install gf2x
GF2X="GF2X"
if [ ! -d $GF2X ]; then
    wget https://gforge.inria.fr/frs/download.php/file/36934/$GF2X_Version.tar.gz
    tar -xf $GF2X_Version.tar.gz
    rm $GF2X_Version.tar.gz
    mv $GF2X_Version GF2X
    cd GF2X
    ./configure ABI=64 CFLAGS="-m64 -O2 -fPIC" --prefix=$libSparkFHE_root
    make CXX=g++-8
    make CXX=g++-8 tune-lowlevel
    make CXX=g++-8 tune-toom
    make CXX=g++-8 check
    make CXX=g++-8 install
    cd ..
fi

# download and install NTL
NTL="NTL"
if [ ! -d $NTL ]; then
    echo "Installing $NTL..."
    wget http://www.shoup.net/ntl/$NTL_Version.tar.gz
    tar xzf $NTL_Version.tar.gz
    rm $NTL_Version.tar.gz
    mv $NTL_Version $NTL
    cd $NTL/src
    ./configure TUNE=x86 NTL_GF2X_LIB=on DEF_PREFIX=$libSparkFHE_root NTL_THREADS=on NTL_THREAD_BOOST=on NTL_GMP_LIP=on NATIVE=off CXX=g++-8
    make CXX=g++-8 CXXFLAGS="-fPIC -O3"
    make install
    echo "Installing $NTL... (DONE)"
    cd ../..
fi


# download and compile HElib
HElib="HElib"
if [ ! -d $HElib ]; then
    echo "Installing $HElib..."
    git clone https://github.com/shaih/HElib.git $HElib
    cd $HElib/src;
    make CC=g++-8 LD=g++-8 LDLIBS+=-L$libSparkFHE_lib CFLAGS+=-I$libSparkFHE_include CFLAGS+=-fPIC
    mkdir -p $libSparkFHE_include/HElib/
    cp *.h $libSparkFHE_include/HElib/
    cp fhe.a $libSparkFHE_lib/libfhe.a
    echo "Installing $HElib... (DONE)"
    cd ../..
fi

# download and install SEAL; due to copyright reason we can automatically fetch the package.
# download from here, https://www.microsoft.com/en-us/research/project/simple-encrypted-arithmetic-library/
# place the folder into deps and rename to "SEAL"
#SEAL="SEAL"
#if [ -d $SEAL ]; then
#    echo "Installing $SEAL..."
#    cd $SEAL/$SEAL
#    cmake .
#    make
#    echo "Installing $SEAL... (DONE)"
#    cd ../..
#else
#    echo "Please download Seal from https://www.microsoft.com/en-us/research/project/simple-encrypted-arithmetic-library/ "
#    echo "and put and rename the library to deps/SEAL before continue."
#    exit
#fi

#PALISADE="PALISADE"
#if [ ! -d $PALISADE ]; then
#    echo "Installing $PALISADE..."
#    git clone https://git.njit.edu/palisade/PALISADE.git $PALISADE
#    cd $PALISADE
#    make CXX=g++-8 LD=g++-8
#    echo "Installing $PALISADE... (DONE)"
#    cd ..
#fi


# Uncomment the follow code to install AWS SDK
# download and compile AWS SDK for c++
#https://docs.aws.amazon.com/sdk-for-cpp/v1/developer-guide/credentials.html
#AwsSDK="AwsSDK"
#if [ ! -d $AwsSDK ]; then
#    echo "Installing $AwsSDK..."
#    git clone https://github.com/aws/aws-sdk-cpp.git $AwsSDK
#    cd $AwsSDK;
#    cmake -DCMAKE_BUILD_TYPE=Release -DBUILD_ONLY="s3" .
#    make CC=g++-8 LD=g++-8 CFLAGS+=-fPIC
#    sudo make install
#    cp AWSSDK/* cmake/
#    echo "Installing $AwsSDK... (DONE)"
#    cd ..
#fi


cd $PROJECT_ROOT_PATH





