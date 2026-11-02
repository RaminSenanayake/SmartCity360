package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.zone.ZoneType;

import java.math.BigDecimal;

public interface PowerControlService {
    void turnOnPower(Long zoneId, ZoneType zoneType);
    void turnOffPower(Long zoneId, ZoneType zoneType);
    BigDecimal getUsage(Long zoneId, ZoneType zoneType);
}
