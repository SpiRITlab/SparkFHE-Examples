package spiritlab.sparkfhe.demo.nonbatching;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.spiritlab.sparkfhe.SparkFHEPlugin;
import org.apache.spark.sql.SparkSession;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.example.Config;

public class BasicOpsExample {

    public static void perform_basic_FHE_op(SparkSession spark, String library, String scheme, Broadcast<String> pk_b,
                                            Broadcast<String> sk_b, String ctxt1, String ctxt2) {



    }

    public static void main(String[] args) {
        String scheme = "", library = "", pk = "", sk = "", ctxt1="", ctxt2="";

        // The variable slices represent the number of time a task is split up
        int slices = 2;
        // Create a SparkConf that loads defaults from system properties and the classpath
        SparkConf sparkConf = new SparkConf();
        //Provides the Spark driver application a name for easy identification in the Spark or Yarn UI
        sparkConf.setAppName("BasicOPsExample");

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
                ctxt1 = args[6];
                ctxt2 = args[7];
                break;
            case LOCAL:
                sparkConf.setMaster("local");
                library = args[1];
                scheme = args[2];
                pk = args[3];
                sk = args[4];
                ctxt1 = args[5];
                ctxt2 = args[6];
                break;
            default:
                break;
        }
        System.out.println("CURRENT_DIRECTORY = " + Config.get_current_directory());

        // Creating a session to Spark. The session allows the creation of the
        // various data abstractions such as RDDs, DataFrame, and more.
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();

        // Creating spark context which allows the communication with worker nodes
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        // Note, the following loading of shared library and init are done on driver only. We need to do the same on the executors.
        // Load C++ shared library
        SparkFHEPlugin.setup();
        // Create SparkFHE object with FHE library
        SparkFHE.init(library, scheme, pk);

        Broadcast<String> pk_b = jsc.broadcast(pk);
        Broadcast<String> sk_b = jsc.broadcast(sk);

        perform_basic_FHE_op(spark, library, scheme, pk_b, sk_b, ctxt1, ctxt2);

        // Stop existing spark context
        jsc.close();

        // Stop existing spark session
        spark.close();
    }
}
