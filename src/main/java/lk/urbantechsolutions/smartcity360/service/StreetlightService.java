package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.streetlight.LightDistribution;
import lk.urbantechsolutions.smartcity360.model.streetlight.Streetlight;

import java.math.BigDecimal;
import java.util.List;

public interface StreetlightService {
    List<Streetlight> getStreetlights(Long streetId);
    Streetlight getStreetlight(Long id);
    void addStreetlight(Long streetId, BigDecimal longitude, BigDecimal latitude,
                        BigDecimal wattage, String color, int colorTemperature,
                        LightDistribution distribution);
    void updateStreetlight(Streetlight streetlight);
    void removeStreetlight(Streetlight streetlight);
}
