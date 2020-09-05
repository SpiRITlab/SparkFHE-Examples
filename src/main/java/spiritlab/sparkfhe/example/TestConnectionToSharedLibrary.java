//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example;

import org.apache.spark.spiritlab.sparkfhe.SparkFHEPlugin;

/**
 * This example test the connection to the libSparkFHE shared library.
 */

public class TestConnectionToSharedLibrary {

    public static void main(String[] args) {
        System.out.println("Loading C++ shared library...");
        // Loading up the necessary libraries for Java and C++ interaction
        SparkFHEPlugin.setup();
        System.out.println("C++ shared library loaded successfully!");
    }

}
