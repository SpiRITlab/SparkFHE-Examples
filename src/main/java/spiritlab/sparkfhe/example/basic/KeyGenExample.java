package spiritlab.sparkfhe.example.basic;

import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.example.Config;

import java.io.File;

/**
 * This is an example for SparkFHE project. Created to test the cryto-libraries basic function - generating a key.
 */
public class KeyGenExample {

    // Loading up the necessary libraries for Java and C++ interaction
    static {

        System.out.println("Execution path: " + System.getProperty("user.dir"));
        System.out.println("libSparkFHE path: " + System.getProperty("java.library.path"));
        try {
            System.loadLibrary("SparkFHE");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. \n" + e);
            System.exit(1);
        }
        System.out.println("Loaded native code library. \n");
    }

    public static void main(String argv[]) {
        // Creates the directory named by the pathname - current_directiory/gen/keys,
        // and including any necessary parent directories.
        new File(Config.DEFAULT_KEY_DIRECTORY).mkdirs();


        // Create SparkFHE object with HElib, a library that implements homomorphic encryption
        SparkFHE.init(FHELibrary.HELIB);

        // Using the object created to call the C++ function to generate the keys.
        SparkFHE.getInstance().generate_key_pair(Config.DEFAULT_CRYPTO_PARAMS_FILE, Config.DEFAULT_PUBLIC_KEY_FILE, Config.DEFAULT_SECRET_KEY_FILE);

        // Encrypting the literal 1, and decrypting it to verify the keys' accuracy.
        String inputNumber="1";
        String ctxt_string = SparkFHE.getInstance().encrypt(inputNumber);
        String ptxt_string = SparkFHE.getInstance().decrypt(ctxt_string);

        // Printing out the result
        System.out.println("InputNumber="+inputNumber + ", result of dec(enc(InputNumber))="+ptxt_string);
    }


}
