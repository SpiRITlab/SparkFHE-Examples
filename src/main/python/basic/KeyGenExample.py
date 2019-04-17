#!/usr/bin/env python2.7
# -*- coding: utf-8 -*-

"""
Copyright SpiRITlab - The SparkFHE project.
License_info - https://github.com/SpiRITlab
"""

# Built-in/Generic Imports
import os

# Own modules
from spiritlab import SparkFHE

__author__ = 'Peizhao Hu'
__copyright__ = 'Copyright 2019, SparkFHE'
__credits__ = ['Qiaoran Li']
__version__ = '0.1.0'
__maintainer__ = 'SpiRITlab Members'
__email__ = 'hxpvcs@rit.edu'
__status__ = '{Dev}'


def pretty_title(title):
    """
    prints a title to the console before each function for easy reading
    :param title: a description for the function
    :return: None
    """
    print(title.center(80, '*'))


def generate_param_path(param_file_dir, filename):
    """
    forms a full path with the param file directory and the filename
    :param param_file_path: where the param file directory is
    :param filename: what the filename will be
    :return: the full param file path
    """
    return param_file_dir + '/' + filename


def generate_key_path(key_file_dir, filename):
    """
    forms a full path with the key file directory and the filename
    :param key_file_dir: where the key file directory is
    :param filename: what the filename will be
    :return: the full key file path
    """
    return key_file_dir + '/' + filename


def create_key_directory():
    """
    creates the directory needed to store the crypto parameters and keys
    :return: the crypto file directory and key file directory
    """
    pretty_title(' Directory Creation ')
    current_working_dir = os.getcwd()
    param_file_dir = current_working_dir + '/../../../../SparkFHE-Addon/resources/params'
    key_file_dir = current_working_dir + '/gen/keys'

    exists = os.path.isdir(key_file_dir)
    if not exists:
        try:
            os.makedirs(key_file_dir)
        except OSError as e:
            print ("Creation of the directory %s failed\n" % key_file_dir)
        else:
            print ("Successfully created the directory %s\n" % key_file_dir)
    else:
        print ("Directory %s already exist\n" % key_file_dir)

    return param_file_dir, key_file_dir


def basic_fhe_operation():
    """
    test the basic function with in the SparkFHE library
    :return: None
    """
    pretty_title(' Basic Operation ')
    print("1+0 = %s" %SparkFHE.add(1, 0))
    print("1*0 = %s" %SparkFHE.mul(1, 0))
    print("1+0 = %s\n" %SparkFHE.sub(1, 0))


def generate_keys(fhe, param_file_path, key_file_path):
    """
    test the key generation feature with in the SparkFHE library
    :param fhe: the SparkFHE object
    :param param_file_path: where crypto parameter will be stored
    :param key_file_path: where crypto key will be stored
    :return: None
    """
    pretty_title(' Key Generation ')

    fhe.generate_key_pair(generate_param_path(param_file_path, "CRYPTO_PARAMS_HELIB_TEMPLATE.json"),
                          generate_key_path(key_file_path, "my_public_key.txt"),
                          generate_key_path(key_file_path, "my_secret_key.txt"))
    print('\n')


def encrypt_decrypt_validation(fhe, input_number='1'):
    """
    test the encrypt and decrypt features of the SparkFHE library
    :param fhe: the SparkFHE object
    :param input_number: the plaintext number in string format
    :return: None
    """
    pretty_title(' Encrypt Decrypt Validation ')

    plaintext = SparkFHE.Plaintext(input_number)
    ciphertext = fhe.encrypt(plaintext)
    decrypted_plaintext = fhe.decrypt(ciphertext)
    print ("input_number=%s, result of dec(enc(input_number))=%s\n" % (input_number, decrypted_plaintext.toString()))


def main():

    # testing basic Sparkfhe operations
    basic_fhe_operation()

    # initialize vars and directories
    param_file_dir, key_file_dir = create_key_directory()

    # creating SparkFHE object with HElib homomorphic encryption
    fhe = SparkFHE.FHE("HELIB")

    # generating public and secret keys to directories initialized above
    generate_keys(fhe, param_file_dir, key_file_dir)

    # validate the encrypt and decrypt accuracy with a simple test
    encrypt_decrypt_validation(fhe)


if __name__ == '__main__':
    main()





