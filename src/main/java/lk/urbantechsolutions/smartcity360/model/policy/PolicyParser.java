package lk.urbantechsolutions.smartcity360.model.policy;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Recursive-descent parser producing an Interpreter expression tree; parsing state is per call. */
public class PolicyParser {
    private static final Pattern TOKEN = Pattern.compile(
            "[A-Za-z_]+|[0-9]+(?::[0-9]+|\\.[0-9]+)?|<=|>=|==|!=|[<>()]");

    public PolicyExpression parse(String source) {
        if (source == null || source.isBlank()) throw new PolicySyntaxException("Policy is empty", 0);
        if (source.length() > 4096) throw new PolicySyntaxException("Policy exceeds 4096 characters", 4096);
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = TOKEN.matcher(source);
        for (int position = 0; position < source.length();) {
            if (Character.isWhitespace(source.charAt(position))) {
                position++;
                continue;
            }
            matcher.region(position, source.length());
            if (!matcher.lookingAt()) throw new PolicySyntaxException("Unexpected character", position);
            tokens.add(new Token(matcher.group().toUpperCase(Locale.ROOT), position));
            if (tokens.size() > 512) throw new PolicySyntaxException("Policy exceeds 512 tokens", position);
            position = matcher.end();
        }
        tokens.add(new Token("<END>", source.length()));
        Cursor cursor = new Cursor(tokens);
        PolicyExpression expression = cursor.parseOr(0);
        cursor.expect("<END>");
        return expression;
    }

    private record Token(String text, int position) {}

    private static class Cursor {
        private final List<Token> tokens;
        private int index;

        Cursor(List<Token> tokens) { this.tokens = tokens; }
        Token current() { return tokens.get(index); }

        boolean accept(String text) {
            if (current().text().equals(text)) {
                index++;
                return true;
            }
            return false;
        }

        void expect(String text) {
            if (!accept(text)) throw error("Expected " + text + ", found " + current().text());
        }

        PolicySyntaxException error(String message) {
            return new PolicySyntaxException(message, current().position());
        }

        PolicyExpression parseOr(int depth) {
            PolicyExpression expression = parseAnd(depth);
            while (accept("OR")) expression = new OrExpression(expression, parseAnd(depth));
            return expression;
        }

        PolicyExpression parseAnd(int depth) {
            PolicyExpression expression = parseUnary(depth);
            while (accept("AND")) expression = new AndExpression(expression, parseUnary(depth));
            return expression;
        }

        PolicyExpression parseUnary(int depth) {
            if (depth > 64) throw error("Policy nesting exceeds 64 levels");
            if (accept("NOT")) return new NotExpression(parseUnary(depth + 1));
            if (accept("(")) {
                PolicyExpression expression = parseOr(depth + 1);
                expect(")");
                return expression;
            }
            if (accept("NOISE")) return parseNoise();
            if (accept("PARKING")) {
                expect("BETWEEN");
                LocalTime start = parseTime();
                expect("AND");
                LocalTime end = parseTime();
                if (start.equals(end)) throw error("Parking start and end must differ");
                return new ParkingHoursExpression(start, end);
            }
            throw error("Expected NOISE, PARKING, NOT or (");
        }

        PolicyExpression parseNoise() {
            NoiseLimitExpression.Comparison comparison = switch (current().text()) {
                case "<" -> NoiseLimitExpression.Comparison.LESS;
                case "<=" -> NoiseLimitExpression.Comparison.LESS_OR_EQUAL;
                case "==" -> NoiseLimitExpression.Comparison.EQUAL;
                case "!=" -> NoiseLimitExpression.Comparison.NOT_EQUAL;
                case ">=" -> NoiseLimitExpression.Comparison.GREATER_OR_EQUAL;
                case ">" -> NoiseLimitExpression.Comparison.GREATER;
                default -> throw error("Expected a noise comparison (<, <=, ==, !=, >=, >)");
            };
            index++;
            if (!current().text().matches("[0-9]+(?:\\.[0-9]+)?")) {
                throw error("Expected a non-negative noise threshold");
            }
            BigDecimal limit = new BigDecimal(current().text());
            index++;
            return new NoiseLimitExpression(comparison, limit);
        }

        LocalTime parseTime() {
            if (!current().text().matches("[0-9]{2}:[0-9]{2}")) throw error("Expected HH:mm time");
            try {
                LocalTime time = LocalTime.parse(current().text());
                index++;
                return time;
            } catch (DateTimeParseException exception) {
                throw error("Invalid time; use 00:00 through 23:59");
            }
        }
    }
}
