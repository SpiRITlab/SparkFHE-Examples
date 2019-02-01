package spiritlab.sparkfhe.example.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.spiritlab.sparkfhe.SparkFHESetup;
import org.apache.spark.sql.SparkSession;
import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.example.Config;

import java.io.File;

public class KeyGenExample {

    public static void main(String args[]) {
        int slices = 2;
        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("KeyGenExample");

        if ( "local".equalsIgnoreCase(args[0]) ) {
            sparkConf.setMaster("local");
        } else {
            slices=Integer.parseInt(args[0]);
            Config.update_current_directory(sparkConf.get("spark.mesos.executor.home"));
            System.out.println("CURRENT_DIRECTORY = "+Config.get_current_directory());
        }

        // required to load our shared library
        SparkFHESetup.setup();
        // create SparkFHE object
        SparkFHE.init(FHELibrary.HELIB);

        new File(Config.get_keys_directory()).mkdirs();
        SparkFHE.getInstance().generate_key_pair(Config.get_default_crypto_params_file(FHELibrary.HELIB), Config.get_default_public_key_file(), Config.get_default_secret_key_file());

        String inputNumber="1";
        String ctxt_string = SparkFHE.getInstance().encrypt(inputNumber);
        String ptxt_string = SparkFHE.getInstance().decrypt(ctxt_string);
        System.out.println("InputNumber="+inputNumber + ", result of dec(enc(InputNumber))="+ptxt_string);
    }


}
