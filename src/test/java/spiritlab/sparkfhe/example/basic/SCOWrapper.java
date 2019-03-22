package spiritlab.sparkfhe.example.basic;

import spiritlab.sparkfhe.api.SerializedCiphertextObject;

import java.io.Serializable;

public interface SCOWrapper extends Serializable {
    public SerializedCiphertextObject create();
}
