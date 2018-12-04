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

import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaPairRDD;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.sql.SparkSession;
import scala.Tuple2;

import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;
import java.util.Scanner;
import java.util.Map;
import java.util.TreeMap;
import java.util.LinkedList;

import java.security.MessageDigest;

import java.io.File;

public class NaiveWordCountExample {
    static {
        try {
            System.loadLibrary("SparkFHE");
        } catch (UnsatisfiedLinkError e) {
            System.err.println("Native code library failed to load. \n" + e);
            System.exit(1);
        }
    }

    private static final Pattern SPACE = Pattern.compile(" ");

    // ASCII representation of the salt is as long as the generated hash.
    // In practice a random salt should be generated for each run of word count
    // to prevent an advesary from generating a lookup table to reverse the 
    // encoding of the words.
    private static String m_salt = "InPracticeUseARandom32ByteString";

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

        // Before words go to RDD, they must be encoded to unique non-reversible tokens
        // This provides minimal security, as multiple copies of the same encoded word
        // will be identical, allowing for frequency analysis attacks.
        File wordFile = new File(args[0]);
        Scanner wordScanner = new Scanner(wordFile);

        // Maintain a mapping of encoded words to their unencoded form
        Map<String, String> wordMappings = new TreeMap<String, String>();

        List<String> encodedWords = new LinkedList<String>(); // constant time append

        // Words are hashed using SHA-256 to preserve privacy
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        while(wordScanner.hasNext()) {
            digest.reset();
            String unencoded = wordScanner.next();
            digest.update(unencoded.getBytes());
            digest.update(m_salt.getBytes()); // salt the hash to frustrate LUT attacks
            String encoded = bytesToHex(digest.digest());

            encodedWords.add(encoded);
            wordMappings.putIfAbsent(encoded, unencoded);
        }

        // Do the RDD word count
        SparkConf sparkConf = new SparkConf().setAppName("SparkFHETest").setMaster("local");
        SparkSession spark = SparkSession.builder().config(sparkConf).getOrCreate();
        JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
        //SparkSession spark = SparkSession
        //    .builder()
        //    .appName("JavaNaivePrivateWordCount")
        //    .getOrCreate();

        //JavaSparkContext jsc = new JavaSparkContext(spark.sparkContext());
        JavaRDD<String> words = jsc.parallelize(encodedWords);

        JavaPairRDD<String, Integer> ones = words.mapToPair(s -> new Tuple2<>(s, 1));

        JavaPairRDD<String, Integer> counts = ones.reduceByKey((i1, i2) -> i1 + i2);

        List<Tuple2<String, Integer>> output = counts.collect();

        // Finally, convert from counts of encoded words back to their unencoded form
        for (Tuple2<?,?> tuple : output) {
            String unencoded = wordMappings.get(tuple._1());
            System.out.println(unencoded + ": " + tuple._2());
        }

        spark.stop();
    }
}

