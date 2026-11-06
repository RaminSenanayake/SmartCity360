package lk.urbantechsolutions.smartcity360.model.policy;

import java.time.LocalTime;
import java.util.Objects;

/** Terminal expression: start is inclusive, end exclusive; overnight ranges wrap midnight. */
public record ParkingHoursExpression(LocalTime start, LocalTime end) implements PolicyExpression {
    public ParkingHoursExpression {
        Objects.requireNonNull(start);
        Objects.requireNonNull(end);
        if (start.equals(end)) throw new IllegalArgumentException("Parking start and end must differ");
    }

    @Override
    public boolean interpret(PolicyContext context) {
        LocalTime time = context.time();
        return start.isBefore(end)
                ? !time.isBefore(start) && time.isBefore(end)
                : !time.isBefore(start) || time.isBefore(end);
    }
}
