package spiritlab.sparkfhe.example.basic;

//import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.spiritlab.sparkfhe.SparkFHESetup;
import org.apache.spark.sql.SparkSession;
import spiritlab.sparkfhe.api.SparkFHE;

import org.apache.spark.SparkConf;

/**
 * This is an example for SparkFHE project. Created to test the functionality
 * of the underlying C++ APIs. A few simple functions are invoked via lambda.
 */

public class BasicExample {

    /**
     * involving the basic C++ API functions
     */
    public static void test_basic_op() {
        // testing the addition function
        System.out.println("ADD(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.ADD));
        // testing the multiplication function
        System.out.println("MUL(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.MUL));
        // testing the substraction function
        System.out.println("SUB(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.SUB));
    }


    public static void main(String[] args) {
        // setting up the SparkFHE
        SparkFHESetup.setup();
        // calling the test_basic_op method
        test_basic_op();
    }

}
