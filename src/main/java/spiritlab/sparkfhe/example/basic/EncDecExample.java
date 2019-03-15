//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.spiritlab.sparkfhe.SparkFHESetup;
import org.apache.spark.sql.SparkSession;
import spiritlab.sparkfhe.api.*;
import spiritlab.sparkfhe.example.Config;

/**
 * This is an example for SparkFHE project. Created to test the functionality
 * of the encryption and decryption features.
 */

import java.io.File;
import java.util.Vector;

public class EncDecExample {


    public static void main(String args[]) {
        int slices = 2;
        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("EncDecExample");

        if ( "local".equalsIgnoreCase(args[0]) ) {
            sparkConf.setMaster("local");
        } else {
            slices=Integer.parseInt(args[0]);
            Config.update_current_directory(sparkConf.get("spark.mesos.executor.home"));
            System.out.println("CURRENT_DIRECTORY = "+Config.get_current_directory());

        }
      
        String pk = args[1];
        String sk = args[2];

        // required to load our shared library
        SparkFHESetup.setup();
        // create SparkFHE object
        SparkFHE.init(FHELibrary.HELIB, pk, sk);

        new File(Config.get_records_directory()).mkdirs();
        String CTXT_0_FILE = Config.get_records_directory() + "/ptxt_long_0_"+ SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json";
        String CTXT_1_FILE = Config.get_records_directory() +"/ptxt_long_1_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json";

        // initialize a literal 1, encrypt it and decrypted it to verify the cryptography functions
        String inputNumberString="1";
        Plaintext inputNumberPtxt = new Plaintext(inputNumberString);
        Ciphertext inputNumberCtxt = SparkFHE.getInstance().encrypt(inputNumberPtxt);
        Plaintext inputNumberPtxt_returned = SparkFHE.getInstance().decrypt(inputNumberCtxt);
        System.out.println("InputNumber="+inputNumberString + ", result of dec(enc(InputNumber))="+inputNumberPtxt_returned);


        // store the cipher text to the pre-defined file location
        for (int l=0;l<2;l++) {
            System.out.println("Storing ciphertext to "+Config.get_records_directory()+"/ptxt_long_"+String.valueOf(l)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json");
            DataSourceHandler.storeCtxt(SparkFHE.getInstance().encrypt(new Plaintext(l)).toString(), Config.get_records_directory()+"/ptxt_long_"+String.valueOf(l)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json");
        }

        String ctxt_0_string, ctxt_1_string;

        // read in the cipher text from file and store them as Strings
        ctxt_0_string = DataSourceHandler.loadCtxt(CTXT_0_FILE);
        ctxt_1_string = DataSourceHandler.loadCtxt(CTXT_1_FILE);

        Ciphertext ctxtresult;
        // perform homomorphic addition on the cipertext
        ctxtresult = SparkFHE.getInstance().do_FHE_basic_op(new Ciphertext(ctxt_0_string), new Ciphertext(ctxt_1_string), SparkFHE.FHE_ADD);
        // decrypt the result and display it
        System.out.println("0+1="+SparkFHE.getInstance().decrypt(ctxtresult));


        /* generating vectors of ctxt */
        int ptxtMod_half = 10;
        PlaintextVector vec_ptxt_1 = new PlaintextVector();
        PlaintextVector vec_ptxt_2 = new PlaintextVector();
        CiphertextVector vec_ctxt_1 = new CiphertextVector();
        CiphertextVector vec_ctxt_2 = new CiphertextVector();

        // create 2 StringVectors
        for (int i = 0; i < Config.NUM_OF_VECTOR_ELEMENTS; ++i) {
            vec_ptxt_1.add(new Plaintext(i%ptxtMod_half));
            vec_ptxt_2.add(new Plaintext((Config.NUM_OF_VECTOR_ELEMENTS-1-i)%ptxtMod_half));
        }



        // encrypt them and store to pre-defined location
        vec_ctxt_1=SparkFHE.getInstance().encrypt(vec_ptxt_1);
        SparkFHE.getInstance().store_ciphertexts_to_file(Config.CTXT_LABEL, vec_ctxt_1, Config.get_records_directory()+"/vec_a_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json");

        // encrypt them and store to pre-defined location
        vec_ctxt_2=SparkFHE.getInstance().encrypt(vec_ptxt_2);
        SparkFHE.getInstance().store_ciphertexts_to_file(Config.CTXT_LABEL, vec_ctxt_2, Config.get_records_directory()+"/vec_b_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json");

    }


}
