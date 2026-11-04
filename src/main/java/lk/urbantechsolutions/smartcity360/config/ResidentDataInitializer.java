package lk.urbantechsolutions.smartcity360.config;

import lk.urbantechsolutions.smartcity360.model.resident.Resident;
import lk.urbantechsolutions.smartcity360.repository.ResidentRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;

@Configuration
public class ResidentDataInitializer {

    @Bean
    @Order(3)
    CommandLineRunner seedResidents(ResidentRepository residentRepository) {
        return args -> {
            if (residentRepository.count() > 0) {
                return;
            }

            List<String> names = List.of(
                    "Amila Rathnayake", "Nethmi Perera", "Sandun Fernando",
                    "Kavindi Silva", "Tharaka Jayawardena", "Ishani Wijeratne",
                    "Lakshan Gunasekara", "Dulani Samarasinghe", "Mohamed Rizwan",
                    "Fathima Nazeer", "Ravindran Selvam", "Priya Krishnan",
                    "Chathura Bandara", "Sithara Dissanayake", "Yasiru Herath"
            );

            residentRepository.saveAll(names.stream().map(Resident::new).toList());
        };
    }
}