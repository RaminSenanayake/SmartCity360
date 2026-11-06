package lk.urbantechsolutions.smartcity360.model.policy;

import java.util.Objects;

/** Nonterminal expression combining two rules with AND. */
public record AndExpression(PolicyExpression left, PolicyExpression right) implements PolicyExpression {
    public AndExpression {
        Objects.requireNonNull(left);
        Objects.requireNonNull(right);
    }

    @Override
    public boolean interpret(PolicyContext context) {
        return left.interpret(context) && right.interpret(context);
    }
}
