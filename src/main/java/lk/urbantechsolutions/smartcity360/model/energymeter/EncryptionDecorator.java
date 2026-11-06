package lk.urbantechsolutions.smartcity360.model.energymeter;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import java.nio.ByteBuffer;
import java.security.GeneralSecurityException;
import java.security.SecureRandom;
import java.util.Objects;

/**
 * Output layout: 12-byte random nonce followed by AES-GCM ciphertext and its 16-byte tag.
 */
public class EncryptionDecorator extends EnergyMeterStreamDecorator {
    private static final SecureRandom RANDOM = new SecureRandom();
    private final SecretKey key;

    public EncryptionDecorator(EnergyMeterStream wrapped, SecretKey key) {
        super(wrapped);
        this.key = Objects.requireNonNull(key);
    }

    @Override
    public byte[] readData() {
        byte[] data = super.readData();
        byte[] nonce = new byte[12];
        RANDOM.nextBytes(nonce);
        try {
            Cipher cipher = Cipher.getInstance("AES/GCM/NoPadding");
            cipher.init(Cipher.ENCRYPT_MODE, key, new GCMParameterSpec(128, nonce));
            byte[] encrypted = cipher.doFinal(data);
            return ByteBuffer.allocate(nonce.length + encrypted.length)
                    .put(nonce).put(encrypted).array();
        } catch (GeneralSecurityException exception) {
            throw new IllegalStateException("Cannot encrypt meter reading", exception);
        }
    }
}
