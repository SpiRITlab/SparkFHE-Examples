//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.ml_seal;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.ml_fhe.linalg.CtxtVectorUDT;
import org.apache.spark.mllib.linalg.Vector;
import org.apache.spark.mllib.linalg.Vectors;
import org.apache.spark.mllib_fhe.linalg.CtxtVector;
import org.apache.spark.mllib_fhe.linalg.CtxtVectors;
import org.apache.spark.spiritlab.sparkfhe.SparkFHEPlugin;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import scala.runtime.AbstractFunction2;
import scala.runtime.BoxedUnit;
import spiritlab.sparkfhe.api.Ciphertext;
import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.example.Config;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// $example on$
// $example off$

public class DotProductExample {
    private static String CTXT_0_FILE;
    private static String CTXT_1_FILE;

    // ciphertext version; dot-product((1,1,1),(1,1,1)) the result is in the first index.
    public static void RunCtxtExample(SparkSession spark, int slices, Broadcast<String> pk_b, Broadcast<String> sk_b) {
        System.out.println("RunCtxtExample");
        // Create some vector data; also works for sparse vectors
        String zero_ctxt = SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label, CTXT_0_FILE);
        String one_ctxt = SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label, CTXT_1_FILE);
        List<Row> data = Arrays.asList(
                RowFactory.create("a", org.apache.spark.ml_fhe.linalg.CtxtVectors.dense(one_ctxt, one_ctxt, one_ctxt))
        );

        List<StructField> fields = new ArrayList<>(2);
        fields.add(DataTypes.createStructField("id", DataTypes.StringType, false));
        fields.add(DataTypes.createStructField("vector", new CtxtVectorUDT(), false));

        StructType schema = DataTypes.createStructType(fields);
        Dataset<Row> dataFrame = spark.createDataFrame(data, schema);
        dataFrame.printSchema();

        org.apache.spark.ml_fhe.linalg.CtxtVector inputVector = org.apache.spark.ml_fhe.linalg.CtxtVectors.dense(one_ctxt, one_ctxt, one_ctxt);

        org.apache.spark.ml_fhe.feature.DotProduct dp = new org.apache.spark.ml_fhe.feature.DotProduct()
                .setScalingVec(inputVector)
                .setInputCol("vector")
                .setOutputCol("transformedVector");

        // Batch transform the vectors to create new column:
        Dataset<Row> tr = dp.transform(dataFrame);
        tr.show();

        tr.foreach(row -> {
            org.apache.spark.ml_fhe.linalg.CtxtVector v = row.getAs("transformedVector");
            AbstractFunction2<Object, String, BoxedUnit> f = new AbstractFunction2<Object, String, BoxedUnit>() {
                public BoxedUnit apply(Object t1, String t2) {
                    if((int)t1==0)
                        System.out.println("Dot-Product:" + SparkFHE.getInstance().decrypt(new Ciphertext((String)t2)));
                    return BoxedUnit.UNIT;
                }
            };
            v.foreachActive(f);
        });
    }


    // ciphertext version using RDD; dot-product((1,1,1),(1,1,1)) the result is in the first index.
    public static void RunCtxtRDDExample(JavaSparkContext jsc, int slices, Broadcast<String> pk_b, Broadcast<String> sk_b) {
        System.out.println("RunCtxtRDDExample");
        String zero_ctxt = SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label, CTXT_0_FILE);
        String one_ctxt = SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label,CTXT_1_FILE);
        JavaRDD<CtxtVector> data = jsc.parallelize(Arrays.asList(
                CtxtVectors.dense(one_ctxt, one_ctxt, one_ctxt)
        ));
        CtxtVector inputVector = CtxtVectors.dense(one_ctxt, one_ctxt, one_ctxt);

        org.apache.spark.mllib_fhe.feature.DotProduct dp = new org.apache.spark.mllib_fhe.feature.DotProduct(inputVector);

        // Batch transform and per-row transform give the same results:
        JavaRDD<CtxtVector> transformedData = dp.transform(data);
        JavaRDD<CtxtVector> transformedData2 = data.map(dp::transform);

        transformedData.cache();
        transformedData2.cache();

        System.out.println("transformedData: ");
        transformedData.foreach(x -> {
            AbstractFunction2<Object, String, BoxedUnit> f = new AbstractFunction2<Object, String, BoxedUnit>() {
                public BoxedUnit apply(Object t1, String t2) {
                    if((int)t1==0)
                        System.out.println("transformedData Dot-Product:" + SparkFHE.getInstance().decrypt(new Ciphertext((String)t2)));
                    return BoxedUnit.UNIT;
                }
            };
            x.foreachActive(f);
        });

        System.out.println("transformedData2: ");
        transformedData2.foreach(x -> {
            AbstractFunction2<Object, String, BoxedUnit> f = new AbstractFunction2<Object, String, BoxedUnit>() {
                public BoxedUnit apply(Object t1, String t2) {
                    if((int)t1==0)
                        System.out.println("transformedData2 Dot-Product:" + SparkFHE.getInstance().decrypt(new Ciphertext((String)t2)));
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
        sparkConf.setAppName("ml.DotProductExample");

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
        // Create SparkFHE object with SEAL, a library that implements homomorphic encryption
        SparkFHE.init(FHELibrary.SEAL,  pk, sk);

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
