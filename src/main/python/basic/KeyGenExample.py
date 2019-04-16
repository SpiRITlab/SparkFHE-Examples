##
# Copyright SpiRITlab - The SparkFHE project.
# https://github.com/SpiRITlab
##

import os
cwd = os.getcwd()

param_file_path = "%s/../../../SparkFHE-Addon/resources/params" % cwd
def generate_param_path(filename):
    return "%s/%s" % (param_file_path,filename)

key_file_path = "%s/gen/keys" % cwd
def generate_key_path(filename):
    return "%s/%s" % (key_file_path,filename)

try:
    os.mkdir(key_file_path)
except OSError:
    print ("Creation of the directory %s failed" % key_file_path)
else:
    print ("Successfully created the directory %s " % key_file_path)


from spiritlab import SparkFHE

print("1+0=", SparkFHE.add(1,0))
print("1*0=", SparkFHE.mul(1,0))
print("1+0=", SparkFHE.sub(1,0))

fhe = SparkFHE.FHE("HELIB")


fhe.generate_key_pair(generate_param_path("CRYPTO_PARAMS_HELIB_TEMPLATE.json"),
                      generate_key_path("my_public_key.txt"),
                        generate_key_path("my_secret_key.txt"))

inputNumber="1"
ptxt = SparkFHE.Plaintext(inputNumber);
ctxt = fhe.encrypt(ptxt);
ptxt_result = fhe.decrypt(ctxt)

print "inputNumber=%s, result of dec(enc(inputNumber))=%s" % (inputNumber, ptxt_result.toString())










