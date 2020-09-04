//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.batching;

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
            System.out.println("Storing ciphertext to "+Config.get_records_directory()+"/packed_ptxt_long_"+String.valueOf(l)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl");
            SparkFHE.getInstance().store_ciphertext_to_file(
                    Config.Ciphertext_Label,
                    SparkFHE.getInstance().encrypt(SparkFHE.getInstance().encode(String.valueOf(l))).toString(),
                    Config.get_records_directory()+"/packed_ptxt_long_"+String.valueOf(l)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl");
        }
    }

    private static void encrypt_vector(String scheme){
        /* generating vectors of ctxt */
        Plaintext ptxt_1, ptxt_2;
        int ptxtMod_half = 10;
        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleVector input_vec_1 = new DoubleVector();
            DoubleVector input_vec_2 = new DoubleVector();
            // create 2 StringVectors
            for (double i = 0; i < Config.NUM_OF_VECTOR_ELEMENTS; ++i) {
                input_vec_1.add(i%ptxtMod_half);
                input_vec_2.add((Config.NUM_OF_VECTOR_ELEMENTS-1-i)%ptxtMod_half);
            }
            ptxt_1 = SparkFHE.getInstance().encode(input_vec_1);
            ptxt_2 = SparkFHE.getInstance().encode(input_vec_2);
        } else { // BGV or BFV
            LongVector input_vec_1 = new LongVector();
            LongVector input_vec_2 = new LongVector();
            // create 2 StringVectors
            for (int i = 0; i < Config.NUM_OF_VECTOR_ELEMENTS; ++i) {
                input_vec_1.add(i%ptxtMod_half);
                input_vec_2.add((Config.NUM_OF_VECTOR_ELEMENTS-1-i)%ptxtMod_half);
            }
            ptxt_1 = SparkFHE.getInstance().encode(input_vec_1);
            ptxt_2 = SparkFHE.getInstance().encode(input_vec_2);
        }

        // encrypt them and store to pre-defined location
        Ciphertext ctxt_1 = SparkFHE.getInstance().encrypt(ptxt_1);
        SparkFHE.getInstance().store_ciphertext_to_file(Config.Ciphertext_Label, ctxt_1.toString(), Config.get_records_directory()+"/packed_ctxt_a_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl");

        // encrypt them and store to pre-defined location
        Ciphertext ctxt_2 = SparkFHE.getInstance().encrypt(ptxt_2);
        SparkFHE.getInstance().store_ciphertext_to_file(Config.Ciphertext_Label, ctxt_2.toString(), Config.get_records_directory()+"/packed_ctxt_b_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl");

        // Generate a vector of 100 elements (for total sum)
        Plaintext ptxt;
        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleVector input_vec = new DoubleVector();
            for (double i = 1; i <= 100; i++){
                input_vec.add(1.0);
            }
            ptxt = SparkFHE.getInstance().encode(input_vec);
        } else { // BGV or BFV
            LongVector input_vec = new LongVector();
            for (int i = 1; i <= 100; i++){
                input_vec.add(1);
            }
            ptxt = SparkFHE.getInstance().encode(input_vec);
        }
        Ciphertext ctxt = SparkFHE.getInstance().encrypt(ptxt);
        SparkFHE.getInstance().store_ciphertext_to_file(Config.Ciphertext_Label, ctxt.toString(), Config.get_records_directory()+"/packed_ctxt_"+String.valueOf(100)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+".jsonl");
    }

    private static void encrypt_matrix(String scheme){
        /* generating two matrices of ctxt of size 10x10 */
        PlaintextVector ptxt_mat_1 = new PlaintextVector();
        PlaintextVector ptxt_mat_2 = new PlaintextVector();

        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleMatrix input_mat_1 = new DoubleMatrix();
            DoubleMatrix input_mat_2 = new DoubleMatrix();
            for (int i = 0; i < 10; i++){
                DoubleVector input_vec_1 = new DoubleVector();
                DoubleVector input_vec_2 = new DoubleVector();
                for (double j = 0; j < 10; j++){
                    input_vec_1.add(1.0);
                    input_vec_2.add(0.0);
                }
                input_mat_1.add(input_vec_1);
                input_mat_2.add(input_vec_2);
            }
            ptxt_mat_1 = SparkFHE.getInstance().encode_many(input_mat_1);
            ptxt_mat_2 = SparkFHE.getInstance().encode_many(input_mat_2);
        } else { // BGV or BFV
            LongMatrix input_mat_1 = new LongMatrix();
            LongMatrix input_mat_2 = new LongMatrix();
            for (int i = 0; i < 10; i++){
                LongVector input_vec_1 = new LongVector();
                LongVector input_vec_2 = new LongVector();
                for (int j = 0; j < 10; j++){
                    input_vec_1.add(1);
                    input_vec_2.add(0);
                }
                input_mat_1.add(input_vec_1);
                input_mat_2.add(input_vec_2);
            }
            ptxt_mat_1 = SparkFHE.getInstance().encode_many(input_mat_1);
            ptxt_mat_2 = SparkFHE.getInstance().encode_many(input_mat_2);
        }

        for (int i = 0; i < ptxt_mat_1.size(); i++){
            Ciphertext ctxt_mat_1 = SparkFHE.getInstance().encrypt(ptxt_mat_1.get(i));
            SparkFHE.getInstance().store_ciphertext_to_file(Config.Ciphertext_Label, ctxt_mat_1.toString(), Config.get_records_directory()+"/packed_matrix_a_"+String.valueOf(10*10)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+".jsonl");

            Ciphertext ctxt_mat_2 = SparkFHE.getInstance().encrypt(ptxt_mat_2.get(i));
            SparkFHE.getInstance().store_ciphertext_to_file(Config.Ciphertext_Label, ctxt_mat_2.toString(), Config.get_records_directory()+"/packed_matrix_b_"+String.valueOf(10*10)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+".jsonl");
        }
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

        // generate encrypted digits, vectors, and matrcies required for the batching examples
        encrypt_data();
        encrypt_vector(scheme);
        encrypt_matrix(scheme);

        String CTXT_0_FILE = Config.get_records_directory() + "/packed_ptxt_long_0_"+ SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";
        String CTXT_1_FILE = Config.get_records_directory() +"/packed_ptxt_long_1_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";

        // initialize a literal 1, encrypt it and decrypted it to verify the cryptography functions
        String inputNumberString="1";
        Plaintext inputNumberPtxt = SparkFHE.getInstance().encode(inputNumberString);
        Ciphertext inputNumberCtxt = SparkFHE.getInstance().encrypt(inputNumberPtxt);
        Plaintext inputNumberPtxt_returned = SparkFHE.getInstance().decrypt(inputNumberCtxt);
        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleVector outputNumberPtxt = new DoubleVector();
            SparkFHE.getInstance().decode(outputNumberPtxt, inputNumberPtxt_returned);
            System.out.println("InputNumber=" + inputNumberString + ", result of dec(enc(InputNumber))=" + String.valueOf(outputNumberPtxt.get(0)));
        } else {
            LongVector outputNumberPtxt = new LongVector();
            SparkFHE.getInstance().decode(outputNumberPtxt, inputNumberPtxt_returned);
            System.out.println("InputNumber=" + inputNumberString + ", result of dec(enc(InputNumber))=" + String.valueOf(outputNumberPtxt.get(0)));
        }

        // read in the cipher text from file and store them as Strings
        String ctxt_0_string = SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label, CTXT_0_FILE);
        String ctxt_1_string = SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label, CTXT_1_FILE);

        // perform homomorphic addition on the ciphertext
        Ciphertext ctxtresult = new Ciphertext(SparkFHE.getInstance().fhe_add(ctxt_0_string, ctxt_1_string));
        // decrypt the result and display it
        Util.decrypt_and_print(scheme, "0 + 1", ctxtresult );
    }
}
