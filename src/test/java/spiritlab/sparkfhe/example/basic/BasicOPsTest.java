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
        System.setProperty("spark.serializer", "spark.KryoSerializer");
        sparkConf = new SparkConf().setAppName("BasicOPsTest").setMaster("local");
        // Now it's 32 Mb of buffer by default instead of 0.064 Mb
        //sparkConf.set("spark.kryoserializer.buffer", "32");
        // http://www.trongkhoanguyen.com/2015/04/understand-shuffle-component-in-spark.html
        //spark.shuffle.file.buffer	32k	Size of the in-memory buffer for each
        //                                      shuffle file output stream. These buffers
        //                                      reduce the number of disk seeks and system
        //                                      calls made in creating intermediate shuffle files.
        //
//        sparkConf.set("spark.shuffle.file.buffer", "64");
        // set a fast serializer
       sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
        // This setting configures the serializer used for not only shuffling data
        // between worker nodes but also when serializing RDDs to disk. Another
        // requirement for Kryo serializer is to register the classes in advance
        // for best performance. If the classes are not registered, then the kryo
        // would store the full class name with each object (instead of mapping
        // with an ID), which can lead to wasted resource.
//        sparkConf.set("spark.kryo.registrator", "spiritlab.sparkfhe.example.basic.myKryoReg");
        List<Class<?>> classes = Arrays.<Class<?>>asList(
                Ciphertext.class,
                Plaintext.class,
                SerializedCiphertextObject.class,
                SWIGTYPE_p_std__variantT_std__string_Ctxt_seal__Ciphertext_t.class,
                SWIGTYPE_p_Ctxt.class,
                SWIGTYPE_p_seal__Ciphertext.class
                );
        sparkConf.registerKryoClasses((Class<?>[]) classes.toArray());
        spark = SparkSession.builder().config(sparkConf).getOrCreate();
        jsc = new JavaSparkContext(spark.sparkContext());

        SparkFHE.init(FHELibrary.HELIB,  Config.get_default_public_key_file(), Config.get_default_secret_key_file());

        CTXT_0_FILE = Config.get_records_directory()+"/ptxt_long_0_"+ SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json";
        CTXT_1_FILE = Config.get_records_directory()+"/ptxt_long_1_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".json";
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
        assertEquals("0", SparkFHE.getInstance().decrypt(new Ciphertext(serialized_ctxt_zero_ds.javaRDD().first().getCtxt())).toString());


        JavaRDD<String> testRDD0 = serialized_ctxt_zero_ds.javaRDD().map(x -> {
            return "hello0";
        });
        System.out.println("testing0: " + testRDD0.first());


        JavaRDD<String> testRDD1 = serialized_ctxt_zero_ds.javaRDD().map(x -> {
               return "hello1";
        });
        System.out.println("testing1: " + testRDD1.first());

        JavaRDD<SerializedCiphertextObject> testRDD2 = serialized_ctxt_zero_ds.javaRDD().map(x -> {
                SerializedCiphertextObject tmp = new SerializedCiphertextObject();
                tmp.setCtxt("hello");
                return tmp;
        });
        System.out.println("testing2: " + testRDD2.first().getCtxt());


//        JavaRDD<SCOWrapper> testRDD = serialized_ctxt_zero_ds.javaRDD().map(x -> {
//            SCOWrapper usw = new SCOWrapper() {
//                public SerializedCiphertextObject create() {
//                    SerializedCiphertextObject tmp = new SerializedCiphertextObject();
//                    tmp.setCtxt("SCO hello");
//                    return tmp;
//                }
//            };
//
//            return usw;
//        });
//        System.out.println("new testing " + testRDD.first().create().getCtxt());


        JavaRDD<Ciphertext> ctxt_zero_rdd = serialized_ctxt_zero_ds.javaRDD().map(x -> {
            return new Ciphertext("1");
        });
        System.out.println("testing ciphertext: " + ctxt_zero_rdd.first().toString());



        Dataset<SerializedCiphertextObject> serialized_ctxt_one_ds = spark.read().json(CTXT_1_FILE).as(ctxtJSONEncoder);
        assertEquals("1", SparkFHE.getInstance().decrypt(new Ciphertext(serialized_ctxt_one_ds.javaRDD().first().getCtxt())).toString());

        JavaPairRDD<SerializedCiphertextObject, SerializedCiphertextObject> Combined_ctxt_RDD = serialized_ctxt_one_ds.javaRDD().zip(serialized_ctxt_zero_ds.javaRDD());


        JavaRDD<SerializedCiphertextObject> Addition_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            SerializedCiphertextObject sco = new SerializedCiphertextObject();
            sco.setCtxt(SparkFHE.getInstance().do_FHE_basic_op(new Ciphertext(tuple._1().getCtxt()), new Ciphertext(tuple._2().getCtxt()), SparkFHE.FHE_ADD).toString());
            return sco;
        });
        assertEquals("1", SparkFHE.getInstance().decrypt(new Ciphertext(Addition_ctxt_RDD.collect().get(0).getCtxt())).toString());

        JavaRDD<SerializedCiphertextObject> Multiplication_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            SerializedCiphertextObject sco = new SerializedCiphertextObject();
            sco.setCtxt(SparkFHE.getInstance().do_FHE_basic_op(new Ciphertext(tuple._1().getCtxt()), new Ciphertext(tuple._2().getCtxt()), SparkFHE.FHE_MULTIPLY).toString());
            return sco;
        });
        assertEquals("0", SparkFHE.getInstance().decrypt(new Ciphertext(Multiplication_ctxt_RDD.collect().get(0).getCtxt())).toString());

        JavaRDD<SerializedCiphertextObject> Subtraction_ctxt_RDD = Combined_ctxt_RDD.map(tuple -> {
            SerializedCiphertextObject sco = new SerializedCiphertextObject();
            sco.setCtxt(SparkFHE.getInstance().do_FHE_basic_op(new Ciphertext(tuple._1().getCtxt()), new Ciphertext(tuple._2().getCtxt()), SparkFHE.FHE_SUBTRACT).toString());
            return sco;
        });
        assertEquals("1", SparkFHE.getInstance().decrypt(new Ciphertext(Subtraction_ctxt_RDD.collect().get(0).getCtxt())).toString());
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
