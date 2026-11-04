package lk.urbantechsolutions.smartcity360.model.streetlight;

import lk.urbantechsolutions.smartcity360.repository.SharedStreetlightRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class StreetlightFactory {

    private record Key(BigDecimal wattage, String color,
                       int colorTemperature, LightDistribution distribution) {
    }

    private final SharedStreetlightRepository repository;
    private final Map<Key, SharedStreetlight> cache = new ConcurrentHashMap<>();

    public StreetlightFactory(SharedStreetlightRepository repository) {
        this.repository = repository;
    }

    public SharedStreetlight getSharedStreetlight(BigDecimal wattage, String color,
                                                  int colorTemperature, LightDistribution distribution) {

        BigDecimal w = wattage.setScale(2, RoundingMode.HALF_UP);
        Key key = new Key(w, color, colorTemperature, distribution);

        return cache.computeIfAbsent(key, k ->
                repository.findByWattageAndColorAndColorTemperatureAndDistribution(w, color, colorTemperature, distribution)
                        .orElseGet(() -> repository.save(
                                new SharedStreetlight(w, color, colorTemperature, distribution)
                        ))
        );
    }
}