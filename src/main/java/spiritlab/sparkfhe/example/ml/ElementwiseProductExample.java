//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

//https://github.com/apache/spark/blob/master/examples/src/main/java/org/apache/spark/examples/ml/JavaElementwiseProductExample.java

package spiritlab.sparkfhe.example.ml;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.SparkSession;

import org.apache.spark.spiritlab.sparkfhe.SparkFHESetup;

// $example on$
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.apache.spark.ml_fhe.linalg.CtxtVectorUDT;
import org.apache.spark.mllib_fhe.linalg.CtxtVector;
import org.apache.spark.mllib_fhe.linalg.CtxtVectors;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.RowFactory;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.api.java.JavaRDD;
import scala.runtime.AbstractFunction2;
import scala.runtime.BoxedUnit;
import static spiritlab.sparkfhe.api.Ciphertext.*;

import spiritlab.sparkfhe.api.*;
// $example off$

public class ElementwiseProductExample {

    private static String sparkfhe_path="../SparkFHE";


//    public static void RunExample(SparkSession spark) {
//        // $example on$
//        // Create some vector data; also works for sparse vectors
//        List<Row> data = Arrays.asList(
//                RowFactory.create("a", org.apache.spark.ml_fhe.linalg.CtxtVectors.dense(1.0, 2.0, 3.0)),
//                RowFactory.create("b", org.apache.spark.ml_fhe.linalg.CtxtVectors.dense(4.0, 5.0, 6.0))
//        );
//
//        List<StructField> fields = new ArrayList<>(2);
//        fields.add(DataTypes.createStructField("id", DataTypes.StringType, false));
//        fields.add(DataTypes.createStructField("vector", new CtxtVectorUDT(), false));
//
//        StructType schema = DataTypes.createStructType(fields);
//
//        Dataset<Row> dataFrame = spark.createDataFrame(data, schema);
//
//        dataFrame.printSchema();
//
//        org.apache.spark.ml_fhe.linalg.CtxtVector transformingVector = org.apache.spark.ml_fhe.linalg.CtxtVectors.dense(0.0, 1.0, 2.0);
//
//        org.apache.spark.ml_fhe.feature.ElementwiseProduct transformer = new org.apache.spark.ml_fhe.feature.ElementwiseProduct()
//                .setScalingVec(transformingVector)
//                .setInputCol("vector")
//                .setOutputCol("transformedVector");
//
//        // Batch transform the vectors to create new column:
//        Dataset<Row> tr = transformer.transform(dataFrame);
//        tr.show();
////        System.out.println(tr.count());
//        // $example off$
//    }
//
//    public static void RunRDDExample(JavaSparkContext jsc) {
//
//        // $example on$
//        // Create some vector data; also works for sparse vectors
//        JavaRDD<CtxtVector> data = jsc.parallelize(Arrays.asList(
//                CtxtVectors.dense(1.0, 2.0, 3.0), CtxtVectors.dense(4.0, 5.0, 6.0)));
//        CtxtVector transformingVector = CtxtVectors.dense(0.0, 1.0, 2.0);
//        org.apache.spark.mllib_fhe.feature.ElementwiseProduct transformer =
//                new org.apache.spark.mllib_fhe.feature.ElementwiseProduct(transformingVector);
//
//        // Batch transform and per-row transform give the same results:
//        JavaRDD<CtxtVector> transformedData = transformer.transform(data);
//        JavaRDD<CtxtVector> transformedData2 = data.map(x -> transformer.transform(x));
//        // $example off$
//
//        System.out.println("transformedData: ");
//        transformedData.foreach(x -> System.out.println(x));
//
//        System.out.println("transformedData2: ");
//        transformedData2.foreach(x -> System.out.println(x));
//    }

    public static void RunCtxtExample(SparkSession spark, String zero_ctxt_path, String one_ctxt_path) {
        // Create some vector data; also works for sparse vectors
        String zero_ctxt = DataSourceHandler.loadCtxt(zero_ctxt_path);
        String one_ctxt = DataSourceHandler.loadCtxt(one_ctxt_path);
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

    public static void RunCtxtRDDExample(JavaSparkContext jsc, String zero_ctxt_path, String one_ctxt_path) {
        String zero_ctxt = DataSourceHandler.loadCtxt(zero_ctxt_path);
        String one_ctxt = DataSourceHandler.loadCtxt(one_ctxt_path);
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
        SparkFHESetup.setup();

        int slices = (args.length == 5) ? Integer.parseInt(args[4]) : 2;

        String pk = args[0];
        String sk = args[1];
        SparkConf sparkConf = new SparkConf().setAppName("ElementwiseProductExample");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        SparkFHE.init(FHELibrary.HELIB, pk, sk);

        RunCtxtExample(spark, args[2], args[3]);
        RunCtxtRDDExample(jsc, args[2], args[3]);

        jsc.close();
        spark.close();
    }

}
