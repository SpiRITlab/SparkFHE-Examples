package spiritlab.sparkfhe.example.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.*;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.CtxtString;

public class BasicOPsExample {
    static {
        System.out.println("libSparkFHE path: " + System.getProperty("java.library.path"));
        try {
            System.loadLibrary("SparkFHE");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. \n" + e);
            System.exit(1);
        }
        System.out.println("Loaded native code library. \n");
    }

    private static String sparkfhe_path="../SparkFHE";
    private static String zero_ctxt = "ptxt_long_0_PlaintextModule71CiphertextModule15313MultiplicativeDepth15SecurityParameter80.json";
    private static String one_ctxt = "ptxt_long_1_PlaintextModule71CiphertextModule15313MultiplicativeDepth15SecurityParameter80.json";

    public static void test_basic_op() {
        System.out.println("ADD(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.ADD));
        System.out.println("MUL(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.MUL));
        System.out.println("SUB(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.SUB));
    }

    public static void test_FHE_basic_op(SparkSession spark, int slices) {
        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<CtxtString> ctxtJSONEncoder = Encoders.bean(CtxtString.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        String ctxt_zero_rdd_path = sparkfhe_path+"/bin/records/"+zero_ctxt;
        String ctxt_one_rdd_path = sparkfhe_path+"/bin/records/"+one_ctxt;


        // READ as a dataset
        Dataset<CtxtString> ctxt_zero_ds = spark.read().json(ctxt_zero_rdd_path).as(ctxtJSONEncoder);
        System.out.println("Ciphertext Zero:"+SparkFHE.getInstance().decrypt(ctxt_zero_ds.first().getCtxt()));
        Dataset<CtxtString> ctxt_one_ds = spark.read().json(ctxt_one_rdd_path).as(ctxtJSONEncoder);
        System.out.println("Ciphertext Zero:"+SparkFHE.getInstance().decrypt(ctxt_one_ds.first().getCtxt()));


        JavaRDD<CtxtString> ctxt_zero_rdd = ctxt_zero_ds.javaRDD();
        JavaRDD<CtxtString> ctxt_one_rdd = ctxt_one_ds.javaRDD();

        JavaPairRDD<CtxtString, CtxtString> Combined_ctxt_RDD = ctxt_one_rdd.zip(ctxt_zero_rdd);

        JavaRDD<String> Addition_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_ADD);
        });
        System.out.println("Homomorphic Addition:"+ SparkFHE.getInstance().decrypt(Addition_ctxt_RDD.collect().get(0)));

        JavaRDD<String> Multiplication_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_MULTIPLY);
        });
        System.out.println("Homomorphic Multiplication:"+SparkFHE.getInstance().decrypt(Multiplication_ctxt_RDD.collect().get(0)));

        JavaRDD<String> Subtraction_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_SUBTRACT);
        });
        System.out.println("Homomorphic Subtraction:"+SparkFHE.getInstance().decrypt(Subtraction_ctxt_RDD.collect().get(0)));
    }


    public static void main(String argv[]) {
        int slices = (argv.length == 1) ? Integer.parseInt(argv[0]) : 2;
        // when testing directly, you will need to set master to local
//        SparkConf sparkConf = new SparkConf().setAppName("DotProductExample").setMaster("local");
        SparkConf sparkConf = new SparkConf().setAppName("BasicOPsExample");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        SparkFHE.init(FHELibrary.HELIB,  sparkfhe_path + "/bin/keys/public_key.txt", sparkfhe_path + "/bin/keys/secret_key.txt");

        test_basic_op();
        test_FHE_basic_op(spark, slices);

        jsc.close();
        spark.close();
    }

}
