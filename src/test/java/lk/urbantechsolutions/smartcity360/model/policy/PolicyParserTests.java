package lk.urbantechsolutions.smartcity360.model.policy;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalTime;

import static org.junit.jupiter.api.Assertions.*;

class PolicyParserTests {
    private final PolicyParser parser = new PolicyParser();

    private boolean evaluate(String rule, String noise, String time) {
        return parser.parse(rule).interpret(new PolicyContext(new BigDecimal(noise), LocalTime.parse(time)));
    }

    @Test
    void noiseComparisonsHandleThresholdsAndDecimalScale() {
        assertTrue(evaluate("NOISE <= 55", "55.0", "12:00"));
        assertFalse(evaluate("NOISE < 55", "55.0", "12:00"));
        assertTrue(evaluate("NOISE == 55.00", "55", "12:00"));
        assertFalse(evaluate("NOISE != 55", "55.00", "12:00"));
        assertTrue(evaluate("NOISE >= 55", "55", "12:00"));
        assertTrue(evaluate("NOISE > 55", "55.01", "12:00"));
        assertFalse(evaluate("NOISE <= 55", "55.01", "12:00"));
        assertTrue(evaluate("NOISE != 55", "54", "12:00"));
    }

    @Test
    void parkingStartsInclusivelyAndEndsExclusively() {
        String rule = "PARKING BETWEEN 08:00 AND 18:00";
        assertFalse(evaluate(rule, "0", "07:59"));
        assertTrue(evaluate(rule, "0", "08:00"));
        assertTrue(evaluate(rule, "0", "17:59:59"));
        assertFalse(evaluate(rule, "0", "18:00"));
    }

    @Test
    void overnightParkingWrapsMidnight() {
        String rule = "PARKING BETWEEN 22:00 AND 06:00";
        assertTrue(evaluate(rule, "0", "22:00"));
        assertTrue(evaluate(rule, "0", "00:00"));
        assertTrue(evaluate(rule, "0", "05:59"));
        assertFalse(evaluate(rule, "0", "06:00"));
        assertFalse(evaluate(rule, "0", "12:00"));
    }

    @Test
    void interpreterHonorsNotAndOrPrecedenceAndParentheses() {
        assertTrue(evaluate("NOISE < 10 OR NOISE < 60 AND PARKING BETWEEN 08:00 AND 18:00", "5", "23:00"));
        assertFalse(evaluate("(NOISE < 10 OR NOISE < 60) AND PARKING BETWEEN 08:00 AND 18:00", "5", "23:00"));
        assertTrue(evaluate("NOT NOISE > 55 AND PARKING BETWEEN 08:00 AND 18:00", "50", "12:00"));
        assertFalse(evaluate("NOT (NOISE > 55 OR PARKING BETWEEN 08:00 AND 18:00)", "50", "12:00"));
        assertTrue(evaluate("NOT NOT NOISE <= 55", "50", "12:00"));
        assertTrue(evaluate("NOISE <= 45 AND PARKING BETWEEN 22:00 AND 06:00", "40", "23:00"));
        assertFalse(evaluate("NOISE <= 45 AND PARKING BETWEEN 22:00 AND 06:00", "50", "23:00"));
    }

    @Test
    void grammarIsCaseInsensitiveAndAcceptsWhitespace() {
        assertTrue(evaluate("  noise<=55.5\n AnD (parking between 08:00 and 18:00) ", "55.50", "12:00"));
    }

    @Test
    void malformedRulesAreRejectedWithPositionsRatherThanPartiallyEvaluated() {
        for (String rule : new String[]{null, "", "  ", "NOISE", "NOISE = 55", "NOISE <= -1",
                "NOISE <= 1.2.3", "NOISE <= abc", "TEMPERATURE < 40", "NOISE <= 55 trailing",
                "NOISE <= 55 AND", "OR NOISE <= 55", "(NOISE <= 55", "NOISE <= 55)",
                "()", "NOT", "PARKING BETWEEN 24:00 AND 06:00", "PARKING BETWEEN 08:60 AND 18:00",
                "PARKING BETWEEN 8:00 AND 18:00", "PARKING BETWEEN 08:00 OR 18:00",
                "PARKING BETWEEN 08:00 AND 08:00", "PARKING BETWEEN 08:00 AND", "NOISE <= 55;"}) {
            PolicySyntaxException exception = assertThrows(PolicySyntaxException.class, () -> parser.parse(rule), rule);
            assertTrue(exception.getMessage().contains("position"));
        }
    }

    @Test
    void excessiveInputAndNestingAreRejected() {
        assertThrows(PolicySyntaxException.class, () -> parser.parse(" ".repeat(4097)));
        assertThrows(PolicySyntaxException.class, () -> parser.parse("NOT ".repeat(66) + "NOISE < 55"));
        assertThrows(PolicySyntaxException.class, () -> parser.parse("(".repeat(66) + "NOISE < 55" + ")".repeat(66)));
        assertThrows(PolicySyntaxException.class, () -> parser.parse("NOISE < 1 OR ".repeat(130) + "NOISE < 1"));
    }

    @Test
    void invalidRuntimeFactsCannotAccidentallyPassARule() {
        assertThrows(IllegalArgumentException.class, () -> new PolicyContext(new BigDecimal("-1"), LocalTime.NOON));
        assertThrows(NullPointerException.class, () -> new PolicyContext(null, LocalTime.NOON));
        assertThrows(NullPointerException.class, () -> new PolicyContext(BigDecimal.ZERO, null));
    }

    @Test
    void compiledTreeCanBeReusedWithDifferentRuntimeContexts() {
        PolicyExpression expression = parser.parse("NOISE <= 55");
        assertTrue(expression instanceof NoiseLimitExpression);
        assertTrue(parser.parse("NOISE <= 55 AND NOT PARKING BETWEEN 22:00 AND 06:00") instanceof AndExpression);
        assertTrue(expression.interpret(new PolicyContext(new BigDecimal("50"), LocalTime.NOON)));
        assertFalse(expression.interpret(new PolicyContext(new BigDecimal("60"), LocalTime.NOON)));
    }
}
