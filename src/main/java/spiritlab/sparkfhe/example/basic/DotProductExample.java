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
import scala.Tuple2;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.StringVector;
import spiritlab.sparkfhe.api.CtxtString;
import java.util.*;
import static org.apache.spark.sql.functions.col;

public class DotProductExample {

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
    private static String vec_a_ctxt = "vec_a_5_PlaintextModule71CiphertextModule15313MultiplicativeDepth15SecurityParameter80.json";
    private static String vec_b_ctxt = "vec_b_5_PlaintextModule71CiphertextModule15313MultiplicativeDepth15SecurityParameter80.json";

    public static void test_basic_dot_product(JavaSparkContext jsc, int slices) {
        System.out.println("test_basic_dot_product");
        JavaRDD A = jsc.parallelize(Arrays.asList(1,2,3,4,5,6,7,8,9), slices);
        JavaRDD B = jsc.parallelize(Arrays.asList(9,8,7,6,5,4,3,2,1), slices);

        JavaPairRDD<Integer, Integer> Combined_RDD = A.zip(B);

        /* print values */
        Combined_RDD.foreach(data -> {
            System.out.println("Combined_RDD: ("+data._1 +","+ data._2+")");
        });

        JavaRDD<Integer> Result_RDD = Combined_RDD.map(tuple -> {
            return SparkFHE.do_basic_op(tuple._1(), tuple._2(), SparkFHE.MUL);
        });

        System.out.println("Result_RDD:"+Result_RDD.reduce((x, y) -> {
            return SparkFHE.do_basic_op(x, y, SparkFHE.ADD);
        }));
    }


    public static void test_FHE_dot_product_via_lambda(SparkSession spark, int slices) {
        System.out.println("test_FHE_dot_product_via_lambda");
        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<CtxtString> ctxtJSONEncoder = Encoders.bean(CtxtString.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        String ctxt_a_rdd_path = sparkfhe_path + "/bin/records/"+vec_a_ctxt;
        String ctxt_b_rdd_path = sparkfhe_path + "/bin/records/"+vec_b_ctxt;

        // READ as a dataset
        Dataset<CtxtString> ctxt_a_ds = spark.read().json(ctxt_a_rdd_path).as(ctxtJSONEncoder);
        Dataset<CtxtString> ctxt_b_ds = spark.read().json(ctxt_b_rdd_path).as(ctxtJSONEncoder);

        JavaRDD<String> ctxt_a_rdd = ctxt_a_ds.select(org.apache.spark.sql.functions.explode(ctxt_a_ds.col("ctxt")).alias("ctxt")).as(Encoders.STRING()).javaRDD();
        JavaRDD<String> ctxt_b_rdd = ctxt_b_ds.select(org.apache.spark.sql.functions.explode(ctxt_b_ds.col("ctxt")).alias("ctxt")).as(Encoders.STRING()).javaRDD();
        JavaRDD<String> ctxt_a_rdd2 = ctxt_a_rdd.repartition(slices);
        JavaRDD<String> ctxt_b_rdd2 = ctxt_b_rdd.repartition(slices);
        System.out.println("Partitions:"+ctxt_a_rdd2.partitions().size());
        JavaPairRDD<String, String> combined_ctxt_rdd = ctxt_a_rdd2.zip(ctxt_b_rdd2);

        JavaRDD<String> result_rdd = combined_ctxt_rdd.map(tuple -> {
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1(), tuple._2(), SparkFHE.FHE_MULTIPLY);
        });

        System.out.println("Dot product: " + SparkFHE.getInstance().decrypt(result_rdd.reduce((x, y) -> {
            return SparkFHE.getInstance().do_FHE_basic_op(x, y, SparkFHE.FHE_ADD);
        })));
    }


    public static void test_FHE_dot_product_via_native_code(SparkSession spark, int slices) {
        System.out.println("test_FHE_dot_product_via_native_code");
        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<CtxtString> ctxtJSONEncoder = Encoders.bean(CtxtString.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        String ctxt_a_rdd_path = sparkfhe_path + "/bin/records/"+vec_a_ctxt;
        String ctxt_b_rdd_path = sparkfhe_path + "/bin/records/"+vec_b_ctxt;

        // READ as a dataset
        Dataset<CtxtString> ctxt_a_ds = spark.read().json(ctxt_a_rdd_path).as(ctxtJSONEncoder);
        Dataset<CtxtString> ctxt_b_ds = spark.read().json(ctxt_b_rdd_path).as(ctxtJSONEncoder);

        JavaRDD<String> ctxt_a_rdd = ctxt_a_ds.select(org.apache.spark.sql.functions.explode(ctxt_a_ds.col("ctxt")).alias("ctxt")).as(Encoders.STRING()).javaRDD();
        JavaRDD<String> ctxt_b_rdd = ctxt_b_ds.select(org.apache.spark.sql.functions.explode(ctxt_b_ds.col("ctxt")).alias("ctxt")).as(Encoders.STRING()).javaRDD();

        System.out.println("ctxt_a_rdd.count() = " + ctxt_a_rdd.count());
        ctxt_a_rdd.foreach(data -> {
            System.out.println(SparkFHE.getInstance().decrypt(data));
        });
        System.out.println("ctxt_b_rdd.count() = " + ctxt_b_rdd.count());
        ctxt_b_rdd.foreach(data -> {
            System.out.println(SparkFHE.getInstance().decrypt(data));
        });

        JavaPairRDD<String, String> combined_ctxt_rdd = ctxt_a_rdd.zip(ctxt_b_rdd);
        combined_ctxt_rdd.repartition(slices);
        System.out.println("combined_ctxt_rdd.count() = " + combined_ctxt_rdd.count());

        JavaRDD<String> collection = combined_ctxt_rdd.mapPartitions(records -> {
            LinkedList v = new LinkedList<String>();
            StringVector a = new StringVector();
            StringVector b = new StringVector();
            while (records.hasNext()) {
                Tuple2<String, String> rec = records.next();
                a.add(rec._1);
                b.add(rec._2);
            }
            String r = SparkFHE.getInstance().do_FHE_dot_product(a, b);
            v.add(r);
            return v.iterator();
        });

        collection.cache();
        System.out.println("collection.count() = " + collection.count());

        String res = collection.reduce((x, y) -> {
            return SparkFHE.getInstance().do_FHE_basic_op(x, y, SparkFHE.FHE_ADD);
        });

        System.out.println("Dot product: " + SparkFHE.getInstance().decrypt(res));
    }

    public static void test_FHE_dot_product_via_sql(SparkSession spark, int slices) {
        System.out.println("test_FHE_dot_product_via_sql");
        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<CtxtString> ctxtJSONEncoder = Encoders.bean(CtxtString.class);
        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        String ctxt_a_rdd_path = sparkfhe_path + "/bin/records/"+vec_a_ctxt;
        String ctxt_b_rdd_path = sparkfhe_path + "/bin/records/"+vec_b_ctxt;

        // READ as a dataset
        Dataset<CtxtString> ctxt_a_ds = spark.read().json(ctxt_a_rdd_path).as(ctxtJSONEncoder);
        Dataset<CtxtString> ctxt_b_ds = spark.read().json(ctxt_b_rdd_path).as(ctxtJSONEncoder);

        Dataset<String> ctxt_a_ds2 = ctxt_a_ds.select(org.apache.spark.sql.functions.explode(ctxt_a_ds.col("ctxt")).alias("ctxt")).as(Encoders.STRING());
        Dataset<String> ctxt_b_ds2 = ctxt_b_ds.select(org.apache.spark.sql.functions.explode(ctxt_b_ds.col("ctxt")).alias("ctxt2")).as(Encoders.STRING());

        Dataset<Row> ctxt_a_ds3 = ctxt_a_ds2.withColumn("id", functions.monotonically_increasing_id());
        ctxt_a_ds3 = ctxt_a_ds3.withColumn("id", functions.row_number().over(Window.orderBy("id")));
        Dataset<Row> ctxt_b_ds3 = ctxt_b_ds2.withColumn("id", functions.monotonically_increasing_id());
        ctxt_b_ds3 = ctxt_b_ds3.withColumn("id", functions.row_number().over(Window.orderBy("id")));

        Dataset<Row> joined = ctxt_a_ds3.join(ctxt_b_ds3, ctxt_a_ds3.col("id").equalTo(ctxt_b_ds3.col("id")));
        Dataset<Row> fin = joined.select(col("ctxt"), col("ctxt2"));
        fin.repartition(slices);

        fin.printSchema();

        StructType structType = new StructType();
        structType = structType.add("ctxt", DataTypes.StringType, false);
        structType = structType.add("ctxt2", DataTypes.StringType, false);

        ExpressionEncoder<Row> encoder = RowEncoder.apply(structType);
        ExpressionEncoder<Row> encoder2 = RowEncoder.apply(structType);

        Dataset<String> collection = fin.mapPartitions((MapPartitionsFunction<Row, String>)  iter -> {
            LinkedList v = new LinkedList<String>();
            StringVector a = new StringVector();
            StringVector b = new StringVector();
            while (iter.hasNext()) {
                Row row = iter.next();
                a.add(row.getAs("ctxt"));
                b.add(row.getAs("ctxt2"));
            }
            String r = SparkFHE.getInstance().do_FHE_dot_product(a, b);
            v.add(r);
            return v.iterator();

        }, Encoders.STRING());

        collection.cache();

        System.out.println("collection.count() = " + collection.count());
        collection.printSchema();

        String res = collection.reduce((ReduceFunction<String>) (x, y) -> {
            return SparkFHE.getInstance().do_FHE_basic_op(x, y, SparkFHE.FHE_ADD);
        });

        System.out.println("Dot product: " + SparkFHE.getInstance().decrypt(res));
    }


    public static void main(String[] argv) {
        int slices = (argv.length == 1) ? Integer.parseInt(argv[0]) : 2;
        // when testing directly, you will need to set master to local
//        SparkConf sparkConf = new SparkConf().setAppName("DotProductExample").setMaster("local");
        SparkConf sparkConf = new SparkConf().setAppName("DotProductExample");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        SparkFHE.init(FHELibrary.HELIB, sparkfhe_path + "/bin/keys/public_key.txt", sparkfhe_path + "/bin/keys/secret_key.txt");

        test_basic_dot_product(jsc, slices);
        test_FHE_dot_product_via_lambda(spark, slices);
        test_FHE_dot_product_via_native_code(spark, slices);
        test_FHE_dot_product_via_sql(spark, slices);


        jsc.close();
        spark.close();
    }

}
