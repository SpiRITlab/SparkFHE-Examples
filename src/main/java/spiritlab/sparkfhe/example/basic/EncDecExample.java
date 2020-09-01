//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.spiritlab.sparkfhe.SparkFHEPlugin;
import spiritlab.sparkfhe.api.*;
import spiritlab.sparkfhe.example.Config;
import spiritlab.sparkfhe.example.Util;

/**
 * This is an example for SparkFHE project. Created to test the functionality
 * of the encryption and decryption features.
 */

import java.io.File;
import java.util.Random;

public class EncDecExample {

    private static String CTXT_0_FILE;
    private static String CTXT_1_FILE;

    private static String CTXT_Vector_a_FILE;
    private static String CTXT_Vector_b_FILE;

    public static void encrypt_data() {
        // Generate two ciphertexts and store them to the pre-defined file location
        System.out.println("Storing ciphertext to "+CTXT_0_FILE);
        SparkFHE.getInstance().store_ciphertext_to_file( Config.Ciphertext_Label, SparkFHE.getInstance().encrypt(new Plaintext(String.valueOf(0))).toString(), CTXT_0_FILE);

        System.out.println("Storing ciphertext to "+CTXT_1_FILE);
        SparkFHE.getInstance().store_ciphertext_to_file( Config.Ciphertext_Label, SparkFHE.getInstance().encrypt(new Plaintext(String.valueOf(1))).toString(), CTXT_1_FILE);
    }

    public static void encrypt_vector(long vecSize) {
        /* generating vectors of ctxt */
        StringVector vec_ptxt_1 = new StringVector();
        StringVector vec_ptxt_2 = new StringVector();

        // create 2 StringVectors
        for (int i = 0; i < vecSize; ++i) {
            vec_ptxt_1.add(String.valueOf(Util.getRandom(10)));
            vec_ptxt_2.add(String.valueOf(Util.getRandom(10)));
        }

        // encrypt them and store to pre-defined location
        StringVector vec_ctxt_1 = SparkFHE.getInstance().encrypt(vec_ptxt_1);
        SparkFHE.getInstance().store_ciphertexts_to_file(Config.Ciphertext_Label, vec_ctxt_1, CTXT_Vector_a_FILE);

        // encrypt them and store to pre-defined location
        StringVector vec_ctxt_2=SparkFHE.getInstance().encrypt(vec_ptxt_2);
        SparkFHE.getInstance().store_ciphertexts_to_file(Config.Ciphertext_Label, vec_ctxt_2, CTXT_Vector_b_FILE);
    }

    public static void main(String args[]) {
        String scheme="", library = "", pk="", sk="", rlk="", glk="";
        long vecSize = 100;

        Config.setExecutionEnvironment(args[0]);
        switch (Config.currentExecutionEnvironment) {
            case CLUSTER:
                Config.set_HDFS_NAME_NODE(args[1]);
                library = args[2];
                scheme = args[3];
                pk = args[4];
                sk = args[5];
                if (library.equalsIgnoreCase(FHELibrary.SEAL)) {
                    rlk = args[6];
                    glk = args[7];
                    vecSize = Long.valueOf(args[8]);
                } else {
                    vecSize = Long.valueOf(args[6]);
                }
                break;
            case LOCAL:
                library = args[1];
                scheme = args[2];
                pk = args[3];
                sk = args[4];
                if (library.equalsIgnoreCase(FHELibrary.SEAL)) {
                    rlk = args[5];
                    glk = args[6];
                    vecSize = Long.valueOf(args[7]);
                } else {
                    vecSize = Long.valueOf(args[5]);
                }
                break;
            default:
                break;
        }
        System.out.println("CURRENT_DIRECTORY = "+Config.get_current_directory());

        // required to load our shared library
        SparkFHEPlugin.setup();
        // create SparkFHE object
        SparkFHE.init(library, scheme, pk, sk, rlk, glk);

        new File(Config.get_records_directory()).mkdirs();

        // set ctxt file names
        CTXT_0_FILE = Config.get_records_directory() + "/ctxt_long_0_"+ SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";
        CTXT_1_FILE = Config.get_records_directory() +"/ctxt_long_1_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";

        CTXT_Vector_a_FILE = Config.get_records_directory()+"/ctxt_vec_a_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";
        CTXT_Vector_b_FILE = Config.get_records_directory()+"/ctxt_vec_b_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";

        /* generating individual ctxts */
        encrypt_data();

        /* generating vectors of ctxt of size vecSize  */
        encrypt_vector(vecSize);

        /* testing enc/dec operations (toy example for debugging purposes) */
        // initialize a literal 1, encrypt it and decrypted it to verify the cryptography functions
        String inputNumberString="1";
        Plaintext inputNumberPtxt = new Plaintext(inputNumberString);
        Ciphertext inputNumberCtxt = SparkFHE.getInstance().encrypt(inputNumberPtxt);
        Plaintext inputNumberPtxt_returned = SparkFHE.getInstance().decrypt(inputNumberCtxt, true);
        System.out.println("InputNumber="+inputNumberString + ", result of dec(enc(InputNumber))="+inputNumberPtxt_returned.toString());


        // read in the cipher text from file and store them as Strings
        String ctxt_0_string = SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label, CTXT_0_FILE);
        String ctxt_1_string = SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label, CTXT_1_FILE);

        // perform homomorphic addition on the cipertext
        Ciphertext ctxtresult = new Ciphertext(SparkFHE.getInstance().do_FHE_basic_op(ctxt_0_string, ctxt_1_string, SparkFHE.FHE_ADD));
        // decrypt the result and display it
        System.out.println("0+1="+SparkFHE.getInstance().decrypt(ctxtresult, true).toString());
    }
}
