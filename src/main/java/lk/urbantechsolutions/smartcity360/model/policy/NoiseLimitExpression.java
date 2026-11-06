package lk.urbantechsolutions.smartcity360.model.policy;

import java.math.BigDecimal;
import java.util.Objects;

/** Terminal expression comparing a measured noise level with a policy threshold. */
public record NoiseLimitExpression(Comparison comparison, BigDecimal limit) implements PolicyExpression {
    public enum Comparison { LESS, LESS_OR_EQUAL, EQUAL, NOT_EQUAL, GREATER_OR_EQUAL, GREATER }

    public NoiseLimitExpression {
        Objects.requireNonNull(comparison);
        Objects.requireNonNull(limit);
        if (limit.signum() < 0) throw new IllegalArgumentException("Noise limit must be non-negative");
    }

    @Override
    public boolean interpret(PolicyContext context) {
        int result = context.noiseLevel().compareTo(limit);
        return switch (comparison) {
            case LESS -> result < 0;
            case LESS_OR_EQUAL -> result <= 0;
            case EQUAL -> result == 0;
            case NOT_EQUAL -> result != 0;
            case GREATER_OR_EQUAL -> result >= 0;
            case GREATER -> result > 0;
        };
    }
}
