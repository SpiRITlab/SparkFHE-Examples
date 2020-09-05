//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.nonbatching;

import org.apache.spark.spiritlab.sparkfhe.SparkFHEPlugin;
import spiritlab.sparkfhe.api.*;
import spiritlab.sparkfhe.example.Config;

/**
 * This is an example for SparkFHE project. Created to test the functionality
 * of the encryption and decryption features.
 */

import java.io.File;

public class EncDecExample {

    private static void encrypt_data(){
        // store the ciphertexts to the pre-defined file location
        for (int l=0; l<2; l++) {
            System.out.println("Storing ciphertext to "+Config.get_records_directory()+"/ptxt_long_"+String.valueOf(l)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl");
            SparkFHE.getInstance().store_ciphertext_to_file(
                    Config.Ciphertext_Label,
                    SparkFHE.getInstance().encrypt(new Plaintext(l)).toString(),
                    Config.get_records_directory()+"/ptxt_long_"+String.valueOf(l)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl");
        }
    }

    private static void encrypt_vector(){
        /* generating vectors of ctxt */
        int ptxtMod_half = 10;
        StringVector vec_ptxt_1 = new StringVector();
        StringVector vec_ptxt_2 = new StringVector();
        StringVector vec_ctxt_1 = new StringVector();
        StringVector vec_ctxt_2 = new StringVector();

        // create 2 StringVectors
        for (int i = 0; i < Config.NUM_OF_VECTOR_ELEMENTS; ++i) {
            vec_ptxt_1.add(String.valueOf(i%ptxtMod_half));
            vec_ptxt_2.add(String.valueOf((Config.NUM_OF_VECTOR_ELEMENTS-1-i)%ptxtMod_half));
        }

        // encrypt them and store to pre-defined location
        vec_ctxt_1=SparkFHE.getInstance().encrypt(vec_ptxt_1);
        SparkFHE.getInstance().store_ciphertexts_to_file(Config.Ciphertext_Label, vec_ctxt_1, Config.get_records_directory()+"/vec_a_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl");

        // encrypt them and store to pre-defined location
        vec_ctxt_2=SparkFHE.getInstance().encrypt(vec_ptxt_2);
        SparkFHE.getInstance().store_ciphertexts_to_file(Config.Ciphertext_Label, vec_ctxt_2, Config.get_records_directory()+"/vec_b_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl");


        // Generate a vector of 100 elements (for total sum)
        StringVector ptxt_vec = new StringVector();
        StringVector ctxt_vec = new StringVector();
        for (int i = 1; i <= 100; i++){
            ptxt_vec.add(String.valueOf(i));
        }

        ctxt_vec = SparkFHE.getInstance().encrypt(ptxt_vec);
        SparkFHE.getInstance().store_ciphertexts_to_file(Config.Ciphertext_Label, ctxt_vec, Config.get_records_directory()+"/vec_ctxt_"+String.valueOf(100)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+".jsonl");
    }

    public static void main(String args[]) {
        String scheme="", library = "", pk="", sk="";

        Config.setExecutionEnvironment(args[0]);
        switch (Config.currentExecutionEnvironment) {
            case CLUSTER:
                Config.set_HDFS_NAME_NODE(args[1]);
                library = args[2];
                scheme = args[3];
                pk = args[4];
                sk = args[5];
                break;
            case LOCAL:
                library = args[1];
                scheme = args[2];
                pk = args[3];
                sk = args[4];
                break;
            default:
                break;
        }
        System.out.println("CURRENT_DIRECTORY = "+Config.get_current_directory());

        // required to load our shared library
        SparkFHEPlugin.setup();
        // create SparkFHE object
        SparkFHE.init(library, scheme, pk, sk);

        new File(Config.get_records_directory()).mkdirs();

        // generate encrypted digits and vectors required for the nonbatching examples
        encrypt_data();
        encrypt_vector();

        String CTXT_0_FILE = Config.get_records_directory() + "/ptxt_long_0_"+ SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";
        String CTXT_1_FILE = Config.get_records_directory() +"/ptxt_long_1_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";

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
        Ciphertext ctxtresult = new Ciphertext(SparkFHE.getInstance().fhe_add(ctxt_0_string, ctxt_1_string));
        // decrypt the result and display it
        System.out.println("0+1="+SparkFHE.getInstance().decrypt(ctxtresult, true).toString());
    }
}
