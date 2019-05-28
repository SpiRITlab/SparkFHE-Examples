//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

//https://github.com/apache/spark/blob/master/examples/src/main/java/org/apache/spark/examples/ml/JavaElementwiseProductExample.java

package spiritlab.sparkfhe.example.ml;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.spiritlab.sparkfhe.SparkFHEPlugin;
import org.apache.spark.sql.*;

// $example on$
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.spark.ml_fhe.linalg.CtxtVectorUDT;
import org.apache.spark.mllib_fhe.linalg.CtxtVector;
import org.apache.spark.mllib_fhe.linalg.CtxtVectors;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.api.java.JavaRDD;
import scala.runtime.AbstractFunction2;
import scala.runtime.BoxedUnit;
import static spiritlab.sparkfhe.api.Ciphertext.*;

import spiritlab.sparkfhe.api.*;
import spiritlab.sparkfhe.example.Config;
// $example off$

public class ElementwiseProductExample {
    private static String CTXT_0_FILE;
    private static String CTXT_1_FILE;

    // plaintext version
    public static void RunExample(SparkSession spark) {
        // $example on$
        // Create some vector data; also works for sparse vectors
        List<Row> data = Arrays.asList(
                RowFactory.create("a", org.apache.spark.ml.linalg.Vectors.dense(1.0, 2.0, 3.0)),
                RowFactory.create("b", org.apache.spark.ml.linalg.Vectors.dense(4.0, 5.0, 6.0))
        );

        List<StructField> fields = new ArrayList<>(2);
        fields.add(DataTypes.createStructField("id", DataTypes.StringType, false));
        fields.add(DataTypes.createStructField("vector", new CtxtVectorUDT(), false));

        StructType schema = DataTypes.createStructType(fields);

        Dataset<Row> dataFrame = spark.createDataFrame(data, schema);

        dataFrame.printSchema();

        org.apache.spark.ml.linalg.Vector transformingVector = org.apache.spark.ml.linalg.Vectors.dense(0.0, 1.0, 2.0);

        org.apache.spark.ml.feature.ElementwiseProduct transformer = new org.apache.spark.ml.feature.ElementwiseProduct()
                .setScalingVec(transformingVector)
                .setInputCol("vector")
                .setOutputCol("transformedVector");

        // Batch transform the vectors to create new column:
        Dataset<Row> tr = transformer.transform(dataFrame);
        tr.show();
        System.out.println(tr.count());
        // $example off$
    }

    // ciphertext version
    public static void RunCtxtExample(SparkSession spark, int slices, Broadcast<String> pk_b, Broadcast<String> sk_b) {
        System.out.println("RunCtxtExample");
        // Create some vector data; also works for sparse vectors
        String zero_ctxt = SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label, CTXT_0_FILE);
        String one_ctxt = SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label, CTXT_1_FILE);
        List<Row> data = Arrays.asList(
                RowFactory.create("a", org.apache.spark.ml_fhe.linalg.CtxtVectors.dense(one_ctxt, zero_ctxt, one_ctxt)),
                RowFactory.create("b", org.apache.spark.ml_fhe.linalg.CtxtVectors.dense(zero_ctxt, one_ctxt, one_ctxt))
        );

        List<StructField> fields = new ArrayList<>(2);
        fields.add(DataTypes.createStructField("id", DataTypes.StringType, false));
        fields.add(DataTypes.createStructField("vector", new CtxtVectorUDT(), false));

        StructType schema = DataTypes.createStructType(fields);
        Dataset<Row> dataFrame = spark.createDataFrame(data, schema);
        dataFrame.printSchema();

        org.apache.spark.ml_fhe.linalg.CtxtVector transformingVector = org.apache.spark.ml_fhe.linalg.CtxtVectors.dense(zero_ctxt, one_ctxt, zero_ctxt);

        org.apache.spark.ml_fhe.feature.ElementwiseProduct transformer = new org.apache.spark.ml_fhe.feature.ElementwiseProduct()
                .setScalingVec(transformingVector)
                .setInputCol("vector")
                .setOutputCol("transformedVector");

        // Batch transform the vectors to create new column:
        Dataset<Row> tr = transformer.transform(dataFrame);
        tr.show();

        tr.foreach(row -> {
            org.apache.spark.ml_fhe.linalg.CtxtVector v = row.getAs("transformedVector");
            AbstractFunction2<Object, String, BoxedUnit> f = new AbstractFunction2<Object, String, BoxedUnit>() {
                public BoxedUnit apply(Object t1, String t2) {
                    System.out.println("Index:" + t1 + "      Value:" + SparkFHE.getInstance().decrypt(new Ciphertext((String)t2)));
                    return BoxedUnit.UNIT;
                }
            };
            v.foreachActive(f);
        });
    }

    // plaintext version using RDD
    public static void RunRDDExample(JavaSparkContext jsc) {

        // $example on$
        // Create some vector data; also works for sparse vectors
        JavaRDD<Vector> data = jsc.parallelize(Arrays.asList(
                Vectors.dense(1.0, 2.0, 3.0), Vectors.dense(4.0, 5.0, 6.0)));
        Vector transformingVector = Vectors.dense(0.0, 1.0, 2.0);
        org.apache.spark.mllib.feature.ElementwiseProduct transformer =
                new org.apache.spark.mllib.feature.ElementwiseProduct(transformingVector);

        // Batch transform and per-row transform give the same results:
        JavaRDD<Vector> transformedData = transformer.transform(data);
        JavaRDD<Vector> transformedData2 = data.map(x -> transformer.transform(x));
        // $example off$

        System.out.println("transformedData: ");
        transformedData.foreach(x -> System.out.println(x));

        System.out.println("transformedData2: ");
        transformedData2.foreach(x -> System.out.println(x));
    }

    // ciphertext version using RDD
    public static void RunCtxtRDDExample(JavaSparkContext jsc, int slices, Broadcast<String> pk_b, Broadcast<String> sk_b) {
        System.out.println("RunCtxtRDDExample");
        String zero_ctxt = SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label, CTXT_0_FILE);
        String one_ctxt = SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label,CTXT_1_FILE);
        JavaRDD<CtxtVector> data = jsc.parallelize(Arrays.asList(
                CtxtVectors.dense(one_ctxt, zero_ctxt, one_ctxt), CtxtVectors.dense(zero_ctxt, one_ctxt, one_ctxt)));
        CtxtVector transformingVector = CtxtVectors.dense(zero_ctxt, one_ctxt, zero_ctxt);
//        System.out.println(SparkFHE.getInstance().decrypt(transformingVector.numNonzeros()));
        org.apache.spark.mllib_fhe.feature.ElementwiseProduct transformer = new org.apache.spark.mllib_fhe.feature.ElementwiseProduct(transformingVector);

        // Batch transform and per-row transform give the same results:
        JavaRDD<CtxtVector> transformedData = transformer.transform(data);
        JavaRDD<CtxtVector> transformedData2 = data.map(transformer::transform);

        transformedData.cache();
        transformedData2.cache();

        System.out.println("transformedData: ");
        transformedData.foreach(x -> {
            AbstractFunction2<Object, String, BoxedUnit> f = new AbstractFunction2<Object, String, BoxedUnit>() {
                public BoxedUnit apply(Object t1, String t2) {
                    System.out.println("Index:" + t1 + "      Value:" + SparkFHE.getInstance().decrypt(new Ciphertext((String)t2)));
                    return BoxedUnit.UNIT;
                }
            };
            x.foreachActive(f);
        });

        System.out.println("transformedData2: ");
        transformedData2.foreach(x -> {
            AbstractFunction2<Object, String, BoxedUnit> f = new AbstractFunction2<Object, String, BoxedUnit>() {
                public BoxedUnit apply(Object t1, String t2) {
                    System.out.println("Index:" + t1 + "      Value:" + SparkFHE.getInstance().decrypt(new Ciphertext((String)t2)));
                    return BoxedUnit.UNIT;
                }
            };
            x.foreachActive(f);
        });
    }

    public static void main(String[] args) {
        String pk="", sk="";

        // The variable slices represent the number of time a task is split up
        int slices = 2;
        // Create a SparkConf that loads defaults from system properties and the classpath
        SparkConf sparkConf = new SparkConf();
        //Provides the Spark driver application a name for easy identification in the Spark or Yarn UI
        sparkConf.setAppName("ElementwiseProductExample");

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

        // set a fast serializer
        sparkConf.set("spark.serializer", "org.apache.spark.serializer.KryoSerializer");
//        List<Class<?>> classes = Arrays.<Class<?>>asList(
//                Ciphertext.class,
//                Plaintext.class
//        );
//        sparkConf.registerKryoClasses((Class<?>[]) classes.toArray());
        sparkConf.set("spark.executor.memory", "16g");
        sparkConf.set("spark.driver.memory", "16g");

        // Creating a session to Spark. The session allows the creation of the
        // various data abstractions such as RDDs, DataFrame, and more.
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();

        // Creating spark context which allows the communication with worker nodes
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        // Note, the following loading of shared library and init are done on driver only. We need to do the same on the executors.
        // Load C++ shared library
        SparkFHEPlugin.setup();
        // Create SparkFHE object with HElib, a library that implements homomorphic encryption
        SparkFHE.init(FHELibrary.HELIB,  pk, sk);

        CTXT_0_FILE = Config.get_records_directory() + "/ptxt_long_0_"+ SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";
        CTXT_1_FILE = Config.get_records_directory() +"/ptxt_long_1_"+SparkFHE.getInstance().generate_crypto_params_suffix()+ ".jsonl";

        Broadcast<String> pk_b = jsc.broadcast(pk);
        Broadcast<String> sk_b = jsc.broadcast(sk);

        RunCtxtExample(spark, slices, pk_b, sk_b);
        RunCtxtRDDExample(jsc, slices, pk_b, sk_b);

        jsc.close();
        spark.close();
    }

}
