package spiritlab.sparkfhe.example.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import org.apache.spark.spiritlab.sparkfhe.SparkFHESetup;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.CtxtString;
import spiritlab.sparkfhe.example.Config;

/**
 * This is an example for SparkFHE project. Created to test the functionality
 * of the underlying C++ APIs. A few simple functions are invoked via lambda.
 */
public class BasicOPsExample {

    private static String CTXT_0_FILE;
    private static String CTXT_1_FILE;


    public static void test_basic_op() {
        // Testing the addition function
        System.out.println("ADD(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.ADD));
        // Testing the multiplication function
        System.out.println("MUL(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.MUL));
        // Testing the substraction function
        System.out.println("SUB(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.SUB));
    }

    public static void test_FHE_basic_op(SparkSession spark, int slices) {
        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<CtxtString> ctxtJSONEncoder = Encoders.bean(CtxtString.class);  // ???

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        // Create dataset with json file.
        // if CtxtString a row? Dataset<Row> is the Dataframe in Java
        Dataset<CtxtString> ctxt_zero_ds = spark.read().json(CTXT_0_FILE).as(ctxtJSONEncoder); // json is okay, but the best, vector cannot handle// ParK
        System.out.println("Ciphertext Zero:"+SparkFHE.getInstance().decrypt(ctxt_zero_ds.first().getCtxt()));
        Dataset<CtxtString> ctxt_one_ds = spark.read().json(CTXT_1_FILE).as(ctxtJSONEncoder);
        System.out.println("Ciphertext One:"+SparkFHE.getInstance().decrypt(ctxt_one_ds.first().getCtxt()));

        // Represents the content of the DataFrame as an RDD of Rows
        JavaRDD<CtxtString> ctxt_zero_rdd = ctxt_zero_ds.javaRDD();
        JavaRDD<CtxtString> ctxt_one_rdd = ctxt_one_ds.javaRDD();

        // combine both rdds as a pair
        JavaPairRDD<CtxtString, CtxtString> Combined_ctxt_RDD = ctxt_one_rdd.zip(ctxt_zero_rdd);

        // call homomorphic addition operators on the rdds
        JavaRDD<String> Addition_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_ADD);
        });
        System.out.println("Homomorphic Addition:"+ SparkFHE.getInstance().decrypt(Addition_ctxt_RDD.collect().get(0)));


        // call homomorphic multiply operators on the rdds
        JavaRDD<String> Multiplication_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_MULTIPLY);
        });
        System.out.println("Homomorphic Multiplication:"+SparkFHE.getInstance().decrypt(Multiplication_ctxt_RDD.collect().get(0)));


        // call homomorphic subtraction operators on the rdds
        JavaRDD<String> Subtraction_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_SUBTRACT);
        });
        System.out.println("Homomorphic Subtraction:"+SparkFHE.getInstance().decrypt(Subtraction_ctxt_RDD.collect().get(0)));
    }


    public static void main(String[] args) {

        // The variable slices represent the number of time a task is split up
        int slices = 2;

        // Create a SparkConf that loads defaults from system properties and the classpath
        SparkConf sparkConf;

        // Loading the C++ library
        SparkFHESetup.setup();

        // Decide whether to run the task locally or on the clusters
        if ( "local".equalsIgnoreCase(args[0]) ) {
            //Provides the Spark driver application a name for easy identification in the Spark or Yarn UI
            //Setting the master URL, in this case its local with 1 thread
            sparkConf = new SparkConf().setAppName("BasicOPsExample").setMaster("local");
        } else {
            slices=Integer.parseInt(args[0]);
            //Provides the Spark driver application a name for easy identification in the Spark or Yarn UI
            sparkConf = new SparkConf().setAppName("BasicOPsExample");
        }

        // Creating a session to Spark. The session allows the creation of the
        // various data abstractions such as RDDs, DataFrame, and more.
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();

        // Creating spark context which allows the communication with worker nodes
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        // Testing and printing the addition function
        System.out.println(String.valueOf(SparkFHE.do_basic_op(1,1, SparkFHE.ADD)));

        // read in the public and secret key and their associated files from terminal arguments
        String pk = args[1];
        String sk = args[2];
        CTXT_0_FILE = args[3];
        CTXT_1_FILE = args[4];

        // Create SparkFHE object with HElib, a library that implements homomorphic encryption
        SparkFHE.init(FHELibrary.HELIB,  pk, sk);

//        SparkFHE.init(FHELibrary.HELIB,  Config.DEFAULT_PUBLIC_KEY_FILE, Config.DEFAULT_SECRET_KEY_FILE);
//        CTXT_0_FILE = Config.DEFAULT_RECORDS_DIRECTORY+"/ptxt_long_0_"+ SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json";
//        CTXT_1_FILE = Config.DEFAULT_RECORDS_DIRECTORY+"/ptxt_long_1_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json";

        // Start testing the basic operations in Helib on plain text, such as addition, subtraction, and multiply.
        test_basic_op();

        // String testing the basic operations in Helib on encrypted data, such as addition, subtraction, and multiply.
        test_FHE_basic_op(spark, slices);

        // Stop existing spark context
        jsc.close();

        // Stop existing spark session
        spark.close();
    }

}
