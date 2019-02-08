package spiritlab.sparkfhe.example.basic;

//import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.spiritlab.sparkfhe.SparkFHESetup;
import org.apache.spark.sql.SparkSession;
import spiritlab.sparkfhe.api.SparkFHE;

import org.apache.spark.SparkConf;

/**
 * This is an example for SparkFHE project. Created to confirm the smooth communication between Java and C++ codes.
 * Data transformations are made in Spark framework are made through the use of lambda function. This program contains
 * a few very simple example of such transformations.
 */

public class BasicExample {

    /**
     * Invoking the basic C++ API functions
     */
    public static void test_basic_op() {
        // Testing the addition function
        System.out.println("ADD(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.ADD));
        // Testing the multiplication function
        System.out.println("MUL(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.MUL));
        // Testing the substraction function
        System.out.println("SUB(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.SUB));
    }


    public static void main(String[] args) {
        // Loading up the necessary libraries for Java and C++ interaction
        SparkFHESetup.setup();
        // Calling the test_basic_op method
        test_basic_op();
    }

}
