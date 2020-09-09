//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.MapPartitionsFunction;
import org.apache.spark.api.java.function.ReduceFunction;
import org.apache.spark.sql.*;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import org.junit.jupiter.api.*;
import scala.Tuple2;
import spiritlab.sparkfhe.api.*;
import spiritlab.sparkfhe.example.Config;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static org.apache.spark.sql.functions.col;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@DisplayName("TestCase for calculating dot product")
public class DotProductTest {
    private static String vec_a_ctxt;
    private static String vec_b_ctxt;
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

        sparkConf = new SparkConf().setAppName("DotProductTest").setMaster("local");
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

        vec_a_ctxt = Config.get_records_directory()+"/vec_a_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+".jsonl";
        vec_b_ctxt = Config.get_records_directory()+"/vec_b_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+".jsonl";

    }

    @BeforeEach
    @DisplayName("Init before test")
    void init() {
    }

    @Test
    @DisplayName("Calculate dot product on plaintext")
    public void test_basic_dot_product() {
        JavaRDD A = jsc.parallelize(Arrays.asList(0,1,2,3,4), slices);
        JavaRDD B = jsc.parallelize(Arrays.asList(4,3,2,1,0), slices);

        JavaPairRDD<Integer, Integer> Combined_RDD = A.zip(B);

        /* print values */
        Combined_RDD.foreach(data -> {
            assertEquals(4, data._1+data._2);
        });

        JavaRDD<Integer> Result_RDD = Combined_RDD.map(tuple -> {
            return SparkFHE.do_basic_op(tuple._1(), tuple._2(), SparkFHE.MUL);
        });

        // TODO need to use plaintext module
        assertEquals(10, (int)Result_RDD.reduce((x, y) -> {
            return SparkFHE.do_basic_op(x, y, SparkFHE.ADD);
        }));
    }

    @Test
    @DisplayName("Calculate dot product on ciphertext as lambda")
    public void test_FHE_dot_product_via_lambda() {
        assertNotNull(spark);
        // Encoders are created for Java beans
        Encoder<SerializedCiphertext> ctxtJSONEncoder = Encoders.bean(SerializedCiphertext.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        // READ as a dataset
        Dataset<SerializedCiphertext> ctxt_a_ds = spark.read().json(vec_a_ctxt).as(ctxtJSONEncoder);
        Dataset<SerializedCiphertext> ctxt_b_ds = spark.read().json(vec_b_ctxt).as(ctxtJSONEncoder);

        JavaRDD<SerializedCiphertext> ctxt_a_rdd = ctxt_a_ds.select(ctxt_a_ds.col("ctxt")).as(Encoders.STRING()).javaRDD().map(x -> new SerializedCiphertext(x));;
        JavaRDD<SerializedCiphertext> ctxt_b_rdd = ctxt_b_ds.select(ctxt_b_ds.col("ctxt")).as(Encoders.STRING()).javaRDD().map(x -> new SerializedCiphertext(x));;
        JavaPairRDD<SerializedCiphertext, SerializedCiphertext> combined_ctxt_rdd = ctxt_a_rdd.zip(ctxt_b_rdd);

        JavaRDD<SerializedCiphertext> result_rdd = combined_ctxt_rdd.map(tuple -> {
            return new SerializedCiphertext(SparkFHE.getInstance().do_FHE_basic_op(tuple._1().getCtxt(), tuple._2().getCtxt(), SparkFHE.FHE_MULTIPLY));
        });

        assertEquals(String.valueOf(10), SparkFHE.getInstance().decrypt(result_rdd.reduce((x, y) -> {
            return new SerializedCiphertext(SparkFHE.getInstance().do_FHE_basic_op(x.getCtxt(), y.getCtxt(), SparkFHE.FHE_ADD));
        }).getCtxt(), true));
    }

    @Test
    @DisplayName("Calculate dot product on ciphertext as native code")
    public void test_FHE_dot_product_via_native_code() {
        assertNotNull(spark);
        // Encoders are created for Java beans
        Encoder<SerializedCiphertext> ctxtJSONEncoder = Encoders.bean(SerializedCiphertext.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        // READ as a dataset
        Dataset<SerializedCiphertext> ctxt_a_ds = spark.read().json(vec_a_ctxt).as(ctxtJSONEncoder);
        Dataset<SerializedCiphertext> ctxt_b_ds = spark.read().json(vec_b_ctxt).as(ctxtJSONEncoder);

        JavaRDD<SerializedCiphertext> ctxt_a_rdd = ctxt_a_ds.select(ctxt_a_ds.col("ctxt")).as(Encoders.STRING()).javaRDD().map(x -> new SerializedCiphertext(x));
        JavaRDD<SerializedCiphertext> ctxt_b_rdd = ctxt_b_ds.select(ctxt_b_ds.col("ctxt")).as(Encoders.STRING()).javaRDD().map(x -> new SerializedCiphertext(x));

        assertEquals(5, ctxt_a_rdd.count());
        ctxt_a_rdd.foreach(data -> {
            System.out.println(SparkFHE.getInstance().decrypt(data.getCtxt(), true));
        });
        assertEquals(5, ctxt_b_rdd.count());
        ctxt_b_rdd.foreach(data -> {
            System.out.println(SparkFHE.getInstance().decrypt(data.getCtxt(), true));
        });

        JavaPairRDD<SerializedCiphertext, SerializedCiphertext> combined_ctxt_rdd = ctxt_a_rdd.zip(ctxt_b_rdd);
        assertEquals(5, combined_ctxt_rdd.count());

        JavaRDD<SerializedCiphertext> collection = combined_ctxt_rdd.mapPartitions(records -> {
            LinkedList<SerializedCiphertext> v = new LinkedList<SerializedCiphertext>();
            StringVector a = new StringVector();
            StringVector b = new StringVector();
            while (records.hasNext()) {
                Tuple2<SerializedCiphertext, SerializedCiphertext> rec = records.next();
                a.add(rec._1.getCtxt());
                b.add(rec._2.getCtxt());
            }
            v.add(new SerializedCiphertext(SparkFHE.getInstance().do_FHE_dot_product(a, b)));
            return v.iterator();
        });

        collection.cache();

        SerializedCiphertext res = collection.reduce((x, y) -> {
            return new SerializedCiphertext(SparkFHE.getInstance().do_FHE_basic_op(x.getCtxt(), y.getCtxt(), SparkFHE.FHE_ADD));
        });

        assertEquals(String.valueOf(10), SparkFHE.getInstance().decrypt(res.getCtxt(), true));
    }

    @Test
    @DisplayName("Calculate dot product on ciphertext as sql")
    public void test_FHE_dot_product_via_sql() {
        assertNotNull(spark);
        // Encoders are created for Java beans
        Encoder<SerializedCiphertext> ctxtJSONEncoder = Encoders.bean(SerializedCiphertext.class);
        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        // READ as a dataset
        Dataset<SerializedCiphertext> ctxt_a_ds = spark.read().json(vec_a_ctxt).as(ctxtJSONEncoder);
        Dataset<SerializedCiphertext> ctxt_b_ds = spark.read().json(vec_b_ctxt).as(ctxtJSONEncoder);

        Dataset<String> ctxt_a_ds2 = ctxt_a_ds.select(ctxt_a_ds.col("ctxt").as("ctxt_a")).as(Encoders.STRING());
        Dataset<String> ctxt_b_ds2 = ctxt_b_ds.select(ctxt_b_ds.col("ctxt").as("ctxt_b")).as(Encoders.STRING());

        Dataset<Row> ctxt_a_ds3 = ctxt_a_ds2.withColumn("id", functions.monotonically_increasing_id());
        ctxt_a_ds3 = ctxt_a_ds3.withColumn("id", functions.row_number().over(Window.orderBy("id")));
        Dataset<Row> ctxt_b_ds3 = ctxt_b_ds2.withColumn("id", functions.monotonically_increasing_id());
        ctxt_b_ds3 = ctxt_b_ds3.withColumn("id", functions.row_number().over(Window.orderBy("id")));

        Dataset<Row> joined = ctxt_a_ds3.join(ctxt_b_ds3, ctxt_a_ds3.col("id").equalTo(ctxt_b_ds3.col("id")));
        Dataset<Row> fin = joined.select(col("ctxt_a"), col("ctxt_b"));

        fin.printSchema();

        StructType structType = new StructType();
        structType = structType.add("ctxt_a", DataTypes.StringType, false);
        structType = structType.add("ctxt_b", DataTypes.StringType, false);

        ExpressionEncoder<Row> encoder = RowEncoder.apply(structType);
        ExpressionEncoder<Row> encoder2 = RowEncoder.apply(structType);

        Dataset<SerializedCiphertext> collection = fin.mapPartitions((MapPartitionsFunction<Row, SerializedCiphertext>)  iter -> {
            LinkedList<SerializedCiphertext> v = new LinkedList<SerializedCiphertext>();
            StringVector a = new StringVector();
            StringVector b = new StringVector();
            while (iter.hasNext()) {
                Row row = iter.next();
                a.add(row.getAs("ctxt_a"));
                b.add(row.getAs("ctxt_b"));
            }
            v.add(new SerializedCiphertext(SparkFHE.getInstance().do_FHE_dot_product(a, b)));
            return v.iterator();

        }, Encoders.kryo(SerializedCiphertext.class));

        collection.cache();
        collection.printSchema();

        SerializedCiphertext res = collection.reduce((ReduceFunction<SerializedCiphertext>) (x, y) -> {
            return new SerializedCiphertext(SparkFHE.getInstance().do_FHE_basic_op(x.getCtxt(), y.getCtxt(), SparkFHE.FHE_ADD));
        });

        assertEquals(String.valueOf(10), SparkFHE.getInstance().decrypt(res.getCtxt(), true));
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
