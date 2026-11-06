package lk.urbantechsolutions.smartcity360.config;

import lk.urbantechsolutions.smartcity360.repository.CityPolicyRepository;
import lk.urbantechsolutions.smartcity360.service.CityPolicyService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class PolicyDataInitializer {
    @Bean
    @Order(5)
    CommandLineRunner seedPolicies(CityPolicyRepository repository, CityPolicyService service,
                                   TransactionTemplate transactionTemplate) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
            if (repository.count() > 0) return;
            service.add("Residential noise limit", "NOISE <= 55");
            service.add("Daytime parking", "PARKING BETWEEN 08:00 AND 18:00");
            service.add("Quiet overnight parking", "NOISE <= 45 AND PARKING BETWEEN 22:00 AND 06:00");
        });
    }
}
