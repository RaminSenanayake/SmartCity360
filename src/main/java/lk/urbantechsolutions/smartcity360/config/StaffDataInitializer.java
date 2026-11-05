package lk.urbantechsolutions.smartcity360.config;

import lk.urbantechsolutions.smartcity360.model.role.CityManager;
import lk.urbantechsolutions.smartcity360.model.role.Supervisor;
import lk.urbantechsolutions.smartcity360.service.CityManagerService;
import lk.urbantechsolutions.smartcity360.service.LocalStaffService;
import lk.urbantechsolutions.smartcity360.service.SupervisorService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.support.TransactionTemplate;

@Configuration
public class StaffDataInitializer {
    @Bean
    @Order(1)
    CommandLineRunner seedStaff(CityManagerService cityManagerService,
                                SupervisorService supervisorService,
                                LocalStaffService localStaffService,
                                TransactionTemplate transactionTemplate) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
            if (!cityManagerService.getAll().isEmpty()) {
                return;
            }

            // City managers
            CityManager manager1 = cityManagerService.add("Nimal Perera");
            CityManager manager2 = cityManagerService.add("Chandani Gunawardena");
            CityManager manager3 = cityManagerService.add("Roshan Abeysekera");
            CityManager manager4 = cityManagerService.add("Priyanka Wijesinghe");

            // Supervisors, two per manager
            Supervisor sup1 = supervisorService.add("Kamal Silva", manager1.getId());
            Supervisor sup2 = supervisorService.add("Sunil Fernando", manager1.getId());
            Supervisor sup3 = supervisorService.add("Malini De Alwis", manager2.getId());
            Supervisor sup4 = supervisorService.add("Tharindu Bandara", manager2.getId());
            Supervisor sup5 = supervisorService.add("Nadeesha Karunaratne", manager3.getId());
            Supervisor sup6 = supervisorService.add("Lasantha Herath", manager3.getId());
            Supervisor sup7 = supervisorService.add("Shanika Dissanayake", manager4.getId());
            Supervisor sup8 = supervisorService.add("Buddhika Senanayake", manager4.getId());

            // Local staff, two per supervisor
            localStaffService.add("Ayesha Jayasuriya", sup1.getId());
            localStaffService.add("Ruwan Wickramasinghe", sup1.getId());
            localStaffService.add("Dilini Rajapaksa", sup2.getId());
            localStaffService.add("Pasan Liyanage", sup2.getId());
            localStaffService.add("Ishara Gamage", sup3.getId());
            localStaffService.add("Nuwan Ekanayake", sup3.getId());
            localStaffService.add("Hiruni Amarasinghe", sup4.getId());
            localStaffService.add("Chamara Jayawardena", sup4.getId());
            localStaffService.add("Sachini Weerasinghe", sup5.getId());
            localStaffService.add("Dinesh Rathnayake", sup5.getId());
            localStaffService.add("Thilini Samarasinghe", sup6.getId());
            localStaffService.add("Kasun Madushanka", sup6.getId());
            localStaffService.add("Anushka Peiris", sup7.getId());
            localStaffService.add("Mahesh Gunasekara", sup7.getId());
            localStaffService.add("Sewwandi Cooray", sup8.getId());
            localStaffService.add("Janaka Premadasa", sup8.getId());
        });
    }

}
