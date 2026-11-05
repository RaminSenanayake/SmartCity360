package lk.urbantechsolutions.smartcity360.model.energymeter;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/** Checks the caller's token before invoking the wrapped stream. */
public class AuthenticationDecorator extends EnergyMeterStreamDecorator {
    private final String providedToken;
    private final String expectedToken;

    public AuthenticationDecorator(EnergyMeterStream wrapped, String providedToken, String expectedToken) {
        super(wrapped);
        if (expectedToken == null || expectedToken.isBlank()) {
            throw new IllegalStateException("Energy meter access token is not configured");
        }
        this.providedToken = providedToken;
        this.expectedToken = expectedToken;
    }

    @Override
    public byte[] readData() {
        if (providedToken == null || !MessageDigest.isEqual(hash(expectedToken), hash(providedToken))) {
            throw new SecurityException("Invalid energy meter access token");
        }
        return super.readData();
    }

    private byte[] hash(String token) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(token.getBytes(StandardCharsets.UTF_8));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }
}
