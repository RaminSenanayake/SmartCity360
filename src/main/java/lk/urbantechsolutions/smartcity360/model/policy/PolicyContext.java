package lk.urbantechsolutions.smartcity360.model.policy;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.Objects;

/** Facts supplied by the caller: measured noise in dB and local city time. */
public record PolicyContext(BigDecimal noiseLevel, LocalTime time) {
    public PolicyContext {
        Objects.requireNonNull(noiseLevel, "Noise level is required");
        Objects.requireNonNull(time, "Local time is required");
        if (noiseLevel.signum() < 0) {
            throw new IllegalArgumentException("Noise level must be non-negative");
        }
    }
}
