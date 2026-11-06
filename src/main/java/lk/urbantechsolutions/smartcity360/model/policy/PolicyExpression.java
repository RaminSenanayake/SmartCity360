package lk.urbantechsolutions.smartcity360.model.policy;

/** Interpreter pattern's abstract expression. True means the supplied facts satisfy this rule. */
public interface PolicyExpression {
    boolean interpret(PolicyContext context);
}
