package lk.urbantechsolutions.smartcity360.model.policy;

import java.util.Objects;

/** Nonterminal expression negating another rule. */
public record NotExpression(PolicyExpression expression) implements PolicyExpression {
    public NotExpression {
        Objects.requireNonNull(expression);
    }

    @Override
    public boolean interpret(PolicyContext context) {
        return !expression.interpret(context);
    }
}
