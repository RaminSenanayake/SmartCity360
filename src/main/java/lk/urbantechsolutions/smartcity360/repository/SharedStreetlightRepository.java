package lk.urbantechsolutions.smartcity360.repository;

import lk.urbantechsolutions.smartcity360.model.streetlight.LightDistribution;
import lk.urbantechsolutions.smartcity360.model.streetlight.SharedStreetlight;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.Optional;

public interface SharedStreetlightRepository extends JpaRepository<SharedStreetlight, Long> {
    Optional<SharedStreetlight> findByWattageAndColorAndColorTemperatureAndDistribution(BigDecimal wattage, String color, int colorTemperature, LightDistribution distribution);
}
