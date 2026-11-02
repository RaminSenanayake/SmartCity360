package lk.urbantechsolutions.smartcity360.model.zone;

import java.math.BigDecimal;

public interface Zone {
    Long getId();
    String getName();
    void setName(String name);
    boolean isPowerOn();
    void turnOnPower();
    void shutDownPower();
    BigDecimal getPowerUsage();
}
