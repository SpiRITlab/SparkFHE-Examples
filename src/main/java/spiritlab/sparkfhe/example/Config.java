package spiritlab.sparkfhe.example;

public class Config {
    public static final String CURRENT_DIRECTORY=System.getProperty("user.dir");
    public static final String DEFAULT_CRYPTO_PARAMS_FILE=CURRENT_DIRECTORY+"/SparkFHE-Addon/resources/params/CRYPTO_PARAMS_HELIB_TEMPLATE.json";

    public static final String DEFAULT_KEY_DIRECTORY=CURRENT_DIRECTORY+"/gen/keys";
    public static final String DEFAULT_PUBLIC_KEY_FILE=DEFAULT_KEY_DIRECTORY+"/my_public_key.txt";
    public static final String DEFAULT_SECRET_KEY_FILE=DEFAULT_KEY_DIRECTORY+"/my_secret_key.txt";

    public static final String DEFAULT_RECORDS_DIRECTORY=CURRENT_DIRECTORY+"/gen/records";

    public static final String CTXT_LABEL="ctxt";

    public static int NUM_OF_VECTOR_ELEMENTS = 5;
}
