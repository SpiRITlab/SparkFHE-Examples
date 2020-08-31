//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.spiritlab.sparkfhe.SparkFHEPlugin;
import org.apache.spark.sql.*;
import spiritlab.sparkfhe.api.*;
import spiritlab.sparkfhe.example.Config;

/**
 * This is an example for SparkFHE project. Created to test the functionality
 * of the underlying C++ APIs. A few simple functions are invoked via lambda.
 */
public class BasicOPsExample {

    private static String CTXT_0_FILE;
    private static String CTXT_1_FILE;

    private static String CTXT_Vector_a_FILE;
    private static String CTXT_Vector_b_FILE;

    public static void test_basic_op() {
        // Testing the addition function
        System.out.println("ADD(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.ADD));
        // Testing the multiplication function
        System.out.println("MUL(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.MUL));
        // Testing the substraction function
        System.out.println("SUB(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.SUB));
    }

    /**
     * This method performs the basic HE operations on ciphertexts and print out the results
     * @param spark spark session
     * @param slices the number of time a task is split up
     * @param pk_b broadcast variable for public key
     * @param sk_b broadcast variable for secret key
     * @param rlk_b broadcast variable for relin keys
     * @param glk_b boradcast variable for galois keys
     */
    public static void test_FHE_basic_op(SparkSession spark, int slices, String library, String scheme, Broadcast<String> pk_b,
                                         Broadcast<String> sk_b, Broadcast<String> rlk_b, Broadcast<String> glk_b) {
        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<SerializedCiphertextObject> ctxtJSONEncoder = Encoders.bean(SerializedCiphertextObject.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        // Create rdd with json file.
        JavaRDD<SerializedCiphertextObject> ctxt_zero_rdd = spark.read().json(CTXT_0_FILE).as(ctxtJSONEncoder).javaRDD();
//        System.out.println("Ciphertext Zero:"+SparkFHE.getInstance().decrypt(ctxt_zero_rdd.first().getCtxt(), true));

        JavaRDD<SerializedCiphertextObject> ctxt_one_rdd = spark.read().json(CTXT_1_FILE).as(ctxtJSONEncoder).javaRDD();
//        System.out.println("Ciphertext One:"+SparkFHE.getInstance().decrypt(ctxt_one_rdd.first().getCtxt(), true));

        // combine both rdds as a pair
        JavaPairRDD<SerializedCiphertextObject, SerializedCiphertextObject> Combined_ctxt_RDD = ctxt_one_rdd.zip(ctxt_zero_rdd).cache();

        // call homomorphic addition operators on the rdds
        JavaRDD<SerializedCiphertextObject> Addition_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
            SparkFHEPlugin.setup();
            SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());
            return new SerializedCiphertextObject(SparkFHE.getInstance().fhe_add(tuple._1().getCtxt(), tuple._2().getCtxt()));
        });
//        System.out.println("Homomorphic Addition:"+ SparkFHE.getInstance().decrypt(Addition_ctxt_RDD.first().getCtxt(), true));


        // call homomorphic multiply operators on the rdds
        JavaRDD<SerializedCiphertextObject> Multiplication_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
            SparkFHEPlugin.setup();
            SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());
            return new SerializedCiphertextObject(SparkFHE.getInstance().fhe_multiply(tuple._1().getCtxt(), tuple._2().getCtxt()));
        });
//        System.out.println("Homomorphic Multiplication:"+ SparkFHE.getInstance().decrypt(Multiplication_ctxt_RDD.first().getCtxt(), true));


        // call homomorphic subtraction operators on the rdds
        JavaRDD<SerializedCiphertextObject> Subtraction_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
            SparkFHEPlugin.setup();
            SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());
            return new SerializedCiphertextObject(SparkFHE.getInstance().fhe_subtract(tuple._1().getCtxt(), tuple._2().getCtxt()));
        });
//        System.out.println("Homomorphic Subtraction:"+SparkFHE.getInstance().decrypt(Subtraction_ctxt_RDD.first().getCtxt(), true));
    }

    /**
     * This method performs the basic HE operations on vectors and print out the results
     * @param spark spark session
     * @param slices the number of time a task is split up
     * @param pk_b broadcast variable for public key
     * @param sk_b broadcast variable for secret key
     * @param rlk_b broadcast variable for relin keys
     * @param glk_b boradcast variable for galois keys
     */
    public static void test_FHE_vector_op(SparkSession spark, int slices, String library, String scheme, Broadcast<String> pk_b,
                                          Broadcast<String> sk_b, Broadcast<String> rlk_b, Broadcast<String> glk_b) {
        /* Spark example for FHE calculations on vectors */
        // Encoders are created for Java beans
        Encoder<SerializedCiphertextObject> ctxtJSONEncoder = Encoders.bean(SerializedCiphertextObject.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        // Create rdd with json file. See http://jsonlines.org
        JavaRDD<SerializedCiphertextObject> ctxt_a_rdd = spark.read().json(CTXT_Vector_a_FILE).as(ctxtJSONEncoder).javaRDD();
        JavaRDD<SerializedCiphertextObject> ctxt_b_rdd = spark.read().json(CTXT_Vector_b_FILE).as(ctxtJSONEncoder).javaRDD();

        // combine both rdds as a pair
        JavaPairRDD<SerializedCiphertextObject, SerializedCiphertextObject> combined_ctxt_rdd = ctxt_a_rdd.zip(ctxt_b_rdd);
//        System.out.println("combined_ctxt_rdd.count() = " + combined_ctxt_rdd.count());

        // call homomorphic addition operators on the rdds
        JavaRDD<SerializedCiphertextObject> Addition_ctxt_RDD = combined_ctxt_rdd.map(tuple -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
            SparkFHEPlugin.setup();
            SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());
            return new SerializedCiphertextObject(SparkFHE.getInstance().fhe_add(tuple._1().getCtxt(), tuple._2().getCtxt()));
        });
//        System.out.println("Vector Homomorphic Addition");

        // call homomorphic multiply operators on the rdds
        JavaRDD<SerializedCiphertextObject> Multiplication_ctxt_RDD = combined_ctxt_rdd.map(tuple -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
            SparkFHEPlugin.setup();
            SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());
            return new SerializedCiphertextObject(SparkFHE.getInstance().fhe_multiply(tuple._1().getCtxt(), tuple._2().getCtxt()));
        });
//        System.out.println("Vector Homomorphic Multiplication");

        // call homomorphic subtraction operators on the rdds
        JavaRDD<SerializedCiphertextObject> Subtraction_ctxt_RDD = combined_ctxt_rdd.map(tuple -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
            SparkFHEPlugin.setup();
            SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());
            return new SerializedCiphertextObject(SparkFHE.getInstance().fhe_subtract(tuple._1().getCtxt(), tuple._2().getCtxt()));
        });
//        System.out.println("Vector Homomorphic Subtraction");
    }


    public static void main(String[] args) {
        String scheme="", library = "", pk="", sk="", rlk="", glk="";

        // The variable slices represent the number of time a task is split up
        int slices = 2;
        // Create a SparkConf that loads defaults from system properties and the classpath
        SparkConf sparkConf = new SparkConf();
        //Provides the Spark driver application a name for easy identification in the Spark or Yarn UI
        sparkConf.setAppName("BasicOPsExample");

	    // Decide whether to run the task locally or on the clusters
        Config.setExecutionEnvironment(args[0]);
        switch (Config.currentExecutionEnvironment) {
            case CLUSTER:
                slices = Integer.parseInt(args[0]);
                Config.set_HDFS_NAME_NODE(args[1]);
                library = args[2];
                scheme = args[3];
                pk = args[4];
                sk = args[5];
                if (library.equalsIgnoreCase(FHELibrary.SEAL)){
                    rlk = args[6];
                    glk = args[7];
                }
                break;
            case LOCAL:
                sparkConf.setMaster("local");
                library = args[1];
                scheme = args[2];
                pk = args[3];
                sk = args[4];
                if (library.equalsIgnoreCase(FHELibrary.SEAL)){
                    rlk = args[5];
                    glk = args[6];
                }
                break;
            default:
                break;
        }
        System.out.println("CURRENT_DIRECTORY = "+Config.get_current_directory());

        // Creating a session to Spark. The session allows the creation of the
        // various data abstractions such as RDDs, DataFrame, and more.
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();

        // Creating spark context which allows the communication with worker nodes
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        // Note, the following loading of shared library and init are done on driver only. We need to do the same on the executors.
        // Load C++ shared library
        SparkFHEPlugin.setup();
        // Create SparkFHE object with FHE library
        SparkFHE.init(library, scheme, pk, sk, rlk, glk);

        // set ctxt file names
        CTXT_0_FILE = Config.get_records_directory() + "/ctxt_long_0_"+ SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";
        CTXT_1_FILE = Config.get_records_directory() +"/ctxt_long_1_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";

        CTXT_Vector_a_FILE = Config.get_records_directory()+"/ctxt_vec_a_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";
        CTXT_Vector_b_FILE = Config.get_records_directory()+"/ctxt_vec_b_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";

        Broadcast<String> pk_b = jsc.broadcast(pk);
        Broadcast<String> sk_b = jsc.broadcast(sk);
        Broadcast<String> rlk_b = jsc.broadcast(rlk);
        Broadcast<String> glk_b = jsc.broadcast(glk);

        // Testing and printing the addition function
        System.out.println("testing 1+1=" +String.valueOf(SparkFHE.do_basic_op(1,1, SparkFHE.ADD)));

        // Start testing the basic operations in HE libraries on plain text, such as addition, subtraction, and multiply.
        test_basic_op();

        // String testing the basic operations in HE libraries on encrypted data, such as addition, subtraction, and multiply.
        test_FHE_basic_op(spark, slices, library, scheme, pk_b, sk_b, rlk_b, glk_b);

        // Testing the basic operations in HE libraries on encrypted vectors, such as addition, subtraction, and multiply.
        test_FHE_vector_op(spark, slices, library, scheme, pk_b, sk_b, rlk_b, glk_b);

        // Stop existing spark context
        jsc.close();

        // Stop existing spark session
        spark.close();
    }

}
