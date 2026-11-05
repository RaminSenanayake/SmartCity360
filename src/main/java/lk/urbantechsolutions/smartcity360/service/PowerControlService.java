package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.zone.ZoneType;
import lk.urbantechsolutions.smartcity360.model.role.StaffIdentity;

import java.math.BigDecimal;

public interface PowerControlService {
    void turnOnPower(Long zoneId, ZoneType zoneType);
    void turnOffPower(Long zoneId, ZoneType zoneType);
    /** Numeric aggregate usage. BUILDING requires the credentialed overload below. */
    BigDecimal getUsage(Long zoneId, ZoneType zoneType);
    /** Building JSON, optionally GZIP-compressed then AES-GCM-encrypted; staff authentication is mandatory.
     * Other zone types return a plain UTF-8 numeric total and reject compression/encryption options.
     */
    byte[] getUsage(Long zoneId, ZoneType zoneType, StaffIdentity staff, String token,
                    boolean compress, boolean encrypt);
}
