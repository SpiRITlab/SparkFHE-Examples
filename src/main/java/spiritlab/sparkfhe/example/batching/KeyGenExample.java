//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.batching;

import org.apache.spark.spiritlab.sparkfhe.SparkFHEPlugin;
import spiritlab.sparkfhe.api.*;
import spiritlab.sparkfhe.example.Config;

import java.io.File;


/**
 * This is an example for SparkFHE project. Created to test the cryto-libraries basic function - generating a key.
 */
public class KeyGenExample {

    public static void main(String args[]) {
        String scheme="", library = "";

        Config.setExecutionEnvironment(args[0]);

        switch (Config.currentExecutionEnvironment) {
            case CLUSTER:
                Config.set_HDFS_NAME_NODE(args[1]);
                library = args[2];
                scheme = args[3];
                break;
            case LOCAL:
                library = args[1];
                scheme = args[2];
                break;
            default:
                break;
        }
        System.out.println("CURRENT_DIRECTORY = "+Config.get_current_directory());

        // Load C++ shared library
        SparkFHEPlugin.setup();
        // Create SparkFHE object with library
        SparkFHE.init(library, scheme);

        // Creates the directory named by the pathname - current_directiory/gen/keys,
        // and including any necessary parent directories.
         new File(Config.get_keys_directory()).mkdirs();

        // Using the object created to call the C++ function to generate the keys.
        SparkFHE.getInstance().generate_key_pair(
                Config.get_batch_crypto_params_file(library, scheme),
                Config.get_default_public_key_file(),
                Config.get_default_secret_key_file());

        // Encrypting the literal 1, and decrypting it to verify the keys' accuracy.
        String inputNumberString="1";
        Plaintext inputNumberPtxt = SparkFHE.getInstance().encode(inputNumberString);
        Ciphertext ctxt = SparkFHE.getInstance().encrypt(inputNumberPtxt);
        Plaintext ptxt = SparkFHE.getInstance().decrypt(ctxt);

        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleVector outputNumberPtxt = new DoubleVector();
            SparkFHE.getInstance().decode(outputNumberPtxt, ptxt);
            // Printing out the result
            System.out.println("InputNumber=" + inputNumberString + ", result of dec(enc(InputNumber))=" + String.valueOf(outputNumberPtxt.get(0)));
        } else { // BGV or BFV
            LongVector outputNumberPtxt = new LongVector();
            SparkFHE.getInstance().decode(outputNumberPtxt, ptxt);
            // Printing out the result
            System.out.println("InputNumber=" + inputNumberString + ", result of dec(enc(InputNumber))=" + String.valueOf(outputNumberPtxt.get(0)));
        }

    }
}
