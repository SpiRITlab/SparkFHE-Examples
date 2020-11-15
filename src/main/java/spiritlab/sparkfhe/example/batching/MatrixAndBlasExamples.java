package spiritlab.sparkfhe.example.batching;

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.mllib_fhe.linalg.*;
import org.apache.spark.spiritlab.sparkfhe.SparkFHEPlugin;
import org.apache.spark.sql.SparkSession;
import spiritlab.sparkfhe.api.*;
import spiritlab.sparkfhe.example.Config;

import java.io.File;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class MatrixAndBlasExamples {

    public static void testFheDgemm() {
        System.out.println("====================== FHE DGEMM Test ==========================");
        /*
            [[1, 2, 3],
             [4, 5, 6]]
         */
        CtxtDenseMatrix matrixA = CtxtMatrices.dense(2, 3,
                new String[]{getCtxt(1), getCtxt(4), getCtxt(2), getCtxt(5),
                        getCtxt(3), getCtxt(6)}, false);
        System.out.println("Matrix A");
        decryptAndPrintCtxtDenseMatrix(matrixA);

        /*
            [[10, 11],
             [20, 21],
             [30, 31]]
         */
        CtxtDenseMatrix matrixB = CtxtMatrices.dense(3, 2,
                new String[]{getCtxt(10), getCtxt(20), getCtxt(30), getCtxt(11),
                        getCtxt(21), getCtxt(31)}, false);
        System.out.println("Matrix B");
        decryptAndPrintCtxtDenseMatrix(matrixB);

        /*
            [[140, 146],
             [320, 335]]
         */
        CtxtDenseMatrix matrixC = CtxtMatrices.dense(2, 2,
                new String[]{getCtxt(0), getCtxt(0), getCtxt(0), getCtxt(0)},
                false);
        System.out.println("Matrix C - Before multiplication");
        decryptAndPrintCtxtDenseMatrix(matrixC);

        StringVector resultStringVector = new StringVector(matrixC.toArray());

        SparkFHE.getInstance().fhe_dgemm("N", "N", 2, 2, 3, 1.0,
                new StringVector(matrixA.toArray()), 2,
                new StringVector(matrixB.toArray()), 3, 0.0,
                resultStringVector, 2);

        for (int i = 0; i < matrixC.values().length; i++) {
            matrixC.values()[i] = resultStringVector.get(i);
        }

        System.out.println("Matrix C - After multiplication");
        decryptAndPrintCtxtDenseMatrix(matrixC);

        // = 139.9999979213519
        // = 319.9999951463613
        // = 145.99997285309118
        // = 334.9999948600883
    }

    public static void testMatrixMultiplication() {
        System.out.println("====================== Matrix Multiply test ============================");
        /*
            [[1, 2, 3],
             [4, 5, 6]]
         */
        CtxtDenseMatrix matrixA = CtxtMatrices.dense(2, 3,
                new String[]{getCtxt(1), getCtxt(4), getCtxt(2), getCtxt(5),
                        getCtxt(3), getCtxt(6)}, false);
        System.out.println("Matrix A");
        decryptAndPrintCtxtDenseMatrix(matrixA);

        /*
            [[10, 11],
             [20, 21],
             [30, 31]]
         */
        CtxtDenseMatrix matrixB = CtxtMatrices.dense(3, 2,
                new String[]{getCtxt(10), getCtxt(20), getCtxt(30), getCtxt(11),
                        getCtxt(21), getCtxt(31)}, false);
        System.out.println("Matrix B");
        decryptAndPrintCtxtDenseMatrix(matrixB);

        /*
            [[140, 146],
             [320, 335]]
         */
        CtxtDenseMatrix matrixC = matrixA.multiply(matrixB);
        System.out.println("Matrix C");
        decryptAndPrintCtxtDenseMatrix(matrixC);
    }

    public static void testZerosMatrix() {
        System.out.println("====================== Test Zeros ===============================");
        decryptAndPrintCtxtDenseMatrix(CtxtMatrices.zeros(2, 3));
    }

    public static void testOnesMatrix() {
        System.out.println("====================== Test Ones ===============================");
        decryptAndPrintCtxtDenseMatrix(CtxtMatrices.ones(4, 3));
    }

    public static void testEyeMatrix() {
        System.out.println("====================== Test Identity ===============================");
        decryptAndPrintCtxtDenseMatrix(CtxtMatrices.eye(5));
    }

    public static void testRandMatrix() {
        System.out.println("====================== Test Random ===============================");
        decryptAndPrintCtxtDenseMatrix(CtxtMatrices.rand(3, 6, new Random()));
    }

    public static void testFheDgemv() {
        System.out.println("=========================== Test FHE DGEMV =============================");
        /*
            [[1, 3, 2],
             [4, 0, 1]]
         */
        CtxtDenseMatrix A = CtxtMatrices.dense(2, 3,
                new String[]{getCtxt(1), getCtxt(4), getCtxt(3), getCtxt(0),
                            getCtxt(2), getCtxt(1)});
        System.out.println("Matrix A");
        decryptAndPrintCtxtDenseMatrix(A);

        /*
            [[1],
             [0],
             [5]]
         */
        CtxtVector X = CtxtVectors.dense(new String[]{getCtxt(1), getCtxt(0), getCtxt(5)});
        System.out.println("Vector X");
        decryptAndPrintCtxtDenseVector(X);

        /*
            [[0],
             [0]]
         */
        CtxtVector Y = CtxtVectors.dense(new String[]{getCtxt(0), getCtxt(0)});
        System.out.println("Vector Y - Before multiplication");
        decryptAndPrintCtxtDenseVector(Y);

        StringVector resultStringVector = new StringVector(Y.toArray());

        SparkFHE.getInstance().fhe_dgemv("N", 2, 3, 1.0,
                new StringVector(A.toArray()), 2,
                new StringVector(X.toArray()), 1,
                0.0, resultStringVector, 1);

        String [] result = new String[resultStringVector.size()];

        /*
            [[11],
             [9]]
         */
        Y = CtxtVectors.dense(resultStringVector.toArray(result));
        System.out.println("Vector Y - After multiplication");
        decryptAndPrintCtxtDenseVector(Y);
    }

    private static void decryptAndPrintCtxtDenseVector(CtxtVector vector) {
        for (int i = 0; i < vector.size(); i++) {
            String encStr = vector.apply(i);
            DoubleVector output_vec = new DoubleVector();
            SparkFHE.getInstance().decode(output_vec, SparkFHE.getInstance().decrypt(new Ciphertext(encStr)));
            System.out.printf("%.2f  ", output_vec.get(0));
        }
        System.out.println();
    }

    private static void testMatrixVectorMultiplication() {
        System.out.println("=================== Test Matrix Vector Multiplication =====================");
        /*
            [[1, 3, 2],
             [4, 0, 1]]
         */
        CtxtDenseMatrix A = CtxtMatrices.dense(2, 3,
                new String[]{getCtxt(1), getCtxt(4), getCtxt(3), getCtxt(0),
                        getCtxt(2), getCtxt(1)});
        System.out.println("Matrix A");
        decryptAndPrintCtxtDenseMatrix(A);

        /*
            [[1],
             [0],
             [5]]
         */
        CtxtVector X = CtxtVectors.dense(new String[]{getCtxt(1), getCtxt(0), getCtxt(5)});
        System.out.println("Vector X");
        decryptAndPrintCtxtDenseVector(X);

        /*
            [[11],
             [9]]
         */
        CtxtDenseVector Y = A.multiply(X);
        System.out.println("Vector Y");
        decryptAndPrintCtxtDenseVector(Y);
    }

    private static void test_fhe_drot() {
        // Example from http://web.mit.edu/axiom-math_v8.14/arch/amd64_ubuntu1404/mnt/ubuntu64/doc/spadhelp/drot.help
        System.out.println("=====================  Test Drot ====================");
        int [] X = new int[]{6, 0, 1, 4, -1, -1};
        int [] Y = new int[]{5, 1, -4, 4, -4};
        System.out.println("Before rotation:");
        System.out.println("X: " + Arrays.toString(X));
        System.out.println("Y: " + Arrays.toString(Y));

        String [] XStr = new String[X.length];
        String [] YStr = new String[Y.length];

        for (int i = 0; i < X.length; i++) {
            XStr[i] = getCtxt(X[i]);
        }
        for (int i = 0; i < Y.length; i++) {
            YStr[i] = getCtxt(Y[i]);
        }

        StringVector xVector = new StringVector(XStr);
        StringVector yVector = new StringVector(YStr);

        SparkFHE.getInstance().fhe_drot(5, xVector, 1, yVector, 1, 0.707106781, 0.707106781);

        /*
         Expected:
                  [7.778174591, 0.70710678100000002, - 2.1213203429999998,
              5.6568542480000001, - 3.5355339050000003, - 1.],

             [- 0.70710678100000002, 0.70710678100000002, - 3.5355339050000003, 0.,
              - 2.1213203429999998]
         */
        System.out.println("After rotation:");
        System.out.print("X: ");
        for (String s: xVector) {
            DoubleVector output_vec = new DoubleVector();
            SparkFHE.getInstance().decode(output_vec, SparkFHE.getInstance().decrypt(new Ciphertext(s)));
            System.out.print(output_vec.get(0) + " ");
        }
        System.out.println();

        System.out.print("Y: ");
        for (String s: yVector) {
            DoubleVector output_vec = new DoubleVector();
            SparkFHE.getInstance().decode(output_vec, SparkFHE.getInstance().decrypt(new Ciphertext(s)));
            System.out.print(output_vec.get(0) + " ");
        }
        System.out.println();

    }

    private static void test_fhe_dtrmm() {
        System.out.println("================ Test dtrmm ==============");
        System.out.println("================ test 1 ================");
        int [] a = new int[]{3, 0, 0, 0, 0, -1, -2, 0, 0, 0, 2, 4, -3, 0, 0, 2, -1, 0, 4, 0, 1, 3, 2, -2, 1};
        int [] b = new int[]{2, 5, 0, 3, -1, 3, 5, 1, 1, 2, 1, 4, 2, -3, 1};

        String [] aStr = new String[a.length];
        String [] bStr = new String[b.length];

        for (int i = 0; i < a.length; i++) {
            aStr[i] = getCtxt(a[i]);
        }
        for (int i = 0; i < b.length; i++) {
            bStr[i] = getCtxt(b[i]);
        }

        CtxtMatrix matA = new CtxtDenseMatrix(5, 5, aStr);
        System.out.println("A: ");
        decryptAndPrintCtxtDenseMatrix(matA);

        StringVector resultVector = new StringVector(bStr);
        System.out.println("B - before multiplication:");
        decryptAndPrintStringVector(resultVector);

        SparkFHE.getInstance().fhe_dtrmm("L", "U", "N", "N",
                5, 3, 1, new StringVector(aStr), 5, resultVector, 5);

        System.out.println("B - after multiplication: ");
        decryptAndPrintStringVector(resultVector);

        System.out.println("=============== test 2 =================");
        a = new int[]{2, 2, 2, 0, 2, 0, 3, 1, 3, 4, 0, 0, 1, 0, -1, 0, 0, 0, -2, 2, 0, 0, 0, 0, -1};
        b = new int[]{3, 2, -2, 4, 1, -1, -1, -1, -3, -1, 0, 0, -1, 3, 2};
        aStr = new String[a.length];
        bStr = new String[b.length];
        for (int i = 0; i < a.length; i++) {
            aStr[i] = getCtxt(a[i]);
        }
        for (int i = 0; i < b.length; i++) {
            bStr[i] = getCtxt(b[i]);
        }

        matA = new CtxtDenseMatrix(5, 5, aStr);
        System.out.println("A: ");
        decryptAndPrintCtxtDenseMatrix(matA);

        resultVector = new StringVector(bStr);
        System.out.println("B - before multiplication:");
        decryptAndPrintStringVector(resultVector);

        SparkFHE.getInstance().fhe_dtrmm("R", "L", "N", "N",
                3, 5, 1, new StringVector(aStr), 5, resultVector, 3);

        System.out.println("B - after multiplication: ");
        decryptAndPrintStringVector(resultVector);

        System.out.println("=============== test 3 =================");
        a = new int[]{0,0,0,0,0,0, 2,0,0,0,0,0, -3,0,0,0,0,0, 1,1,4,0,0,0, 2,1,-1,0,0,0, 4,-2,1,-1,2,0};
        b = new int[]{1,2,1,3,-1,-2};
        aStr = new String[a.length];
        bStr = new String[b.length];
        for (int i = 0; i < a.length; i++) {
            aStr[i] = getCtxt(a[i]);
        }
        for (int i = 0; i < b.length; i++) {
            bStr[i] = getCtxt(b[i]);
        }

        matA = new CtxtDenseMatrix(6, 6, aStr);
        System.out.println("A: ");
        decryptAndPrintCtxtDenseMatrix(matA);

        resultVector = new StringVector(bStr);
        System.out.println("B - before multiplication:");
        decryptAndPrintStringVector(resultVector);

        SparkFHE.getInstance().fhe_dtrmm("R", "U", "N", "U",
                1, 6, 1, new StringVector(aStr), 6, resultVector, 1);

        System.out.println("B - after multiplication: ");
        decryptAndPrintStringVector(resultVector);

        System.out.println("=============== test 4 =================");
        a = new int[]{-1,0,0,0,0, -4,-2,0,0,0, -2,2,-3,0,0, 2,2,-1,1,0, 3,2,4,0,-2};
        b = new int[]{1,3,-2,4,2, 2,3,-1,4,2, 3,-1,0,-3,2, 4,2,1,-3,2};
        aStr = new String[a.length];
        bStr = new String[b.length];
        for (int i = 0; i < a.length; i++) {
            aStr[i] = getCtxt(a[i]);
        }
        for (int i = 0; i < b.length; i++) {
            bStr[i] = getCtxt(b[i]);
        }

        matA = new CtxtDenseMatrix(5, 5, aStr);
        System.out.println("A: ");
        decryptAndPrintCtxtDenseMatrix(matA);

        resultVector = new StringVector(bStr);
        System.out.println("B - before multiplication:");
        decryptAndPrintStringVector(resultVector);

        SparkFHE.getInstance().fhe_dtrmm("L", "U", "T", "N",
                5, 4, 1, new StringVector(aStr), 5, resultVector, 5);

        System.out.println("B - after multiplication: ");
        decryptAndPrintStringVector(resultVector);
    }

    private static void test_fhe_dtrmv() {
        int [] a = new int[]{0, 1, 2, 3, 0, 0, 3, 4, 0, 0, 0, 3, 0, 0, 0, 0};
        int [] x = new int[]{1, 2, 3, 4};
        String [] aStr = new String[a.length];
        String [] xStr = new String[x.length];
        for (int i = 0; i < a.length; i++) {
            aStr[i] = getCtxt(a[i]);
        }
        for (int i = 0; i < x.length; i++) {
            xStr[i] = getCtxt(x[i]);
        }
        CtxtMatrix matA = new CtxtDenseMatrix(4, 4, aStr);
        System.out.println("A: ");
        decryptAndPrintCtxtDenseMatrix(matA);

        StringVector resultVector = new StringVector(xStr);
        System.out.println("X - before multiplication:");
        decryptAndPrintStringVector(resultVector);

        SparkFHE.getInstance().fhe_dtrmv("L", "N", "U",
                5, new StringVector(aStr), 4, resultVector, 1);

        System.out.println("X - after multiplication: ");
        decryptAndPrintStringVector(resultVector);
    }

    private static void test_fhe_dsymm() {
        int [] a = new int[]{1, 2, 1, 0, 4, -1, 0, 0, -1};
        int [] b = new int[]{1, 2, 1, -3, 4, -1, 2, 0, -1, 2, 0, -1, -1, 1, -1, 2, -2, 1};
        int [] c = new int[]{6, 9, -2, 4, 11, -6, 1, 5, 3, 1, 5, 3, 0, 3, -1, -1, -5, 32};
        String [] aStr = new String[a.length];
        String [] bStr = new String[b.length];
        String [] cStr = new String[c.length];

        for (int i = 0; i < a.length; i++) aStr[i] = getCtxt(a[i]);
        for (int i = 0; i < b.length; i++) bStr[i] = getCtxt(b[i]);
        for (int i = 0; i < c.length; i++) cStr[i] = getCtxt(c[i]);

        StringVector resultVector = new StringVector(cStr);

        SparkFHE.getInstance().fhe_dsymm("L", "L", 3, 6, 2,
                new StringVector(aStr), 3, new StringVector(bStr), 3, 2, resultVector, 3);

        decryptAndPrintStringVector(resultVector);
    }

    private static void decryptAndPrintStringVector(StringVector vector) {
        for (String s: vector) {
            DoubleVector output_vec = new DoubleVector();
            SparkFHE.getInstance().decode(output_vec, SparkFHE.getInstance().decrypt(new Ciphertext(s)));
            System.out.printf("%.2f\t", output_vec.get(0));
        }
        System.out.println();
    }

    private static void decryptAndPrintCtxtDenseMatrix(CtxtMatrix matrix) {
        double[][] arr = new double[matrix.numRows()][matrix.numCols()];
        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                String encStr = matrix.apply(i, j);
                DoubleVector output_vec = new DoubleVector();
                SparkFHE.getInstance().decode(output_vec, SparkFHE.getInstance().decrypt(new Ciphertext(encStr)));
                arr[i][j] = output_vec.get(0);
            }
        }

        for (int i = 0; i < arr.length; i++) {
            for (int j = 0; j < arr[i].length; j++) {
                System.out.printf("%.2f\t", arr[i][j]);
            }
            System.out.println();
        }
        System.out.println();
    }

    private static void encrypt_data(int ... values){
        // store the ciphertexts to the pre-defined file location
        for (int l: values) {
            String filePath = String.format("%s/packed_ptxt_long_%d_%s.jsonl", Config.get_records_directory(), l,
                    SparkFHE.getInstance().generate_crypto_params_suffix());
            File file = new File(filePath);
            if (file.exists()) continue;

            System.out.println("Storing ciphertext to " + filePath);
            SparkFHE.getInstance().store_ciphertext_to_file(
                    Config.Ciphertext_Label,
                    SparkFHE.getInstance().encrypt(SparkFHE.getInstance().encode(String.valueOf(l))).toString(),
                    filePath);
        }
    }

    private static String getCtxt(int ptxt) {
        String filePath = String.format("%s/packed_ptxt_long_%d_%s.jsonl", Config.get_records_directory(), ptxt,
                SparkFHE.getInstance().generate_crypto_params_suffix());
        File file = new File(filePath);
        if (!file.exists()) {
            encrypt_data(ptxt);
        }

        return SparkFHE.getInstance().read_ciphertext_from_file_as_string(Config.Ciphertext_Label, filePath);
    }

    public static void main(String[] args) {
        String scheme="", library = "", pk="", sk="";
        // The variable slices represent the number of time a task is split up
        int slices=2;

        // Create a SparkConf that loads defaults from system properties and the classpath
        SparkConf sparkConf = new SparkConf();
        //Provides the Spark driver application a name for easy identification in the Spark or Yarn UI
        sparkConf.setAppName("MatrixOperationsWithBatchingExample");

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
                break;
            case LOCAL:
                sparkConf.setMaster("local");
                library = args[1];
                scheme = args[2];
                pk = args[3];
                sk = args[4];
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
        SparkFHE.init(library, scheme, pk, sk);

        Broadcast<String> pk_b = jsc.broadcast(pk);
        Broadcast<String> sk_b = jsc.broadcast(sk);

//        encrypt_data(1, 2, 3, 4, 5, 6, 10, 20, 30, 11, 21, 31);
//        testFheDgemm();
//        testMatrixMultiplication();
//        testZerosMatrix();
//        testOnesMatrix();
//        testEyeMatrix();
//        testRandMatrix();
//
//        testFheDgemv();
//        testMatrixVectorMultiplication();
//        test_fhe_drot();
        test_fhe_dtrmm();
//        test_fhe_dtrmv();
//        test_fhe_dsymm();
        // Normally, the Spark web UI at http://127.0.0.1:4040 will be shutdown after the experiment run.
        // Uncomment the following block of code to paused the shutdown so that you have a chance to check the Spark web UI.
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
