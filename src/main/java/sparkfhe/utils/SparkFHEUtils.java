package sparkfhe.utils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import sparkfhe.SparkFHE;

/**
 * Hello world!
 *
 */
public class SparkFHEUtils {
    public static final String RECORDS_PATH = System.getProperty("user.dir") + "/../../../bin/records";
    private static final JsonNodeFactory factory = JsonNodeFactory.instance;

    public static String loadCtxt(String ctxtFilePath) {
        try
        {
            String ctxt_full = new String(Files.readAllBytes(Paths.get(ctxtFilePath)));
            final ObjectNode ctxt_node = new ObjectMapper().readValue(ctxt_full, ObjectNode.class);
            return ctxt_node.get("ctxt").textValue();
        }
        catch (IOException e)
        {
            System.err.println("Could not load ciphertext from file: " + ctxtFilePath);
            return null;
        }
    }

    public static void storeCtxt(String ctxt, String ctxtFilePath) {
        ObjectNode ctxtObj = factory.objectNode();
        ctxtObj.put("ctxt", ctxt);
        try {
            Files.write(Paths.get(ctxtFilePath), ctxtObj.toString().getBytes());
        } catch (IOException e) {
            System.err.println("Could not write ciphertext to file: " + ctxtFilePath);
        }
    }

    public static String decryptCtxt(String ctxt) {
        return SparkFHE.decrypt(ctxt);
    }
}
