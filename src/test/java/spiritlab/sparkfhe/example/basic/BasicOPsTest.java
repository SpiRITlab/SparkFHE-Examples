//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.Function;
import org.apache.spark.api.java.function.MapFunction;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Encoder;
import org.apache.spark.sql.Encoders;
import org.apache.spark.sql.SparkSession;
import org.junit.jupiter.api.*;
import scala.collection.Seq;
import spiritlab.sparkfhe.api.*;
import spiritlab.sparkfhe.example.Config;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/*
More info, https://junit.org/junit5/docs/current/user-guide/#writing-tests-classes-and-methods
*/

@DisplayName("TestCase for basic operations")
public class BasicOPsTest {
    private static String CTXT_0_FILE;
    private static String CTXT_1_FILE;
    private static final int slices = 2;

    private static SparkConf sparkConf;
    private static SparkSession spark;
    private static JavaSparkContext jsc;

    @BeforeAll
    @DisplayName("Init before all tests")
    static void initAll() {
        System.out.println("libSparkFHE path: " + System.getProperty("java.library.path"));

        try {
            System.loadLibrary("SparkFHE");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. \n" + e);
            System.exit(1);
        }
        System.out.println("Loaded native code library. \n");


        sparkConf = new SparkConf().setAppName("BasicOPsTest").setMaster("local");
        // set a fast serializer
        sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        List<Class<?>> classes = Arrays.<Class<?>>asList(
                Ciphertext.class,
                Plaintext.class
                );
        sparkConf.registerKryoClasses((Class<?>[]) classes.toArray());
        sparkConf.set("spark.executor.memory", "1g");
        sparkConf.set("spark.driver.memory", "4g");
        spark = SparkSession.builder().config(sparkConf).getOrCreate();
        jsc = new JavaSparkContext(spark.sparkContext());

        SparkFHE.init(FHELibrary.HELIB, FHEScheme.BGV, Config.get_default_public_key_file(), Config.get_default_secret_key_file());

        CTXT_0_FILE = Config.get_records_directory()+"/ptxt_long_0_"+ SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";
        CTXT_1_FILE = Config.get_records_directory()+"/ptxt_long_1_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";
        System.out.println("Opening ciphertext files "+CTXT_0_FILE+ " and "+ CTXT_1_FILE);
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
        Encoder<SerializedCiphertextObject> ctxtJSONEncoder = Encoders.bean(SerializedCiphertextObject.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        // READ as a dataset
        Dataset<SerializedCiphertextObject> serialized_ctxt_zero_ds= spark.read().json(CTXT_0_FILE).as(ctxtJSONEncoder);
        JavaRDD<SerializedCiphertextObject> ctxt_zero_rdd = serialized_ctxt_zero_ds.javaRDD();
        assertEquals("0", SparkFHE.getInstance().decrypt(ctxt_zero_rdd.first().getCtxt(), true));

        Dataset<SerializedCiphertextObject> serialized_ctxt_one_ds = spark.read().json(CTXT_1_FILE).as(ctxtJSONEncoder);
        JavaRDD<SerializedCiphertextObject> ctxt_one_rdd = serialized_ctxt_one_ds.javaRDD();
        assertEquals("1", SparkFHE.getInstance().decrypt(ctxt_one_rdd.first().getCtxt(), true));


        // cached here because of reuse later
        JavaPairRDD<SerializedCiphertextObject, SerializedCiphertextObject> Combined_ctxt_RDD = ctxt_one_rdd.zip(ctxt_zero_rdd).cache();

        JavaRDD<SerializedCiphertextObject> Addition_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            return new SerializedCiphertextObject(SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_ADD));
        });
        assertEquals("1", SparkFHE.getInstance().decrypt(Addition_ctxt_RDD.first().getCtxt(), true));

        JavaRDD<SerializedCiphertextObject> Multiplication_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            return new SerializedCiphertextObject(SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_MULTIPLY));
        });
        assertEquals("0", SparkFHE.getInstance().decrypt(Multiplication_ctxt_RDD.first().getCtxt(), true));

        JavaRDD<SerializedCiphertextObject> Subtraction_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            return new SerializedCiphertextObject(SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_SUBTRACT));
        });
        assertEquals("1", SparkFHE.getInstance().decrypt(Subtraction_ctxt_RDD.first().getCtxt(), true));

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
