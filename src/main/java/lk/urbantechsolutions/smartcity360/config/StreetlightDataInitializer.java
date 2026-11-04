package lk.urbantechsolutions.smartcity360.config;

import lk.urbantechsolutions.smartcity360.model.streetlight.LightDistribution;
import lk.urbantechsolutions.smartcity360.model.zone.Street;
import lk.urbantechsolutions.smartcity360.repository.StreetRepository;
import lk.urbantechsolutions.smartcity360.repository.StreetlightRepository;
import lk.urbantechsolutions.smartcity360.service.StreetlightService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
public class StreetlightDataInitializer {

    private static final int LIGHTS_PER_STREET = 25;

    // The few distinct configurations (the flyweights)
    private record LightType(BigDecimal wattage, String color,
                             int colorTemperature, LightDistribution distribution) {}

    private static final List<LightType> TYPES = List.of(
            new LightType(new BigDecimal("100"), "White", 4000, LightDistribution.III),
            new LightType(new BigDecimal("150"), "Warm White", 3000, LightDistribution.II),
            new LightType(new BigDecimal("70"), "Cool White", 5000, LightDistribution.IV)
    );

    @Bean
    @Order(2) // runs after the zone seeder, so streets already exist
    CommandLineRunner seedStreetlights(StreetRepository streetRepository,
                                       StreetlightRepository streetlightRepository,
                                       StreetlightService streetlightService) {
        return args -> {
            if (streetlightRepository.count() > 0) {
                return; // already seeded
            }

            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (Street street : streetRepository.findAll()) {
                // a base point per street, so lights on one street sit close together
                BigDecimal baseLongitude = BigDecimal.valueOf(79.83 + random.nextDouble(0, 0.05))
                        .setScale(6, RoundingMode.HALF_UP);
                BigDecimal baseLatitude = BigDecimal.valueOf(7.20 + random.nextDouble(0, 0.05))
                        .setScale(6, RoundingMode.HALF_UP);

                for (int i = 0; i < LIGHTS_PER_STREET; i++) {
                    LightType type = TYPES.get(i % TYPES.size());

                    // extrinsic state: a unique position for every light
                    BigDecimal longitude = baseLongitude
                            .add(BigDecimal.valueOf(i).multiply(new BigDecimal("0.0003")))
                            .setScale(6, RoundingMode.HALF_UP);
                    BigDecimal latitude = baseLatitude
                            .add(BigDecimal.valueOf(random.nextDouble(-0.0001, 0.0001)))
                            .setScale(6, RoundingMode.HALF_UP);

                    streetlightService.addStreetlight(
                            street.getId(),
                            longitude,
                            latitude,
                            type.wattage(),
                            type.color(),
                            type.colorTemperature(),
                            type.distribution());
                }
            }
        };
    }
}