/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package spiritlab.sparkfhe.example.text;

import java.util.List;
import java.util.Scanner;
import java.util.ArrayList;
import java.io.File;
import spiritlab.sparkfhe.api.Config;
import spiritlab.sparkfhe.api.FHE;

public class FHEWordCountExample {
    static {
        try {
            System.loadLibrary("SparkFHE");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. \n" + e);
            System.exit(1);
        }
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
        FHE sparkFHE = new FHE(Config.HELIB);

        //// Do the RDD word count
        //SparkConf sparkConf = new SparkConf().setAppName("SparkFHETest").setMaster("local");
        //SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
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

