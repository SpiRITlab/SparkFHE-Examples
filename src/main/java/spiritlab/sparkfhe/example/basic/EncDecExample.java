package spiritlab.sparkfhe.example.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.spiritlab.sparkfhe.SparkFHESetup;
import org.apache.spark.sql.SparkSession;
import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.api.Ciphertext;
import spiritlab.sparkfhe.api.StringVector;
import spiritlab.sparkfhe.example.Config;

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

        String inputNumber="1";
        String ctxt_string = SparkFHE.getInstance().encrypt(inputNumber);
        String ptxt_string = SparkFHE.getInstance().decrypt(ctxt_string);
        System.out.println("InputNumber="+inputNumber + ", result of dec(enc(InputNumber))="+ptxt_string);

        for (long l=0;l<2;l++) {
            System.out.println("Storing ciphertext to "+Config.get_records_directory()+"/ptxt_long_"+String.valueOf(l)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json");
            Ciphertext.storeCtxt(SparkFHE.getInstance().encrypt(String.valueOf(l)), Config.get_records_directory()+"/ptxt_long_"+String.valueOf(l)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json");
        }

        String ctxt_0_string, ctxt_1_string, ctxtresult;

        ctxt_0_string = Ciphertext.loadCtxt(CTXT_0_FILE);
        ctxt_1_string = Ciphertext.loadCtxt(CTXT_1_FILE);

        ctxtresult = SparkFHE.getInstance().do_FHE_basic_op(ctxt_0_string, ctxt_1_string, SparkFHE.FHE_ADD);
        System.out.println("0+1="+SparkFHE.getInstance().decrypt(ctxtresult));


        /* generating vectors of ctxt */
        long ptxtMod_half = 10;
        StringVector vec_ptxt_string_1 = new StringVector();
        StringVector vec_ptxt_string_2 = new StringVector();
        StringVector vec_ctxt_string_1 = new StringVector();
        StringVector vec_ctxt_string_2 = new StringVector();

        for (int i = 0; i < Config.NUM_OF_VECTOR_ELEMENTS; ++i) {
            vec_ptxt_string_1.add(String.valueOf(i%ptxtMod_half));
            vec_ptxt_string_2.add(String.valueOf((Config.NUM_OF_VECTOR_ELEMENTS-1-i)%ptxtMod_half));
        }

        vec_ctxt_string_1=SparkFHE.getInstance().encrypt(vec_ptxt_string_1);
        SparkFHE.store_ciphertexts_to_file(Config.CTXT_LABEL, vec_ctxt_string_1, Config.get_records_directory()+"/vec_a_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json");

        vec_ctxt_string_2=SparkFHE.getInstance().encrypt(vec_ptxt_string_2);
        SparkFHE.store_ciphertexts_to_file(Config.CTXT_LABEL, vec_ctxt_string_2, Config.get_records_directory()+"/vec_b_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json");

    }


}
