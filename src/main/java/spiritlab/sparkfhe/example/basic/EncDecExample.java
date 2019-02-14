package spiritlab.sparkfhe.example.basic;

import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.api.Ciphertext;
import spiritlab.sparkfhe.api.StringVector;
import spiritlab.sparkfhe.example.Config;

/**
 * This is an example for SparkFHE project. Created to test the functionality
 * of the encryption and decryption features.
 */

import java.io.File;
import java.util.Vector;

public class EncDecExample {

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

        // Create SparkFHE object with HElib, a library that implements homomorphic encryption
        SparkFHE.init(FHELibrary.HELIB, Config.DEFAULT_PUBLIC_KEY_FILE, Config.DEFAULT_SECRET_KEY_FILE);

        // Creates the directory named by the pathname - current_directiory/gen/keys,
        // and including any necessary parent directories.
        new File(Config.DEFAULT_RECORDS_DIRECTORY).mkdirs();

        String CTXT_0_FILE = Config.DEFAULT_RECORDS_DIRECTORY+"/ptxt_long_0_"+ SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json";
        String CTXT_1_FILE = Config.DEFAULT_RECORDS_DIRECTORY+"/ptxt_long_1_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json";

        // initialize a literal 1, encrypt it and decrypted it to verify the cryptography functions
        String inputNumber="1";
        String ctxt_string = SparkFHE.getInstance().encrypt(inputNumber);
        String ptxt_string = SparkFHE.getInstance().decrypt(ctxt_string);
        System.out.println("InputNumber="+inputNumber + ", result of dec(enc(InputNumber))="+ptxt_string);

        // store the cipher text to the pre-defined file location
        for (long l=0;l<2;l++) {
            Ciphertext.storeCtxt(SparkFHE.getInstance().encrypt(String.valueOf(l)), Config.DEFAULT_RECORDS_DIRECTORY+"/ptxt_long_"+String.valueOf(l)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json");
        }

        String ctxt_0_string, ctxt_1_string, ctxtresult;

        // read in the cipher text from file and store them as Strings
        ctxt_0_string = Ciphertext.loadCtxt(CTXT_0_FILE);
        ctxt_1_string = Ciphertext.loadCtxt(CTXT_1_FILE);

        // perform homomorphic addition on the cipertext
        ctxtresult = SparkFHE.getInstance().do_FHE_basic_op(ctxt_0_string, ctxt_1_string, SparkFHE.FHE_ADD);
        // decrypt the result and display it
        System.out.println("0+1="+SparkFHE.getInstance().decrypt(ctxtresult));


        /* generating vectors of ctxt */
        long ptxtMod_half = 10;
        StringVector vec_ptxt_string_1 = new StringVector();
        StringVector vec_ptxt_string_2 = new StringVector();
        StringVector vec_ctxt_string_1 = new StringVector();
        StringVector vec_ctxt_string_2 = new StringVector();

        // create 2 StringVectors
        for (int i = 0; i < Config.NUM_OF_VECTOR_ELEMENTS; ++i) {
            vec_ptxt_string_1.add(String.valueOf(i%ptxtMod_half));
            vec_ptxt_string_2.add(String.valueOf((Config.NUM_OF_VECTOR_ELEMENTS-1-i)%ptxtMod_half));
        }

        // encrypt them and store to pre-defined location
        vec_ctxt_string_1=SparkFHE.getInstance().encrypt(vec_ptxt_string_1);
        SparkFHE.store_ciphertexts_to_file(Config.CTXT_LABEL, vec_ctxt_string_1, Config.DEFAULT_RECORDS_DIRECTORY+"/vec_a_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json");

        // encrypt them and store to pre-defined location
        vec_ctxt_string_2=SparkFHE.getInstance().encrypt(vec_ptxt_string_2);
        SparkFHE.store_ciphertexts_to_file(Config.CTXT_LABEL, vec_ctxt_string_2, Config.DEFAULT_RECORDS_DIRECTORY+"/vec_b_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json");

    }


}
