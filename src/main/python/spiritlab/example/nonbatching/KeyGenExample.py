###
# Copyright SpiRITlab - The SparkFHE project.
# https://github.com/SpiRITlab
###

import SparkFHE
from spiritlab.example.Config import Config
import os

print("CURRENT_DIRECTORY = "+Config.get_current_directory())
library="HELIB"
scheme="BGV"

# Create SparkFHE object with library
SparkFHE.init(library, scheme)

# Creates the directory named by the pathname - current_directiory/gen/keys,
# and including any necessary parent directories.
if not os.path.exists(Config.get_keys_directory()):
    os.makedirs(Config.get_keys_directory())

# Using the object created to call the C++ function to generate the keys.
SparkFHE.getInstance().generate_key_pair(
    Config.get_default_crypto_params_file(library),
    Config.get_default_public_key_file(),
    Config.get_default_secret_key_file()
)

# Encrypting the literal 1, and decrypting it to verify the keys' accuracy.
inputNumberString = "1"
inputNumberPtxt = SparkFHE.Plaintext(inputNumberString)
ctxt = SparkFHE.getInstance().encrypt(inputNumberPtxt)
ptxt = SparkFHE.getInstance().decrypt(ctxt, True);

# Printing out the result
print("InputNumber="+inputNumberString + ", result of dec(enc(InputNumber))="+ptxt.toString())


