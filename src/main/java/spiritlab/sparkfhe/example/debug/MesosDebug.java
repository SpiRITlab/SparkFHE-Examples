//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//

package spiritlab.sparkfhe.example.test;

import spiritlab.sparkfhe.example.Config;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class MesosDebug {

    public static void main(String[] args) throws InterruptedException {
        new File(Config.get_keys_directory()).mkdirs();

        TimeUnit.MINUTES.sleep(20);

    }
}
