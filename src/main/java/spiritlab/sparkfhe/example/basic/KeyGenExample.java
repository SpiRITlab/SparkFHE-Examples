package spiritlab.sparkfhe.example.basic;

import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.SparkFHE;
import spiritlab.sparkfhe.example.Config;

import java.io.File;

public class KeyGenExample {
    static {
        System.out.println("Execution path: " + System.getProperty("user.dir"));
        System.out.println("libSparkFHE path: " + System.getProperty("java.library.path"));
        try {
            System.loadLibrary("SparkFHE");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. \n" + e);
            System.exit(1);
        }
        System.out.println("Loaded native code library. \n");
    }

    public static void main(String argv[]) {
        new File(Config.DEFAULT_KEY_DIRECTORY).mkdirs();
        SparkFHE.init(FHELibrary.HELIB);
        SparkFHE.getInstance().generate_key_pair(Config.DEFAULT_CRYPTO_PARAMS_FILE, Config.DEFAULT_PUBLIC_KEY_FILE, Config.DEFAULT_SECRET_KEY_FILE);

        String inputNumber="1";
        String ctxt_string = SparkFHE.getInstance().encrypt(inputNumber);
        String ptxt_string = SparkFHE.getInstance().decrypt(ctxt_string);
        System.out.println("InputNumber="+inputNumber + ", result of dec(enc(InputNumber))="+ptxt_string);
    }


}
