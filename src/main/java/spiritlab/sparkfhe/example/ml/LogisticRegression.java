//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.ml;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.spiritlab.sparkfhe.SparkFHESetup;

// $example on$
import java.util.Arrays;
import org.apache.spark.mllib_fhe.linalg.CtxtVector;
import org.apache.spark.mllib_fhe.linalg.CtxtVectors;
import org.apache.spark.api.java.JavaRDD;
import static spiritlab.sparkfhe.api.Ciphertext.*;
import spiritlab.sparkfhe.api.*;

public class LogisticRegression {

	private static String sparkfhe_path = "../SparkFHE";

	public static void RunCtxtRDDExample(JavaSparkContext jsc, String size, String depth) {

//    LogisticRegression logisticRegression;
//    CtxtVector features;
//    JavaRDD<Tree> tree = jsc.parallelize(Arrays.asList(forest));
//    JavaRDD<CtxtVector> feature = jsc.parallelize(Arrays.asList(features));

	}

	public static void main(String[] args) {
		SparkFHESetup.setup();

		int slices = (args.length == 5) ? Integer.parseInt(args[4]) : 2;

		String pk = args[0];
		String sk = args[1];
		SparkConf sparkConf = new SparkConf().setAppName("LogisticRegressionExample");
		SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
		JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

		SparkFHE.init(FHELibrary.SEAL, pk, sk);

		RunCtxtRDDExample(jsc, args[2], args[3]);

		jsc.close();
		spark.close();
	}
}