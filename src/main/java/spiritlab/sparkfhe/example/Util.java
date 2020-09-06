package spiritlab.sparkfhe.example;

import spiritlab.sparkfhe.api.*;
import java.util.Random;

public class Util {

    // source: https://mkyong.com/java/java-generate-random-integers-in-a-range/
    public static int getRandom(int max) {
        int min = 0;
        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }
        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static void decrypt_and_print(String scheme, String output_label, Ciphertext ctxt){
        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleVector output_vec = new DoubleVector();
            SparkFHE.getInstance().decode(output_vec, SparkFHE.getInstance().decrypt(ctxt));
            System.out.println(output_label + " = " + String.valueOf(output_vec.get(0)));
        } else { // BGV or BFV
            LongVector output_vec = new LongVector();
            SparkFHE.getInstance().decode(output_vec, SparkFHE.getInstance().decrypt(ctxt));
            System.out.println(output_label + " = " + String.valueOf(output_vec.get(0)));
        }
    }

    public static void decode_and_print(String scheme, String output_label, Plaintext ptxt){
        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleVector output_vec = new DoubleVector();
            SparkFHE.getInstance().decode(output_vec, ptxt);
            System.out.println(output_label + " = " + String.valueOf(output_vec.get(0)));
        } else { // BGV or BFV
            LongVector output_vec = new LongVector();
            SparkFHE.getInstance().decode(output_vec, ptxt);
            System.out.println(output_label + " = " + String.valueOf(output_vec.get(0)));
        }
    }

    public static void decrypt_and_print(String scheme, String output_label, Ciphertext ctxt, boolean loop, int bound){
        if (!output_label.equals("")) {
            output_label += " = ";
        }

        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleVector output_vec = new DoubleVector();
            SparkFHE.getInstance().decode(output_vec, SparkFHE.getInstance().decrypt(ctxt));
            if (loop) {
                if (bound == 0) bound = output_vec.size();
                for (int i = 0; i < bound; i++){
                    System.out.println(output_label + String.valueOf(output_vec.get(i)));
                }
            } else {
                System.out.println(output_label + String.valueOf(output_vec.get(0)));
            }
        } else { // BGV or BFV
            LongVector output_vec = new LongVector();
            SparkFHE.getInstance().decode(output_vec, SparkFHE.getInstance().decrypt(ctxt));
            if (loop) {
                if (bound == 0) bound = output_vec.size();
                for (int i = 0; i < bound; i++){
                    System.out.println(output_label + String.valueOf(output_vec.get(i)));
                }
            } else {
                System.out.println(output_label + String.valueOf(output_vec.get(0)));
            }
        }
    }

    public static void decode_and_print(String scheme, String output_label, Plaintext ptxt, boolean loop, int bound){
        if (!output_label.equals(""))
            output_label += " = ";

        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleVector output_vec = new DoubleVector();
            SparkFHE.getInstance().decode(output_vec, ptxt);
            if (loop) {
                if (bound == 0) bound = output_vec.size();
                for (int i = 0; i < bound; i++){
                    System.out.println(output_label + String.valueOf(output_vec.get(i)));
                }
            } else {
                System.out.println(output_label + String.valueOf(output_vec.get(0)));
            }
        } else { // BGV or BFV
            LongVector output_vec = new LongVector();
            SparkFHE.getInstance().decode(output_vec, ptxt);
            if (loop) {
                if (bound == 0) bound = output_vec.size();
                for (int i = 0; i < bound; i++){
                    System.out.println(output_label + String.valueOf(output_vec.get(i)));
                }
            } else {
                System.out.println(output_label + String.valueOf(output_vec.get(0)));
            }
        }
    }

    public static void decrypt_and_print_matrix(String scheme, String output_label, Ciphertext ctxt, int column_size, boolean column_major, int rows){
        if (!output_label.equals("")) {
            output_label += " = ";
        }

        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleMatrix output_mat = new DoubleMatrix();
            SparkFHE.getInstance().decode(output_mat, SparkFHE.getInstance().decrypt(ctxt), column_size);

            if (rows == 0) rows = output_mat.size();
            System.out.println(output_label);
            for (int i = 0; i < rows; i++){
                for (int j = 0; j < output_mat.get(0).size(); j++){
                    System.out.print(String.valueOf(output_mat.get(i).get(j)) + "   ");
                }
                System.out.println("");
            }
        } else { // BGV or BFV
            LongMatrix output_mat = new LongMatrix();
            SparkFHE.getInstance().decode(output_mat, SparkFHE.getInstance().decrypt(ctxt), column_size);

            if (rows == 0) rows = output_mat.size();
            System.out.println(output_label);
            for (int i = 0; i < rows; i++){
                for (int j = 0; j < output_mat.get(0).size(); j++){
                    System.out.print(String.valueOf(output_mat.get(i).get(j)) + "   ");
                }
                System.out.println("");
            }
        }
    }

    public static void decode_and_print_matrix(String scheme, String output_label, Plaintext ptxt, int column_size, boolean column_major, int rows){
        if (!output_label.equals(""))
            output_label += " = ";

        if (scheme.equalsIgnoreCase(FHEScheme.CKKS)){
            DoubleMatrix output_mat = new DoubleMatrix();
            SparkFHE.getInstance().decode(output_mat, ptxt, column_size);

            if (rows == 0) rows = output_mat.size();
            System.out.println(output_label);
            for (int i = 0; i < rows; i++){
                for (int j = 0; j < output_mat.get(0).size(); j++){
                    System.out.print(String.valueOf(output_mat.get(i).get(j)) + "   ");
                }
                System.out.println("");
            }
        } else { // BGV or BFV
            LongMatrix output_mat = new LongMatrix();
            SparkFHE.getInstance().decode(output_mat, ptxt, column_size);

            if (rows == 0) rows = output_mat.size();
            System.out.println(output_label);
            for (int i = 0; i < rows; i++){
                for (int j = 0; j < output_mat.get(0).size(); j++){
                    System.out.print(String.valueOf(output_mat.get(i).get(j)) + "   ");
                }
                System.out.println("");
            }
        }
    }
}
