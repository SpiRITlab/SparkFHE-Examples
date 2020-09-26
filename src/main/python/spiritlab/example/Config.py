###
# Copyright SpiRITlab - The SparkFHE project.
# https://github.com/SpiRITlab
###
import os

class Config(object):
    Current_Directory = os.path.dirname(os.path.abspath(__file__))+"../../../../../.."
    DEFAULT_CRYPTO_PARAMS_DIRECTORY = "/SparkFHE-Addon/resources/params"
    DEFAULT_COMMON_CRYPTO_PARAMS_FILE = "CRYPTO_PARAMS_COMMON_TEMPLATE.json"
    DEFAULT_HELIB_CRYPTO_PARAMS_FILE = "CRYPTO_PARAMS_HELIB_TEMPLATE.json"
    DEFAULT_SEAL_CRYPTO_PARAMS_FILENAME = "CRYPTO_PARAMS_SEAL_TEMPLATE.json"
    DEFAULT_PALISADE_CRYPTO_PARAMS_FILENAME = "CRYPTO_PARAMS_PALISADE_TEMPLATE.json"

    BATCH_HELIB_BGV_CRYPTO_PARAMS_FILENAME = "CRYPTO_PARAMS_HELIB_BGV_BATCH.json"
    BATCH_HELIB_CKKS_CRYPTO_PARAMS_FILENAME = "CRYPTO_PARAMS_HELIB_CKKS_BATCH.json"
    BATCH_SEAL_BFV_CRYPTO_PARAMS_FILENAME = "CRYPTO_PARAMS_SEAL_BFV_BATCH.json"
    BATCH_SEAL_CKKS_CRYPTO_PARAMS_FILENAME = "CRYPTO_PARAMS_SEAL_CKKS_BATCH.json"

    DEFAULT_KEY_DIRECTORY = "/gen/keys"
    DEFAULT_PUBLIC_KEY_FILE = "my_public_key.txt"
    DEFAULT_SECRET_KEY_FILE = "my_secret_key.txt"
    DEFAULT_RECORDS_DIRECTORY = "/gen/records"
    NUM_OF_VECTOR_ELEMENTS = 5
    Ciphertext_Label = "ctxt"

    ExecutionEnvironment = ["LOCAL", "CLUSTER"]
    EnumerateExecutionEnvironment=enumerate(ExecutionEnvironment)
    currentExecutionEnvironment="LOCAL"

    HDFS_NAME_NODE = "hdfs://localhost:0"
    HDFS_CURRENT_DIRECTORY = "/SparkFHE/HDFSFolder"

    def __init__(self):
        pass

    @staticmethod
    def get_current_directory():
        if (Config.currentExecutionEnvironment == "CLUSTER"):
            return Config.get_HDFS_path
        elif (Config.currentExecutionEnvironment == "LOCAL"):
            return Config.Current_Directory

    @staticmethod
    def get_default_public_key_file():
        return Config.get_current_directory() + Config.DEFAULT_KEY_DIRECTORY + "/" + Config.DEFAULT_PUBLIC_KEY_FILE

    @staticmethod
    def get_default_secret_key_file():
        return Config.get_current_directory() + Config.DEFAULT_KEY_DIRECTORY + "/" + Config.DEFAULT_SECRET_KEY_FILE

    @staticmethod
    def get_keys_directory():
        return Config.get_current_directory() + Config.DEFAULT_KEY_DIRECTORY

    @staticmethod
    def get_HDFS_path():
        return Config.HDFS_NAME_NODE + Config.HDFS_CURRENT_DIRECTORY

    @staticmethod
    def get_default_crypto_params_file(lib_name):
        crypto_param_file = Config.DEFAULT_COMMON_CRYPTO_PARAMS_FILE
        if (lib_name == "HELIB"):
            crypto_param_file = Config.DEFAULT_HELIB_CRYPTO_PARAMS_FILE
        elif (lib_name == "SEAL"):
            crypto_param_file = Config.DEFAULT_SEAL_CRYPTO_PARAMS_FILENAME
        elif (lib_name == "PALISADE"):
            crypto_param_file = Config.DEFAULT_PALISADE_CRYPTO_PARAMS_FILENAME

        if (Config.currentExecutionEnvironment == "CLUSTER"):
            return Config.get_HDFS_path() + Config.DEFAULT_CRYPTO_PARAMS_DIRECTORY + "/" + crypto_param_file
        elif (Config.currentExecutionEnvironment == "LOCAL"):
            return Config.get_current_directory() + Config.DEFAULT_CRYPTO_PARAMS_DIRECTORY + "/" + crypto_param_file