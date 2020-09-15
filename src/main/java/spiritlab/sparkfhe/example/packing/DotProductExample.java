//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.packing;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.api.java.function.MapPartitionsFunction;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.spiritlab.sparkfhe.SparkFHEPlugin;
import org.apache.spark.sql.*;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;
import spiritlab.sparkfhe.api.*;
import spiritlab.sparkfhe.example.Config;

import java.io.IOException;
import java.util.*;
import static org.apache.spark.sql.functions.col;

/**
 * This is an example for SparkFHE project. Created to test the functionality
 * of the doc product operation on both plaintext and cipher-text, in local and distributed environment.
 */
public class DotProductExample {

    // declare variables to hold cipher-text
    private static String CTXT_Vector_a_FILE;
    private static String CTXT_Vector_b_FILE;

    /**
     * This method performs the dot product operation on plaintext vectors and print out the results
     * @param jsc spark context which allows the communication with worker nodes
     * @param slices the number of time a task is split up
     */
    public static void test_basic_dot_product(JavaSparkContext jsc, int slices) {
        System.out.println("test_basic_dot_product");

        // distribute a local Scala collection (lists in this case) to form 2 RDDs
        JavaRDD A = jsc.parallelize(Arrays.asList(0,1,2,3,4), slices);
        JavaRDD B = jsc.parallelize(Arrays.asList(4,3,2,1,0), slices);

        // combine both RDDs as pairs
        JavaPairRDD<Integer, Integer> Combined_RDD = A.zip(B);

        /* print values */
        Combined_RDD.foreach(data -> {
            System.out.println("Combined_RDD: ("+data._1 +","+ data._2+")");
        });

        // perform the multiply operator on each of the pairs
        JavaRDD<Integer> Result_RDD = Combined_RDD.map(tuple -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
//            SparkFHEPlugin.setup();
            return SparkFHE.do_basic_op(tuple._1(), tuple._2(), SparkFHE.MUL);
        });

        // sum up the results from the previous operation and display
        System.out.println("(Plaintext) Result_RDD:"+Result_RDD.reduce((x, y) -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
//            SparkFHEPlugin.setup();
            return SparkFHE.do_basic_op(x, y, SparkFHE.ADD);
        }));
    }

    /**
     * This method performs the dot product operation on cipher-text vectors and print out the results
     * @param spark the spark session which allows the creation of the various data abstractions such
     *              as RDDs, DataFrame, and more.
     * @param slices the number of time a task is split up
     * @param library the HE library name
     * @param scheme  the HE scheme name
     * @param pk_b broadcast variable for public key
     * @param sk_b broadcast variable for secret key
     * @param rlk_b broadcast variable for relin keys
     * @param glk_b boradcast variable for galois keys
     */
    public static void test_FHE_dot_product_via_lambda(SparkSession spark, int slices, String library, String scheme, Broadcast<String> pk_b,
                                                       Broadcast<String> sk_b, Broadcast<String> rlk_b, Broadcast<String> glk_b) {
        System.out.println("test_FHE_dot_product_via_lambda");

        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<SerializedCiphertext> ctxtJSONEncoder = Encoders.bean(SerializedCiphertext.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        // Create rdd with json line file. See http://jsonlines.org
        JavaRDD<SerializedCiphertext> ctxt_vec_a_rdd = spark.read().json(CTXT_Vector_a_FILE).as(ctxtJSONEncoder).javaRDD();
        JavaRDD<SerializedCiphertext> ctxt_vec_b_rdd = spark.read().json(CTXT_Vector_b_FILE).as(ctxtJSONEncoder).javaRDD();

        // combine both RDDs as pairs
        JavaPairRDD<SerializedCiphertext, SerializedCiphertext> combined_ctxt_rdd = ctxt_vec_a_rdd.zip(ctxt_vec_b_rdd);

        long startTime, endTime;
        startTime = System.currentTimeMillis();
        // perform the multiply operator on each of the pairs
        JavaRDD<SerializedCiphertext> result_rdd = combined_ctxt_rdd.map(tuple -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
//            SparkFHEPlugin.setup();
//            SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());
            return new SerializedCiphertext(SparkFHE.getInstance().fhe_multiply(tuple._1().getCtxt(), tuple._2().getCtxt()));
        });

        // sum up the results from the previous operation
        result_rdd.reduce((x, y) -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
//            SparkFHEPlugin.setup();
//            SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());
            return new SerializedCiphertext(SparkFHE.getInstance().fhe_add(x.getCtxt(), y.getCtxt()));
        });

        // sum up all slots of the result
        Ciphertext dot_product_ctxt = new Ciphertext(SparkFHE.getInstance().fhe_total_sum(result_rdd.first().getCtxt())); // SparkFHE.getInstance().fhe_total_sum(
        endTime = System.currentTimeMillis();
        System.out.println("TIMEINFO:batch_FHE_dot_product_via_lambda:" + (endTime - startTime) + ":ms");

        if (Config.DEBUG) {
            // causes n = slice tasks to be started using NODE_LOCAL data locality.
            System.out.println("Partitions:"+ctxt_vec_b_rdd.partitions().size());

            // display results for debugging purposes
            Util.decrypt_and_print(scheme, "Dot product result", dot_product_ctxt, false, 0);
        }
    }

    /**
     This method performs the dot product operation on cipher-text vectors and print out the results naively
     * @param spark the spark session which allows the creation of the various data abstractions such
     *              as RDDs, DataFrame, and more.
     * @param slices the number of time a task is split up
     * @param library the HE library name
     * @param scheme  the HE scheme name
     * @param pk_b broadcast variable for public key
     * @param sk_b broadcast variable for secret key
     * @param rlk_b broadcast variable for relin keys
     * @param glk_b boradcast variable for galois keys
     */
    public static void test_FHE_dot_product_via_native_code(SparkSession spark, int slices, String library, String scheme, Broadcast<String> pk_b,
                                                            Broadcast<String> sk_b, Broadcast<String> rlk_b, Broadcast<String> glk_b) {
        System.out.println("test_FHE_dot_product_via_native_code");
        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<SerializedCiphertext> ctxtJSONEncoder = Encoders.bean(SerializedCiphertext.class);

        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations
        // Create dataset with json file. See http://jsonlines.org
        JavaRDD<SerializedCiphertext> ctxt_vec_a_rdd = spark.read().json(CTXT_Vector_a_FILE).as(ctxtJSONEncoder).javaRDD();
        JavaRDD<SerializedCiphertext> ctxt_vec_b_rdd = spark.read().json(CTXT_Vector_b_FILE).as(ctxtJSONEncoder).javaRDD();

        // combine both rdds as a pair
        JavaPairRDD<SerializedCiphertext, SerializedCiphertext> combined_ctxt_rdd = ctxt_vec_a_rdd.zip(ctxt_vec_b_rdd);

        long startTime, endTime;
        startTime = System.currentTimeMillis();
        // call homomorphic doc product operators on the rdds
        JavaRDD<SerializedCiphertext> collection = combined_ctxt_rdd.mapPartitions(records -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
//            SparkFHEPlugin.setup();
//            SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());

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

        // sum up the results from the previous operation and display
        SerializedCiphertext res = collection.reduce((x, y) -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
//            SparkFHEPlugin.setup();
//            SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());
            return new SerializedCiphertext(SparkFHE.getInstance().fhe_add(x.getCtxt(), y.getCtxt()));
        });

        // sum up all slots of the result
        Ciphertext dot_product_ctxt = new Ciphertext(SparkFHE.getInstance().fhe_total_sum(res.getCtxt()));
        endTime = System.currentTimeMillis();
        System.out.println("TIMEINFO:batch_FHE_dot_product_via_native_code:" + (endTime - startTime) + ":ms");

        if (Config.DEBUG) {
            // print out the cipher text vectors after decryption for verification purposes
            System.out.println("ctxt_a_rdd.count() = " + ctxt_vec_a_rdd.count());
            ctxt_vec_a_rdd.foreach(data -> {
                // we need to load the shared library and init a copy of SparkFHE on the executor
//                SparkFHEPlugin.setup();
//                SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());
                Util.decrypt_and_print(scheme, "", new Ciphertext(data.getCtxt()), true, 100);
            });
            System.out.println("ctxt_b_rdd.count() = " + ctxt_vec_b_rdd.count());
            ctxt_vec_b_rdd.foreach(data -> {
                // we need to load the shared library and init a copy of SparkFHE on the executor
//                SparkFHEPlugin.setup();
//                SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());
                Util.decrypt_and_print(scheme, "", new Ciphertext(data.getCtxt()), true, 100);
            });

            System.out.println("combined_ctxt_rdd.count() = " + combined_ctxt_rdd.count());

            Util.decrypt_and_print(scheme, "Dot product", dot_product_ctxt, false, 0);
        }
    }


    /**
     This method performs the dot product operation on cipher-text vectors and print out the results as SparkSQL
     * @param spark the spark session which allows the creation of the various data abstractions such
     *              as RDDs, DataFrame, and more.
     * @param slices the number of time a task is split up
     * @param library the HE library name
     * @param scheme  the HE scheme name
     * @param pk_b broadcast variable for public key
     * @param sk_b broadcast variable for secret key
     * @param rlk_b broadcast variable for relin keys
     * @param glk_b boradcast variable for galois keys
     */
    public static void test_FHE_dot_product_via_sql(SparkSession spark, int slices, String library, String scheme, Broadcast<String> pk_b,
                                                    Broadcast<String> sk_b, Broadcast<String> rlk_b, Broadcast<String> glk_b) {
        System.out.println("test_FHE_dot_product_via_sql");
        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<SerializedCiphertext> ctxtJSONEncoder = Encoders.bean(SerializedCiphertext.class);
        // https://spark.apache.org/docs/latest/sql-programming-guide.html#untyped-dataset-operations-aka-dataframe-operations\
        // Create a dataset from a json line file. See http://jsonlines.org
        Dataset<SerializedCiphertext> ctxt_a_ds = spark.read().json(CTXT_Vector_a_FILE).as(ctxtJSONEncoder);
        Dataset<SerializedCiphertext> ctxt_b_ds = spark.read().json(CTXT_Vector_b_FILE).as(ctxtJSONEncoder);

        // col - Returns a Column based on the given column name, ctxt.
        // explode - Creates a new row for each element in the given array or map column
        // select - select the newly created column, and alias it accordingly
        Dataset<String> ctxt_a_ds2 = ctxt_a_ds.select(ctxt_a_ds.col("ctxt").as("ctxt_a")).as(Encoders.STRING());
        Dataset<String> ctxt_b_ds2 = ctxt_b_ds.select(ctxt_b_ds.col("ctxt").as("ctxt_b")).as(Encoders.STRING());

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
        Dataset<Row> fin = joined.select(col("ctxt_a"), col("ctxt_b"));

        fin.printSchema();

        StructType structType = new StructType();
        // Creates a new StructType by adding a new field with no metadata where the dataType is specified as a String.
        structType = structType.add("ctxt_a", DataTypes.StringType, false);
        structType = structType.add("ctxt_b", DataTypes.StringType, false);

        // RowEncoder - is part of the Encoder framework and acts as the encoder for DataFrames, i.e. Dataset[Row] 
        // — Datasets of Rows.
        // ExpressionEncoder[T] - is a generic Encoder of JVM objects of the type T to and from internal binary rows.
        ExpressionEncoder<Row> encoder = RowEncoder.apply(structType);
        ExpressionEncoder<Row> encoder2 = RowEncoder.apply(structType);

        // mapPartition - converts each partition of the source RDD into multiple elements of the result
        // perform dot product on each pair (StringVector) of the dataFrame, and saving the rcesults to a LinkedList
        Dataset<SerializedCiphertext> collection = fin.mapPartitions((MapPartitionsFunction<Row, SerializedCiphertext>)  iter -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
//            SparkFHEPlugin.setup();
//            SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());

            LinkedList<SerializedCiphertext> v = new LinkedList<SerializedCiphertext>();
            StringVector a = new StringVector();
            StringVector b = new StringVector();
            while (iter.hasNext()) {
                Row row = iter.next();
                a.add((String)row.getAs("ctxt_a"));
                b.add((String)row.getAs("ctxt_b"));
            }
            v.add(new SerializedCiphertext(SparkFHE.getInstance().do_FHE_dot_product(a, b)));
            return v.iterator();
        }, Encoders.kryo(SerializedCiphertext.class));

        // sum up the results from the previous operation and display
        SerializedCiphertext res = collection.javaRDD().reduce((x, y) -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
//            SparkFHEPlugin.setup();
//            SparkFHE.init(library, scheme, pk_b.getValue(), sk_b.getValue(), rlk_b.getValue(), glk_b.getValue());
            return new SerializedCiphertext(SparkFHE.getInstance().fhe_add(x.getCtxt(), y.getCtxt()));
        });

        // sum up all slots of the result and display
        Ciphertext dot_product_ctxt = new Ciphertext(SparkFHE.getInstance().fhe_total_sum(res.getCtxt()));
        if (Config.DEBUG) {
            Util.decrypt_and_print(scheme, "Dot product", dot_product_ctxt, false, 0);
        }
    }


    public static void main(String[] args) {
        String scheme="", library = "", pk="", sk="", rlk="", glk="", master="";

        // The variable slices represent the number of time a task is split up
        int slices=2;

        // Create a SparkConf that loads defaults from system properties and the classpath
        SparkConf sparkConf = new SparkConf();
        //Provides the Spark driver application a name for easy identification in the Spark or Yarn UI
        sparkConf.setAppName("DotProductWithBatchingExample");

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
                if (args[1] == "") {
                    master = "local";
                } else {
                    master = "local["+args[1]+"]";
                }
                sparkConf.setMaster(master);
                library = args[2];
                scheme = args[3];
                pk = args[4];
                sk = args[5];
                if (library.equalsIgnoreCase(FHELibrary.SEAL)){
                    rlk = args[6];
                    glk = args[7];
                }
                break;
            default:
                break;
        }
        System.out.println("CURRENT_DIRECTORY = "+Config.get_current_directory());

        // Creating a session to Spark. The session allows the creation of the
        // various data abstractions such as RDDs, DataFrame, and more.
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        spark.sparkContext().setLogLevel("ERROR");

        // Creating spark context which allows the communication with worker nodes
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        // required to load our shared library
        SparkFHEPlugin.setup();
        // create SparkFHE object
        SparkFHE.init(library, scheme, pk, sk, rlk, glk);

        Broadcast<String> pk_b = jsc.broadcast(pk);
        Broadcast<String> sk_b = jsc.broadcast(sk);
        Broadcast<String> rlk_b = jsc.broadcast(rlk);
        Broadcast<String> glk_b = jsc.broadcast(glk);

        // set ctxt file names
        CTXT_Vector_a_FILE = Config.get_records_directory()+"/packed_vec_a_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";
        CTXT_Vector_b_FILE = Config.get_records_directory()+"/packed_vec_b_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";

        // testing the dot product operation on plaintext vector.
        test_basic_dot_product(jsc, slices);

//        long main_startTime, main_endTime;
         // testing the dot product operation in HE libraries on cipher text vector.
//        main_startTime = System.currentTimeMillis();
        test_FHE_dot_product_via_lambda(spark, slices, library, scheme, pk_b, sk_b, rlk_b, glk_b);
//        main_endTime = System.currentTimeMillis();
//        System.out.println("TIMEINFO:batch_total_Spark_dot_product_job_via_lambda:" + (main_endTime - main_startTime) + ":ms");

//        main_startTime = System.currentTimeMillis();
        test_FHE_dot_product_via_native_code(spark, slices, library, scheme, pk_b, sk_b, rlk_b, glk_b);
//        main_endTime = System.currentTimeMillis();
//        System.out.println("TIMEINFO:batch_total_Spark_dot_product_job_via_native_code:" + (main_endTime - main_startTime) + ":ms");

//        main_startTime = System.currentTimeMillis();
        test_FHE_dot_product_via_sql(spark, slices, library, scheme, pk_b, sk_b, rlk_b, glk_b);
//        main_endTime = System.currentTimeMillis();
//        System.out.println("TIMEINFO:batch_total_Spark_dot_product_job_via_sql:" + (main_endTime - main_startTime) + ":ms");

//        try {
//            System.out.println("Paused to allow checking the Spark server log, press enter to continue.");
//            System.in.read();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

        // Stop existing spark context
        jsc.close();

        // Stop existing spark session
        spark.close();
    }

}
