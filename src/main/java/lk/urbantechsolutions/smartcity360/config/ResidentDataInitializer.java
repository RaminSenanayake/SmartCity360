package lk.urbantechsolutions.smartcity360.config;

import lk.urbantechsolutions.smartcity360.repository.ResidentRepository;
import lk.urbantechsolutions.smartcity360.service.ResidentService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;

@Configuration
public class ResidentDataInitializer {

    @Bean
    @Order(3)
    CommandLineRunner seedResidents(ResidentRepository residentRepository, ResidentService residentService,
                                    TransactionTemplate transactionTemplate) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
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

            names.forEach(residentService::addResident);
        });
    }
}
