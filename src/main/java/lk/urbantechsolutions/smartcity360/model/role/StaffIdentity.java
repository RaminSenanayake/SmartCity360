package lk.urbantechsolutions.smartcity360.model.role;

/** Staff IDs are only unique within a role's table, so both values are required. */
public record StaffIdentity(StaffType type, Long id) {}
