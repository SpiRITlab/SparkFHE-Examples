//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.packing;

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

    private static String CTXT_Matrix_a_FILE;
    private static String CTXT_Matrix_b_FILE;

    public static void encrypt_data() {
        // Generate two ciphertexts and store them to the pre-defined file location
        System.out.println("Storing ciphertext to "+CTXT_0_FILE);
        SparkFHE.getInstance().store_ciphertext_to_file( Config.Ciphertext_Label, SparkFHE.getInstance().encrypt(new Plaintext(String.valueOf(0))).toString(), CTXT_0_FILE);

        System.out.println("Storing ciphertext to "+CTXT_1_FILE);
        SparkFHE.getInstance().store_ciphertext_to_file( Config.Ciphertext_Label, SparkFHE.getInstance().encrypt(new Plaintext(String.valueOf(0))).toString(), CTXT_1_FILE);
    }

    public static void encrypt_vector(String scheme, long row) {
        // Generate two vectors of size (row)
        PlaintextVector ptxt_vec_1, ptxt_vec_2;
        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleVector input_vec_1 = new DoubleVector();
            DoubleVector input_vec_2 = new DoubleVector();
            // create 2 StringVectors
            for (int i = 0; i < row; ++i) {
                input_vec_1.add(Double.valueOf(Util.getRandom(10)));
                input_vec_2.add(Double.valueOf(Util.getRandom(10)));
            }
            ptxt_vec_1 = SparkFHE.getInstance().encode_many(input_vec_1);
            ptxt_vec_2 = SparkFHE.getInstance().encode_many(input_vec_2);
        } else { // BGV or BFV
            LongVector input_vec_1 = new LongVector();
            LongVector input_vec_2 = new LongVector();
            // create 2 StringVectors
            for (int i = 0; i < row; ++i) {
                input_vec_1.add(Util.getRandom(10));
                input_vec_2.add(Util.getRandom(10));
            }
            ptxt_vec_1 = SparkFHE.getInstance().encode_many(input_vec_1);
            ptxt_vec_2 = SparkFHE.getInstance().encode_many(input_vec_2);
        }

        // encrypt first vector and store to pre-defined location
        CiphertextVector ctxt_vec_1 = SparkFHE.getInstance().encrypt(ptxt_vec_1);
        SparkFHE.getInstance().store_ciphertexts_to_file(Config.Ciphertext_Label, ctxt_vec_1, CTXT_Vector_a_FILE);

        // encrypt second vector and store to pre-defined location
        CiphertextVector ctxt_vec_2 = SparkFHE.getInstance().encrypt(ptxt_vec_2);
        SparkFHE.getInstance().store_ciphertexts_to_file(Config.Ciphertext_Label, ctxt_vec_2, CTXT_Vector_b_FILE);
    }

    public static void encrypt_matrix(String scheme, long row, long col) {
        // Generate two matrices of size (row x col)
        PlaintextVector ptxt_mat_1, ptxt_mat_2;
        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleMatrix input_mat_1 = new DoubleMatrix();
            DoubleMatrix input_mat_2 = new DoubleMatrix();
            for (int i = 0; i < row; i++){
                DoubleVector input_vec_1 = new DoubleVector();
                DoubleVector input_vec_2 = new DoubleVector();
                for (int j = 0; j < col; j++){
                    input_vec_1.add(Double.valueOf(Util.getRandom(10)));
                    input_vec_2.add(Double.valueOf(Util.getRandom(10)));
                }
                input_mat_1.add(input_vec_1);
                input_mat_2.add(input_vec_2);
            }
            ptxt_mat_1 = SparkFHE.getInstance().encode_many(input_mat_1);
            ptxt_mat_2 = SparkFHE.getInstance().encode_many(input_mat_2);
        } else { // BGV or BFV
            LongMatrix input_mat_1 = new LongMatrix();
            LongMatrix input_mat_2 = new LongMatrix();
            for (int i = 0; i < row; i++){
                LongVector input_vec_1 = new LongVector();
                LongVector input_vec_2 = new LongVector();
                for (int j = 0; j < col; j++){
                    input_vec_1.add(Util.getRandom(10));
                    input_vec_2.add(Util.getRandom(10));
                }
                input_mat_1.add(input_vec_1);
                input_mat_2.add(input_vec_2);
            }
            ptxt_mat_1 = SparkFHE.getInstance().encode_many(input_mat_1);
            ptxt_mat_2 = SparkFHE.getInstance().encode_many(input_mat_2);
        }

        CiphertextVector ctxt_mat_1 = SparkFHE.getInstance().encrypt(ptxt_mat_1);
        SparkFHE.getInstance().store_ciphertexts_to_file(Config.Ciphertext_Label, ctxt_mat_1, CTXT_Matrix_a_FILE);

        CiphertextVector ctxt_mat_2 = SparkFHE.getInstance().encrypt(ptxt_mat_2);
        SparkFHE.getInstance().store_ciphertexts_to_file(Config.Ciphertext_Label, ctxt_mat_2, CTXT_Matrix_b_FILE);
    }

    public static void main(String args[]) {
        String scheme="", library = "", pk="", sk="", rlk="", glk="";
        // Row, col are the dimensions of matrix.
        // If row = 1, it represents a column-vector. If col = 1, it represents a row-vector.
        long row = 100, col = 1;

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
                    row = Long.valueOf(args[8]);
                    col = Long.valueOf(args[9]);
                } else {
                    row = Long.valueOf(args[6]);
                    col = Long.valueOf(args[7]);
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
                    row = Long.valueOf(args[7]);
                    col = Long.valueOf(args[8]);
                } else {
                    row = Long.valueOf(args[5]);
                    col = Long.valueOf(args[6]);
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
        CTXT_0_FILE = Config.get_records_directory() + "/packed_ctxt_long_0_"+ SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";
        CTXT_1_FILE = Config.get_records_directory() +"/packed_ctxt_long_1_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";

        CTXT_Vector_a_FILE = Config.get_records_directory()+"/packed_vec_a_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";
        CTXT_Vector_b_FILE = Config.get_records_directory()+"/packed_vec_b_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";

        CTXT_Matrix_a_FILE = Config.get_records_directory()+"/packed_matrix_a_"+SparkFHE.getInstance().generate_crypto_params_suffix()+".jsonl";
        CTXT_Matrix_b_FILE = Config.get_records_directory()+"/packed_matrix_b_"+SparkFHE.getInstance().generate_crypto_params_suffix()+".jsonl";

        /* generating individual ctxts */
        encrypt_data();

        /* generating vectors of ctxt */
        encrypt_vector(scheme, row);

        /* generating matrices of ctxt */
        encrypt_matrix(scheme, row, col);

        /* testing encode/enc/dec/decode operations (toy example for debugging purposes) */
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
        Ciphertext ctxtresult = new Ciphertext(SparkFHE.getInstance().do_FHE_basic_op(ctxt_0_string, ctxt_1_string, SparkFHE.FHE_ADD));

        // decrypt the result and display it
        Util.decrypt_and_print(scheme, "0 + 1", ctxtresult );
    }
}
