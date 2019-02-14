package spiritlab.sparkfhe.example.basic;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.MapPartitionsFunction;
import org.apache.spark.api.java.function.ReduceFunction;
import org.apache.spark.spiritlab.sparkfhe.SparkFHESetup;
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
import spiritlab.sparkfhe.example.Config;

import java.util.*;
import static org.apache.spark.sql.functions.col;

/**
 * This is an example for SparkFHE project. Created to test the functionality
 * of the doc product operation on both plaintext and cipher-text, in local and distributed environment.
 */
public class DotProductExample {

    // Loading up the necessary libraries for Java and C++ interaction
    static {
        System.out.println("Execution path: " + System.getProperty("user.dir"));
        System.out.println("libSparkFHE path: " + System.getProperty("java.library.path"));
        try {
            System.loadLibrary("SparkFHE");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. \n" + e);
            System.exit(1);
        }
        System.out.println("Loaded native code library. \n");
    }

    // declare variables to hold cipher-text
    private static String vec_a_ctxt;
    private static String vec_b_ctxt;


    /**
     * This method performs the dot product operation on plaintext vectors and print out the results
     * @param jsc spark context which allows the communication with worker nodes
     * @param slices the number of time a task is split up
     */
    public static void test_basic_dot_product(JavaSparkContext jsc, int slices) {
        System.out.println("test_basic_dot_product");

        // distribute a local Scala collection (lists in this case) to form 2 RDDs
        JavaRDD A = jsc.parallelize(Arrays.asList(1,2,3,4,5,6,7,8,9), slices);
        JavaRDD B = jsc.parallelize(Arrays.asList(9,8,7,6,5,4,3,2,1), slices);

        // combine both RDDs as pairs
        JavaPairRDD<Integer, Integer> Combined_RDD = A.zip(B);

        /* print values */
        Combined_RDD.foreach(data -> {
            System.out.println("Combined_RDD: ("+data._1 +","+ data._2+")");
        });

        // perform the multiply operator on each of the pairs
        JavaRDD<Integer> Result_RDD = Combined_RDD.map(tuple -> {
            return SparkFHE.do_basic_op(tuple._1(), tuple._2(), SparkFHE.MUL);
        });

        // sum up the results from the previous operation and display
        System.out.println("Result_RDD:"+Result_RDD.reduce((x, y) -> {
            return SparkFHE.do_basic_op(x, y, SparkFHE.ADD);
        }));
    }

    /**
     * This method performs the dot product operation on cipher-text vectors and print out the results
     * @param spark the spark session which allows the creation of the various data abstractions such
     *              as RDDs, DataFrame, and more.
     * @param slices the number of time a task is split up
     */
    public static void test_FHE_dot_product_via_lambda(SparkSession spark, int slices) {
        System.out.println("test_FHE_dot_product_via_lambda");

        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<CtxtString> ctxtJSONEncoder = Encoders.bean(CtxtString.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        // Create dataset with json file.
        // if CtxtString a row? Dataset<Row> is the Dataframe in Java
        Dataset<CtxtString> ctxt_a_ds = spark.read().json(vec_a_ctxt).as(ctxtJSONEncoder);
        Dataset<CtxtString> ctxt_b_ds = spark.read().json(vec_b_ctxt).as(ctxtJSONEncoder);

        // Represents the content of the DataFrame as an RDD of Rows
        JavaRDD<String> ctxt_a_rdd = ctxt_a_ds.select(org.apache.spark.sql.functions.explode(ctxt_a_ds.col("ctxt")).alias("ctxt")).as(Encoders.STRING()).javaRDD();
        JavaRDD<String> ctxt_b_rdd = ctxt_b_ds.select(org.apache.spark.sql.functions.explode(ctxt_b_ds.col("ctxt")).alias("ctxt")).as(Encoders.STRING()).javaRDD();

        // causes n = slice tasks to be started using NODE_LOCAL data locality.
        JavaRDD<String> ctxt_a_rdd2 = ctxt_a_rdd.repartition(slices);
        JavaRDD<String> ctxt_b_rdd2 = ctxt_b_rdd.repartition(slices);
        System.out.println("Partitions:"+ctxt_a_rdd2.partitions().size());

        // combine both RDDs as pairs
        JavaPairRDD<String, String> combined_ctxt_rdd = ctxt_a_rdd2.zip(ctxt_b_rdd2);

        // perform the multiply operator on each of the pairs
        JavaRDD<String> result_rdd = combined_ctxt_rdd.map(tuple -> {
            return SparkFHE.getInstance().do_FHE_basic_op(tuple._1(), tuple._2(), SparkFHE.FHE_MULTIPLY);
        });

        // sum up the results from the previous operation and display
        System.out.println("Dot product: " + SparkFHE.getInstance().decrypt(result_rdd.reduce((x, y) -> {
            return SparkFHE.getInstance().do_FHE_basic_op(x, y, SparkFHE.FHE_ADD);
        })));
    }

    /**
     This method performs the dot product operation on cipher-text vectors and print out the results naively
     * @param spark the spark session which allows the creation of the various data abstractions such
     *              as RDDs, DataFrame, and more.
     * @param slices the number of time a task is split up
     */
    public static void test_FHE_dot_product_via_native_code(SparkSession spark, int slices) {
        System.out.println("test_FHE_dot_product_via_native_code");
        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<CtxtString> ctxtJSONEncoder = Encoders.bean(CtxtString.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        // Create dataset with json file.
        // if CtxtString a row? Dataset<Row> is the Dataframe in Java
        Dataset<CtxtString> ctxt_a_ds = spark.read().json(vec_a_ctxt).as(ctxtJSONEncoder);
        Dataset<CtxtString> ctxt_b_ds = spark.read().json(vec_b_ctxt).as(ctxtJSONEncoder);

        // Represents the content of the DataFrame as an RDD of Rows
        JavaRDD<String> ctxt_a_rdd = ctxt_a_ds.select(org.apache.spark.sql.functions.explode(ctxt_a_ds.col("ctxt")).alias("ctxt")).as(Encoders.STRING()).javaRDD();
        JavaRDD<String> ctxt_b_rdd = ctxt_b_ds.select(org.apache.spark.sql.functions.explode(ctxt_b_ds.col("ctxt")).alias("ctxt")).as(Encoders.STRING()).javaRDD();

        // print out the cipher text vectors after decryption for verification purposes
        System.out.println("ctxt_a_rdd.count() = " + ctxt_a_rdd.count());
        ctxt_a_rdd.foreach(data -> {
            System.out.println(SparkFHE.getInstance().decrypt(data));
        });
        System.out.println("ctxt_b_rdd.count() = " + ctxt_b_rdd.count());
        ctxt_b_rdd.foreach(data -> {
            System.out.println(SparkFHE.getInstance().decrypt(data));
        });

        // combine both rdds as a pair
        JavaPairRDD<String, String> combined_ctxt_rdd = ctxt_a_rdd.zip(ctxt_b_rdd);
        combined_ctxt_rdd.repartition(slices);
        System.out.println("combined_ctxt_rdd.count() = " + combined_ctxt_rdd.count());

        // call homomorphic doc product operators on the rdds
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

        // reset the collection and display the output
        collection.cache();
        System.out.println("collection.count() = " + collection.count());

        // sum up the results from the previous operation and display
        String res = collection.reduce((x, y) -> {
            return SparkFHE.getInstance().do_FHE_basic_op(x, y, SparkFHE.FHE_ADD);
        });

        // decrypt the result and verify it
        System.out.println("Dot product: " + SparkFHE.getInstance().decrypt(res));
    }


    /**
     This method performs the dot product operation on cipher-text vectors and print out the results as SparkSQL
     * @param spark the spark session which allows the creation of the various data abstractions such
     *              as RDDs, DataFrame, and more.
     * @param slices the number of time a task is split up
     */
    public static void test_FHE_dot_product_via_sql(SparkSession spark, int slices) {
        System.out.println("test_FHE_dot_product_via_sql");
        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<CtxtString> ctxtJSONEncoder = Encoders.bean(CtxtString.class);
        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations\
        // READ as a dataset
        Dataset<CtxtString> ctxt_a_ds = spark.read().json(vec_a_ctxt).as(ctxtJSONEncoder);
        Dataset<CtxtString> ctxt_b_ds = spark.read().json(vec_b_ctxt).as(ctxtJSONEncoder);

        // col - Returns a Column based on the given column name, ctxt.
        // explode - Creates a new row for each element in the given array or map column
        // select - select the newly created column, and alias it accordingly
        Dataset<String> ctxt_a_ds2 = ctxt_a_ds.select(org.apache.spark.sql.functions.explode(ctxt_a_ds.col("ctxt")).alias("ctxt")).as(Encoders.STRING());
        Dataset<String> ctxt_b_ds2 = ctxt_b_ds.select(org.apache.spark.sql.functions.explode(ctxt_b_ds.col("ctxt")).alias("ctxt2")).as(Encoders.STRING());

        // withColumn - create a new DataFrame with a column added or renamed.
        // monotonically_increasing_id - A column that generates monotonically increasing 64-bit integers
        Dataset<Row> ctxt_a_ds3 = ctxt_a_ds2.withColumn("id", functions.monotonically_increasing_id());
        // orderBy - creates a window specification that defines the partitioning, ordering, and frame boundaries with
        // the ordering defined. add this column as "id" and same it to the dataFrame ctxt_a_ds3
        ctxt_a_ds3 = ctxt_a_ds3.withColumn("id", functions.row_number().over(Window.orderBy("id")));

        // repeat the process for the other cipher text
        Dataset<Row> ctxt_b_ds3 = ctxt_b_ds2.withColumn("id", functions.monotonically_increasing_id());
        ctxt_b_ds3 = ctxt_b_ds3.withColumn("id", functions.row_number().over(Window.orderBy("id")));

        // join - cartesian join between ctxt_a_ds3's id column and ctxt_b_ds3
        // equalTo - A filter that evaluates to true iff the attribute evaluates to a value equal to value.
        Dataset<Row> joined = ctxt_a_ds3.join(ctxt_b_ds3, ctxt_a_ds3.col("id").equalTo(ctxt_b_ds3.col("id")));
        Dataset<Row> fin = joined.select(col("ctxt"), col("ctxt2"));
        fin.repartition(slices);

        fin.printSchema();

        StructType structType = new StructType();
        // Creates a new StructType by adding a new field with no metadata where the dataType is specified as a String.
        structType = structType.add("ctxt", DataTypes.StringType, false);
        structType = structType.add("ctxt2", DataTypes.StringType, false);

        // RowEncoder - is part of the Encoder framework and acts as the encoder for DataFrames, i.e. Dataset[Row] 
        // — Datasets of Rows.
        // ExpressionEncoder[T] - is a generic Encoder of JVM objects of the type T to and from internal binary rows.
        ExpressionEncoder<Row> encoder = RowEncoder.apply(structType);
        ExpressionEncoder<Row> encoder2 = RowEncoder.apply(structType);

        // mapPartition - converts each partition of the source RDD into multiple elements of the result
        // perform dot product on each pair (StringVector) of the dataFrame, and saving the results to a LinkedList
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

        // reset the collection and display the output
        collection.cache();

        System.out.println("collection.count() = " + collection.count());
        collection.printSchema();

        // sum up the results from the previous operation
        String res = collection.reduce((ReduceFunction<String>) (x, y) -> {
            return SparkFHE.getInstance().do_FHE_basic_op(x, y, SparkFHE.FHE_ADD);
        });

        // decrypt the result to verify it
        System.out.println("Dot product: " + SparkFHE.getInstance().decrypt(res));
    }


    public static void main(String[] argv) {

        // The variable slices represent the number of time a task is split up
        int slices=2;

        // Create a SparkConf that loads defaults from system properties and the classpath
        SparkConf sparkConf;

        // Loading the C++ library
        SparkFHESetup.setup();

        // Decide whether to run the task locally or on the clusters
        if( "local".equalsIgnoreCase(argv[0]) ) {
            //Provides the Spark driver application a name for easy identification in the Spark or Yarn UI
            //Setting the master URL, in this case its local with 1 thread
            sparkConf = new SparkConf().setAppName("DotProductExample").setMaster("local");
        } else {
            slices=Integer.parseInt(argv[0]);
            //Provides the Spark driver application a name for easy identification in the Spark or Yarn UI
            sparkConf = new SparkConf().setAppName("DotProductExample");
        }

        // Creating a session to Spark. The session allows the creation of the
        // various data abstractions such as RDDs, DataFrame, and more.
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();

        // Creating spark context which allows the communication with worker nodes
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        // read in the public and secret key and their associated files from terminal arguments
        String pk = argv[1];
        String sk = argv[2];
        vec_a_ctxt = argv[3]; // Config.DEFAULT_RECORDS_DIRECTORY+"/vec_a_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+".json";
        vec_b_ctxt = argv[4]; // Config.DEFAULT_RECORDS_DIRECTORY+"/vec_b_"+String.valueOf(Config.NUM_OF_VECTOR_ELEMENTS)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+".json";

        // Create SparkFHE object with HElib, a library that implements homomorphic encryption
        SparkFHE.init(FHELibrary.HELIB, pk, sk);

        // testing the dot product operation in Helib on plaintext vector.
        test_basic_dot_product(jsc, slices);

        // testing the dot product operation in Helib on cipher text vector.
        test_FHE_dot_product_via_lambda(spark, slices);


        test_FHE_dot_product_via_native_code(spark, slices);


        test_FHE_dot_product_via_sql(spark, slices);

        // Stop existing spark context
        jsc.close();

        // Stop existing spark session
        spark.close();
    }

}
