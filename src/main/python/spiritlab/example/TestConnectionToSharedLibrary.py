###
# Copyright SpiRITlab - The SparkFHE project.
# https://github.com/SpiRITlab
###

import SparkFHE

print("Loading Python API and C++ shared library...")
sum = SparkFHE.add(5, 2)
assert sum == 7, "Something wrong!!!"
SparkFHE.FHE("HELIB", "BGV")
print("Python API and C++ shared library loaded successfully!")