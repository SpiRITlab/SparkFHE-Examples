package spiritlab.sparkfhe.example.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.*;
import org.apache.spark.spiritlab.sparkfhe.SparkFHESetup;
import spiritlab.sparkfhe.api.*;
import spiritlab.sparkfhe.example.Config;


public class BasicOPsExample {

    private static String CTXT_0_FILE;
    private static String CTXT_1_FILE;


    public static void test_basic_op() {
        System.out.println("ADD(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.ADD));
        System.out.println("MUL(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.MUL));
        System.out.println("SUB(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.SUB));
    }

    public static void test_FHE_basic_op(SparkSession spark, int slices, Broadcast<String> pk_b, Broadcast<String> sk_b) {
        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<CtxtString> ctxtJSONEncoder = Encoders.bean(CtxtString.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        // READ as a dataset
        Dataset<CtxtString> ctxt_zero_ds = spark.read().json(CTXT_0_FILE).as(ctxtJSONEncoder);
        System.out.println("Ciphertext Zero:"+SparkFHE.getInstance().decrypt(ctxt_zero_ds.first().getCtxt()));
        Dataset<CtxtString> ctxt_one_ds = spark.read().json(CTXT_1_FILE).as(ctxtJSONEncoder);
        System.out.println("Ciphertext One:"+SparkFHE.getInstance().decrypt(ctxt_one_ds.first().getCtxt()));

        JavaRDD<CtxtString> ctxt_zero_rdd = ctxt_zero_ds.javaRDD();
        JavaRDD<CtxtString> ctxt_one_rdd = ctxt_one_ds.javaRDD();

        JavaPairRDD<CtxtString, CtxtString> Combined_ctxt_RDD = ctxt_one_rdd.zip(ctxt_zero_rdd);

        JavaRDD<String> Addition_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
            SparkFHESetup.setup();
            SparkFHE.init(FHELibrary.HELIB,  pk_b.getValue(), sk_b.getValue());
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_ADD);
        });
        System.out.println("Homomorphic Addition:"+ SparkFHE.getInstance().decrypt(Addition_ctxt_RDD.collect().get(0)));

        JavaRDD<String> Multiplication_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
            SparkFHESetup.setup();
            SparkFHE.init(FHELibrary.HELIB,  pk_b.getValue(), sk_b.getValue());
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_MULTIPLY);
        });
        System.out.println("Homomorphic Multiplication:"+ SparkFHE.getInstance().decrypt(Multiplication_ctxt_RDD.collect().get(0)));

        JavaRDD<String> Subtraction_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
            SparkFHESetup.setup();
            SparkFHE.init(FHELibrary.HELIB,  pk_b.getValue(), sk_b.getValue());
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_SUBTRACT);
        });
        System.out.println("Homomorphic Subtraction:"+SparkFHE.getInstance().decrypt(Subtraction_ctxt_RDD.collect().get(0)));
    }


    public static void main(String[] args) {
        int slices = 2;
        SparkConf sparkConf = new SparkConf();
        sparkConf.setAppName("BasicOPsExample");

        if ( "local".equalsIgnoreCase(args[0]) ) {
            sparkConf.setMaster("local");
        } else {
            slices=Integer.parseInt(args[0]);
            Config.update_current_directory(sparkConf.get("spark.mesos.executor.home"));
            System.out.println("CURRENT_DIRECTORY = "+Config.get_current_directory());
        }
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        System.out.println("after Spark context");
        String pk = args[1];
        String sk = args[2];
        CTXT_0_FILE = args[3];
        CTXT_1_FILE = args[4];

        // Note, the following loading of shared library and init are done on driver only. We need to do the same on the executors.
        // required to load our shared library
        SparkFHESetup.setup();
        System.out.println("after SparkFHE setup");
        // create SparkFHE object
        SparkFHE.init(FHELibrary.HELIB,  pk, sk);
        System.out.println("after SparkFHE init");

        Broadcast<String> pk_b = jsc.broadcast(pk);
        Broadcast<String> sk_b = jsc.broadcast(sk);

        System.out.println(String.valueOf(SparkFHE.do_basic_op(1,1, SparkFHE.ADD)));

        test_basic_op();
        test_FHE_basic_op(spark, slices, pk_b, sk_b);

        jsc.close();
        spark.close();
    }

}
