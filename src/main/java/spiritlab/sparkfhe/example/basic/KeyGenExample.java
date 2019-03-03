package spiritlab.sparkfhe.example.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.spiritlab.sparkfhe.SparkFHESetup;
import org.apache.spark.sql.SparkSession;
import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.example.Config;

import java.io.File;

/**
 * This is an example for SparkFHE project. Created to test the cryto-libraries basic function - generating a key.
 */
public class KeyGenExample {

    public static void main(String args[]) {
        int slices = 2;
        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("KeyGenExample");

        Config.setExecutionEnvironment(args[0]);

        switch (Config.currentExecutionEnvironment) {
            case CLUSTER:
                slices = Integer.parseInt(args[0]);
                break;
            case LOCAL:
                sparkConf.setMaster("local");
                Config.update_current_directory(sparkConf.get("spark.mesos.executor.home"));
                System.out.println("CURRENT_DIRECTORY = "+Config.get_current_directory());
                break;
            default:
                break;
        }

        // Load C++ shared library
        SparkFHESetup.setup();
        // Create SparkFHE object with HElib, a library that implements homomorphic encryption
        SparkFHE.init(FHELibrary.HELIB);

        // Creates the directory named by the pathname - current_directiory/gen/keys,
        // and including any necessary parent directories.
        new File(Config.get_keys_directory()).mkdirs();
        // Using the object created to call the C++ function to generate the keys.
        SparkFHE.getInstance().generate_key_pair(Config.get_default_crypto_params_file(FHELibrary.HELIB), Config.get_default_public_key_file(), Config.get_default_secret_key_file());
      
        // Encrypting the literal 1, and decrypting it to verify the keys' accuracy.
        String inputNumber="1";
        String ctxt_string = SparkFHE.getInstance().encrypt(inputNumber);
        String ptxt_string = SparkFHE.getInstance().decrypt(ctxt_string);

        // Printing out the result
        System.out.println("InputNumber="+inputNumber + ", result of dec(enc(InputNumber))="+ptxt_string);
    }


}
