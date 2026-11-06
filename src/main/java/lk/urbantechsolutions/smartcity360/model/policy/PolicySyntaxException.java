package lk.urbantechsolutions.smartcity360.model.policy;

public class PolicySyntaxException extends IllegalArgumentException {
    public PolicySyntaxException(String message, int position) {
        super(message + " at position " + position);
    }
}
