package lk.urbantechsolutions.smartcity360.model.policy;

import java.util.Objects;

/** Nonterminal expression combining two rules with OR. */
public record OrExpression(PolicyExpression left, PolicyExpression right) implements PolicyExpression {
    public OrExpression {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
    }

    @Override
    public boolean interpret(PolicyContext context) {
        return left.interpret(context) || right.interpret(context);
    }
}
