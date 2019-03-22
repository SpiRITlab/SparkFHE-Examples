package spiritlab.sparkfhe.example.basic;

import com.esotericsoftware.kryo.Kryo;
import org.apache.spark.serializer.KryoRegistrator;
import spiritlab.sparkfhe.api.Ciphertext;
import spiritlab.sparkfhe.api.Plaintext;
import spiritlab.sparkfhe.api.SerializedCiphertextObject;

public class myKryoReg implements KryoRegistrator {
    @Override
    public void registerClasses(Kryo kryo) {
        kryo.register(Ciphertext.class);
        kryo.register(Plaintext.class);
        kryo.register(SerializedCiphertextObject.class);
    }
}
