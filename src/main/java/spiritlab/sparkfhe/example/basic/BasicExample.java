package spiritlab.sparkfhe.example.basic;

import org.apache.spark.spiritlab.sparkfhe.SparkFHESetup;
import spiritlab.sparkfhe.api.SparkFHE;

public class BasicExample {

    public static void test_basic_op() {
        System.out.println("ADD(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.ADD));
        System.out.println("MUL(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.MUL));
        System.out.println("SUB(1, 0):"+SparkFHE.do_basic_op(1, 0, SparkFHE.SUB));
    }

    public static void main(String[] args) {
        // required to load our shared library
        SparkFHESetup.setup();

        test_basic_op();
    }

}
