//
// Copyright SpiRITlab - The SparkFHE project.
// https://github.com/SpiRITlab
//


package spiritlab.sparkfhe.example.text;

import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import spiritlab.sparkfhe.api.FHELibrary;
import spiritlab.sparkfhe.api.FHE;
import spiritlab.sparkfhe.api.FHEScheme;

public class FHEWordCountExample {
    static {
        System.out.println("libSparkFHE path: " + System.getProperty("java.library.path"));
        try {
            System.loadLibrary("SparkFHE");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. \n" + e);
            System.exit(1);
        }
        System.out.println("Loaded native code library. \n");
    }

    public static String bytesToHex(byte[] bytes) {
        StringBuilder builder = new StringBuilder();
        for (byte b: bytes) {
            builder.append(String.format("%02x", b));
        }
        return builder.toString();
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1) {
            System.err.println("Usage: JavaWordCount <file>");
            System.exit(1);
        }

        // Before performing the word count each word must be homomorphically encrypyted to preserve
        // privacy. The encryption scheme is as follows:
        // 1. Words are converted to a fixed length binary token using CRC64
        // 2. CRC64 representations are encrypted as 64 1-bit ciphertexts
        // 3. Word count is performed on the ciphertexts
        // 4. The results are decrypted locally
        //
        // Steps 1-4 are performed in native code. This class calls the native SWIG interface

        File wordFile = new File(args[0]);
        Scanner wordScanner = new Scanner(wordFile);
        List<String> words = new ArrayList<String>(); // constant time append

        while(wordScanner.hasNext()) {
            words.add(wordScanner.next());
        }

        // Convert words to a vector of 64 bit integers (CRC64 encoded words)
        


        //// Convert to FHE Ciphertexts using native call
        //// TODO List<Words> encodedWords = native call;
        FHE sparkFHE = new FHE(FHELibrary.HELIB, FHEScheme.BGV);

        //// Do the RDD word count
        //SparkConf sparkConf = new SparkConf().setAppName("SparkFHETest").setMaster("local");
        //SparkSession spark = SparkSession.builder().Config(sparkConf).getOrCreate();
        //JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());

        ////JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
        //JavaRDD<String> words = jsc.parallelize(encodedWords);

        //JavaPairRDD<String, Integer> ones = words.mapToPair(s -> new Tuple2<>(s, 1));

        //JavaPairRDD<String, Integer> counts = ones.reduceByKey((i1, i2) -> i1 + i2);

        //List<Tuple2<String, Integer>> output = counts.collect();

        //// Finally, convert from counts of encoded words back to their unencoded form
        //for (Tuple2<?,?> tuple : output) {
        //    String unencoded = wordMappings.get(tuple._1());
        //    System.out.println(unencoded + ": " + tuple._2());
        //}

        //spark.stop();
    }
}

