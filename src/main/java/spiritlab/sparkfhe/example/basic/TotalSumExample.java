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
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.spiritlab.sparkfhe.SparkFHEPlugin;
import org.apache.spark.sql.*;
import org.apache.spark.sql.catalyst.encoders.ExpressionEncoder;
import org.apache.spark.sql.catalyst.encoders.RowEncoder;
import org.apache.spark.sql.expressions.Window;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructType;
import scala.Tuple2;
import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.SerializedCiphertextObject;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.api.StringVector;
import spiritlab.sparkfhe.example.Config;

import java.util.Arrays;
import java.util.LinkedList;

import static org.apache.spark.sql.functions.col;

/**
 * This is an example for SparkFHE project. Created to test the functionality
 * of the doc product operation on both plaintext and cipher-text, in local and distributed environment.
 */
public class TotalSumExample {

    // declare variable to hold a ciphertext vector
    private static String ctxt_vec;

    /**
     * This method performs the total sum operation on a plaintext vector and print out the result
     * @param jsc spark context which allows the communication with worker nodes
     * @param slices the number of time a task is split up
     * @param pk_b broadcast variable for public key
     * @param sk_b broadcast variable for secret key
     */
    public static void test_basic_total_sum(JavaSparkContext jsc, int slices, Broadcast<String> pk_b, Broadcast<String> sk_b) {
        System.out.println("test_basic_total_sum");

        // distribute a local Scala collection (lists in this case) to form 2 RDDs
        JavaRDD<Integer> values_RDD = jsc.parallelize(Arrays.asList(0,1,2,3,4,5,6,7,8,9), slices);

        // sum up the values and display
        System.out.println("values_RDD:"+values_RDD.reduce((x, y) -> {
            // we need to load the shared library and init a copy of SparkSEAL on the executor
            SparkFHEPlugin.setup();
            return SparkFHE.do_basic_op(x, y, SparkFHE.ADD);
        }));

    }

    /**
     * This method performs the total sum operation on a ciphertext vector and print out the result
     * @param spark the spark session which allows the creation of the various data abstractions such
     *              as RDDs, DataFrame, and more.
     * @param slices the number of time a task is split up
     * @param pk_b broadcast variable for public key
     * @param sk_b broadcast variable for secret key
     */
    public static void test_FHE_total_sum_via_lambda(SparkSession spark, int slices, Broadcast<String> pk_b, Broadcast<String> sk_b) {
        System.out.println("test_FHE_total_sum_via_lambda");

        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<SerializedCiphertextObject> ctxtJSONEncoder = Encoders.bean(SerializedCiphertextObject.class);
        Dataset<SerializedCiphertextObject> serialized_ctxt_vec_ds = spark.read().json(ctxt_vec).as(ctxtJSONEncoder);


        JavaRDD<String> ctxt_vec_rdd = serialized_ctxt_vec_ds.select(serialized_ctxt_vec_ds.col("ctxt")).as(Encoders.STRING()).javaRDD();

        // causes n = slice tasks to be started using NODE_LOCAL data locality.
        JavaRDD<SerializedCiphertextObject> ctxt_vec_rdd2 = ctxt_vec_rdd.map(x -> new SerializedCiphertextObject(x));
        System.out.println("Partitions:"+ctxt_vec_rdd2.partitions().size());


        // sum up the results from the previous operation and display
        System.out.println("Total Sum: " + SparkFHE.getInstance().decrypt(ctxt_vec_rdd2.reduce((x, y) -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
            SparkFHEPlugin.setup();
            SparkFHE.init(FHELibrary.SEAL,  pk_b.getValue(), sk_b.getValue());
            return new SerializedCiphertextObject(SparkFHE.getInstance().do_FHE_basic_op(x.getCtxt(), y.getCtxt(), SparkFHE.FHE_ADD));
        }).getCtxt()));
    }


    /**
     This method performs the total sum operation on a ciphertext vector and print out the result naively
     * @param spark the spark session which allows the creation of the various data abstractions such
     *              as RDDs, DataFrame, and more.
     * @param slices the number of time a task is split up
     * @param pk_b broadcast variable for public key
     * @param sk_b broadcast variable for secret key
     */
    public static void test_FHE_total_sum_via_native_code(SparkSession spark, int slices, Broadcast<String> pk_b, Broadcast<String> sk_b) {
        System.out.println("test_FHE_total_sum_via_native_code");
        /* Spark example for FHE calculations */
        // Encoders are created for Java beans
        Encoder<SerializedCiphertextObject> ctxtJSONEncoder = Encoders.bean(SerializedCiphertextObject.class);

        Dataset<SerializedCiphertextObject> serialized_ctxt_vec_ds = spark.read().json(ctxt_vec).as(ctxtJSONEncoder);
        JavaRDD<SerializedCiphertextObject> ctxt_vec_rdd = serialized_ctxt_vec_ds.select(serialized_ctxt_vec_ds.col("ctxt")).as(Encoders.STRING()).javaRDD().map(x -> new SerializedCiphertextObject(x));

        // print out the cipher text vectors after decryption for verification purposes
        System.out.println("ctxt_vec_rdd.count() = " + ctxt_vec_rdd.count());

        ctxt_vec_rdd.foreach(data -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
            SparkFHEPlugin.setup();
            SparkFHE.init(FHELibrary.SEAL,  pk_b.getValue(), sk_b.getValue());
            System.out.println(SparkFHE.getInstance().decrypt(data.getCtxt()));
        });


        // call homomorphic array sum operator on the rdd
        JavaRDD<SerializedCiphertextObject> collection = ctxt_vec_rdd.mapPartitions(records -> {
            // we need to load the shared library and init a copy of SparkSEAL on the executor
            SparkFHEPlugin.setup();
            SparkFHE.init(FHELibrary.SEAL,  pk_b.getValue(), sk_b.getValue());

            LinkedList<SerializedCiphertextObject> sum = new LinkedList<SerializedCiphertextObject>();
            StringVector vec = new StringVector();
            while (records.hasNext()) {
                SerializedCiphertextObject rec = records.next();
                vec.add(rec.getCtxt());
            }
            sum.add(new SerializedCiphertextObject(SparkFHE.getInstance().do_FHE_array_sum(vec)));
            return sum.iterator();
        });

        // sum up the results from the previous operation and display
        SerializedCiphertextObject res = collection.reduce((x, y) -> {
            // we need to load the shared library and init a copy of SparkFHE on the executor
            SparkFHEPlugin.setup();
            SparkFHE.init(FHELibrary.SEAL,  pk_b.getValue(), sk_b.getValue());
            return new SerializedCiphertextObject(SparkFHE.getInstance().do_FHE_basic_op(x.getCtxt(), y.getCtxt(), SparkFHE.FHE_ADD));
        });

        // decrypt the result and verify it
        System.out.println("Total sum: " + SparkFHE.getInstance().decrypt(res.getCtxt()));
    }


    /**
     This method performs the Total sum operation on cipher-text vectors and print out the results as SparkSQL
     * @param spark the spark session which allows the creation of the various data abstractions such
     *              as RDDs, DataFrame, and more.
     * @param slices the number of time a task is split up
     * @param pk_b broadcast variable for public key
     * @param sk_b broadcast variable for secret key
     */
    public static void test_FHE_total_sum_via_sql(SparkSession spark, int slices, Broadcast<String> pk_b, Broadcast<String> sk_b) {
//        System.out.println("test_FHE_total_sum_via_sql");
//        /* Spark example for FHE calculations */
//        // Encoders are created for Java beans
//        Encoder<SerializedCiphertextObject> ctxtJSONEncoder = Encoders.bean(SerializedCiphertextObject.class);
//        Dataset<SerializedCiphertextObject> ctxt_vec_ds = spark.read().json(ctxt_vec).as(ctxtJSONEncoder);
//
//        // col - Returns a Column based on the given column name, ctxt.
//        // explode - Creates a new row for each element in the given array or map column
//        // select - select the newly created column, and alias it accordingly
//        Dataset<String> ctxt_vec_ds2 = ctxt_vec_ds.select(ctxt_vec_ds.col("ctxt").as("ctxt_a")).as(Encoders.STRING());
//        Dataset<String> ctxt_b_ds2 = ctxt_b_ds.select(ctxt_b_ds.col("ctxt").as("ctxt_b")).as(Encoders.STRING());
//
//        // withColumn - create a new DataFrame with a column added or renamed.
//        // monotonically_increasing_id - A column that generates monotonically increasing 64-bit integers
//        Dataset<Row> ctxt_a_ds3 = ctxt_a_ds2.withColumn("id", functions.monotonically_increasing_id());
//        // orderBy - creates a window specification that defines the partitioning, ordering, and frame boundaries with
//        // the ordering defined. add this column as "id" and same it to the dataFrame ctxt_a_ds3
//        ctxt_a_ds3 = ctxt_a_ds3.withColumn("id", functions.row_number().over(Window.orderBy("id")));
//
//        // repeat the process for the other cipher text
//        Dataset<Row> ctxt_b_ds3 = ctxt_b_ds2.withColumn("id", functions.monotonically_increasing_id());
//        ctxt_b_ds3 = ctxt_b_ds3.withColumn("id", functions.row_number().over(Window.orderBy("id")));
//
//        // join - cartesian join between ctxt_a_ds3's id column and ctxt_b_ds3
//        // equalTo - A filter that evaluates to true iff the attribute evaluates to a value equal to value.
//        Dataset<Row> joined = ctxt_a_ds3.join(ctxt_b_ds3, ctxt_a_ds3.col("id").equalTo(ctxt_b_ds3.col("id")));
//        Dataset<Row> fin = joined.select(col("ctxt_a"), col("ctxt_b"));
//
//        fin.printSchema();
//
//        StructType structType = new StructType();
//        // Creates a new StructType by adding a new field with no metadata where the dataType is specified as a String.
//        structType = structType.add("ctxt_a", DataTypes.StringType, false);
//        structType = structType.add("ctxt_b", DataTypes.StringType, false);
//
//        // RowEncoder - is part of the Encoder framework and acts as the encoder for DataFrames, i.e. Dataset[Row] 
//        // — Datasets of Rows.
//        // ExpressionEncoder[T] - is a generic Encoder of JVM objects of the type T to and from internal binary rows.
//        ExpressionEncoder<Row> encoder = RowEncoder.apply(structType);
//        ExpressionEncoder<Row> encoder2 = RowEncoder.apply(structType);
//
//        // mapPartition - converts each partition of the source RDD into multiple elements of the result
//        // perform dot product on each pair (StringVector) of the dataFrame, and saving the rcesults to a LinkedList
//        Dataset<SerializedCiphertextObject> collection = fin.mapPartitions((MapPartitionsFunction<Row, SerializedCiphertextObject>)  iter -> {
//            // we need to load the shared library and init a copy of SparkFHE on the executor
//            SparkFHEPlugin.setup();
//            SparkFHE.init(FHELibrary.SEAL,  pk_b.getValue(), sk_b.getValue());
//
//            LinkedList<SerializedCiphertextObject> v = new LinkedList<SerializedCiphertextObject>();
//            StringVector a = new StringVector();
//            StringVector b = new StringVector();
//            while (iter.hasNext()) {
//                Row row = iter.next();
//                a.add((String)row.getAs("ctxt_a"));
//                b.add((String)row.getAs("ctxt_b"));
//            }
//            v.add(new SerializedCiphertextObject(SparkFHE.getInstance().do_FHE_dot_product(a, b)));
//            return v.iterator();
//        }, Encoders.kryo(SerializedCiphertextObject.class));
//
//        // sum up the results from the previous operation and display
//        SerializedCiphertextObject res = collection.javaRDD().reduce((x, y) -> {
//            // we need to load the shared library and init a copy of SparkFHE on the executor
//            SparkFHEPlugin.setup();
//            SparkFHE.init(FHELibrary.SEAL,  pk_b.getValue(), sk_b.getValue());
//            return new SerializedCiphertextObject(SparkFHE.getInstance().do_FHE_basic_op(x.getCtxt(), y.getCtxt(), SparkFHE.FHE_ADD));
//        });
//
//        // decrypt the result to verify it
//        System.out.println("Dot product: " + SparkFHE.getInstance().decrypt(res.getCtxt()));
    }


    public static void main(String[] args) {
        String pk="", sk="";
        // The variable slices represent the number of time a task is split up
        int slices=2;
          
        // Create a SparkConf that loads defaults from system properties and the classpath
        SparkConf sparkConf = new SparkConf();
        //Provides the Spark driver application a name for easy identification in the Spark or Yarn UI
        sparkConf.setAppName("TotalSumExample");

        // Decide whether to run the task locally or on the clusters
        Config.setExecutionEnvironment(args[0]);
        switch (Config.currentExecutionEnvironment) {
            case CLUSTER:
                slices = Integer.parseInt(args[0]);
                Config.set_HDFS_NAME_NODE(args[1]);
                pk = args[2];
                sk = args[3];
                break;
            case LOCAL:
                sparkConf.setMaster("local");
                pk = args[1];
                sk = args[2];
                break;
            default:
                break;
        }
        System.out.println("CURRENT_DIRECTORY = "+Config.get_current_directory());

        // Creating a session to Spark. The session allows the creation of the
        // various data abstractions such as RDDs, DataFrame, and more.
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();

        // Creating spark context which allows the communication with worker nodes
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        // required to load our shared library
        SparkFHEPlugin.setup();
        // create SparkFHE object
        SparkFHE.init(FHELibrary.SEAL, pk, sk);

        ctxt_vec = Config.get_records_directory()+"/vec_ctxt_"+String.valueOf(10)+"_"+SparkFHE.getInstance().generate_crypto_params_suffix()+".jsonl";

        Broadcast<String> pk_b = jsc.broadcast(pk);
        Broadcast<String> sk_b = jsc.broadcast(sk);

        // testing the total sum operation in SEAL on plaintext vector.
        test_basic_total_sum(jsc, slices, pk_b, sk_b);
      
         // testing the total sum operation in SEAL on cipher text vector.
        test_FHE_total_sum_via_lambda(spark, slices, pk_b, sk_b);
        test_FHE_total_sum_via_native_code(spark, slices, pk_b, sk_b);
//        test_FHE_total_sum_via_sql(spark, slices, pk_b, sk_b);

        // Stop existing spark context
        jsc.close();

        // Stop existing spark session
        spark.close();
    }

}
