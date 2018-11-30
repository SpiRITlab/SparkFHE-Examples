package spiritlab.sparkfhe.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.*;
import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.api.CtxtString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/*
More info, https://junit.org/junit5/docs/current/user-guide/#writing-tests-classes-and-methods
 */

@DisplayName("TestCase for basic operations")
public class BasicOPsTest {
    private static String sparkfhe_path="../SparkFHE";
    private static String zero_ctxt = "ptxt_long_0_PlaintextModule71CiphertextModule15313MultiplicativeDepth15SecurityParameter80.json";
    private static String one_ctxt = "ptxt_long_1_PlaintextModule71CiphertextModule15313MultiplicativeDepth15SecurityParameter80.json";
    private static final int slices = 4;

    private static SparkConf sparkConf;
    private static SparkSession spark;
    private static JavaSparkContext jsc;

    @BeforeAll
    @DisplayName("Init before all tests")
    static void initAll() {
        System.out.println(System.getProperty("java.library.path"));

        try {
            System.loadLibrary("SparkFHE");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. \n" + e);
            System.exit(1);
        }
        System.out.println("Load native code library. \n");

        sparkConf = new SparkConf().setAppName("SparkFHETest").setMaster("local");
        spark = SparkSession.builder().config(sparkConf).getOrCreate();
        jsc = new JavaSparkContext(spark.sparkContext());

        SparkFHE.init(FHELibrary.HELIB, sparkfhe_path + "/bin/keys/public_key.txt", sparkfhe_path + "/bin/keys/secret_key.txt");
    }

    @BeforeEach
    @DisplayName("Init before test")
    void init() {
    }

    @Test
    @DisplayName("Testing basic operations")
    public void test_basic_op() {
        assertEquals(1, SparkFHE.do_basic_op(1, 0, SparkFHE.ADD));
        assertEquals(0, SparkFHE.do_basic_op(1, 0, SparkFHE.MUL));
        assertEquals(1, SparkFHE.do_basic_op(1, 0, SparkFHE.SUB));
    }

    @Test
    @DisplayName("Testing basic FHE operations (+,*,-) on ciphertext")
    public void test_FHE_basic_op() {
        assertNotNull(spark);
        // Encoders are created for Java beans
        Encoder<CtxtString> ctxtJSONEncoder = Encoders.bean(CtxtString.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        String ctxt_zero_rdd_path = sparkfhe_path + "/bin/records/"+zero_ctxt;
        String ctxt_one_rdd_path = sparkfhe_path + "/bin/records/"+one_ctxt;

        // READ as a dataset
        Dataset<CtxtString> ctxt_zero_ds = spark.read().json(ctxt_zero_rdd_path).as(ctxtJSONEncoder);
        assertEquals("0", SparkFHE.getInstance().decrypt(ctxt_zero_ds.first().getCtxt()));
        Dataset<CtxtString> ctxt_one_ds = spark.read().json(ctxt_one_rdd_path).as(ctxtJSONEncoder);
        assertEquals("1", SparkFHE.getInstance().decrypt(ctxt_one_ds.first().getCtxt()));

        JavaRDD<CtxtString> ctxt_zero_rdd = ctxt_zero_ds.javaRDD();
        JavaRDD<CtxtString> ctxt_one_rdd = ctxt_one_ds.javaRDD();

        JavaPairRDD<CtxtString, CtxtString> Combined_ctxt_RDD = ctxt_one_rdd.zip(ctxt_zero_rdd);

        JavaRDD<String> Addition_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_ADD);
        });
        assertEquals("1", SparkFHE.getInstance().decrypt(Addition_ctxt_RDD.collect().get(0)));

        JavaRDD<String> Multiplication_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_MULTIPLY);
        });
        assertEquals("0", SparkFHE.getInstance().decrypt(Multiplication_ctxt_RDD.collect().get(0)));

        JavaRDD<String> Subtraction_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_SUBTRACT);
        });
        assertEquals("1", SparkFHE.getInstance().decrypt(Subtraction_ctxt_RDD.collect().get(0)));
    }

    @AfterEach
    @DisplayName("teardown after test")
    void tearDown() {
    }

    @AfterAll
    @DisplayName("teardown after all tests")
    static void tearDownAll() {
        jsc.close();
        spark.close();
    }
}
