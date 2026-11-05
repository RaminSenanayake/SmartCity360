package lk.urbantechsolutions.smartcity360.config;

import lk.urbantechsolutions.smartcity360.model.role.CityManager;
import lk.urbantechsolutions.smartcity360.model.role.Staff;
import lk.urbantechsolutions.smartcity360.model.role.StaffType;
import lk.urbantechsolutions.smartcity360.model.role.Supervisor;
import lk.urbantechsolutions.smartcity360.service.CityManagerService;
import lk.urbantechsolutions.smartcity360.service.LocalStaffService;
import lk.urbantechsolutions.smartcity360.service.SupervisorService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

@Configuration
public class StaffDataInitializer {
    @Bean
    @Order(1)
    CommandLineRunner seedStaff(CityManagerService cityManagerService,
                                SupervisorService supervisorService,
                                LocalStaffService localStaffService,
                                TransactionTemplate transactionTemplate,
                                @Value("${seed.staff-token-file:.seed/staff-access-tokens.properties}") String tokenFile) {
        return args -> {
            Properties tokens = transactionTemplate.execute(status -> {
                if (!cityManagerService.getAll().isEmpty()) {
                    return new Properties();
                }

                Properties credentials = new Properties();

                // City managers
                CityManager manager1 = cityManagerService.add("Nimal Perera");
                provision(credentials, StaffType.CITY_MANAGER, manager1);
                CityManager manager2 = cityManagerService.add("Chandani Gunawardena");
                provision(credentials, StaffType.CITY_MANAGER, manager2);
                CityManager manager3 = cityManagerService.add("Roshan Abeysekera");
                provision(credentials, StaffType.CITY_MANAGER, manager3);
                CityManager manager4 = cityManagerService.add("Priyanka Wijesinghe");
                provision(credentials, StaffType.CITY_MANAGER, manager4);

                // Supervisors, two per manager
                Supervisor sup1 = supervisorService.add("Kamal Silva", manager1.getId());
                provision(credentials, StaffType.SUPERVISOR, sup1);
                Supervisor sup2 = supervisorService.add("Sunil Fernando", manager1.getId());
                provision(credentials, StaffType.SUPERVISOR, sup2);
                Supervisor sup3 = supervisorService.add("Malini De Alwis", manager2.getId());
                provision(credentials, StaffType.SUPERVISOR, sup3);
                Supervisor sup4 = supervisorService.add("Tharindu Bandara", manager2.getId());
                provision(credentials, StaffType.SUPERVISOR, sup4);
                Supervisor sup5 = supervisorService.add("Nadeesha Karunaratne", manager3.getId());
                provision(credentials, StaffType.SUPERVISOR, sup5);
                Supervisor sup6 = supervisorService.add("Lasantha Herath", manager3.getId());
                provision(credentials, StaffType.SUPERVISOR, sup6);
                Supervisor sup7 = supervisorService.add("Shanika Dissanayake", manager4.getId());
                provision(credentials, StaffType.SUPERVISOR, sup7);
                Supervisor sup8 = supervisorService.add("Buddhika Senanayake", manager4.getId());
                provision(credentials, StaffType.SUPERVISOR, sup8);

                // Local staff, two per supervisor
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Ayesha Jayasuriya", sup1.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Ruwan Wickramasinghe", sup1.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Dilini Rajapaksa", sup2.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Pasan Liyanage", sup2.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Ishara Gamage", sup3.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Nuwan Ekanayake", sup3.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Hiruni Amarasinghe", sup4.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Chamara Jayawardena", sup4.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Sachini Weerasinghe", sup5.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Dinesh Rathnayake", sup5.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Thilini Samarasinghe", sup6.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Kasun Madushanka", sup6.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Anushka Peiris", sup7.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Mahesh Gunasekara", sup7.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Sewwandi Cooray", sup8.getId()));
                provision(credentials, StaffType.LOCAL_STAFF, localStaffService.add("Janaka Premadasa", sup8.getId()));
                return credentials;
            });
            if (tokens != null && !tokens.isEmpty()) {
                writeTokens(Path.of(tokenFile), tokens);
            }
        };
    }

    private void provision(Properties tokens, StaffType type, Staff staff) {
        tokens.setProperty(type.name() + "." + staff.getId(), staff.rotateAccessToken());
    }

    private void writeTokens(Path file, Properties tokens) throws IOException {
        Path parent = file.toAbsolutePath().getParent();
        Files.createDirectories(parent);
        try (OutputStream output = Files.newOutputStream(file)) {
            tokens.store(output, "Local demo staff tokens: keys are STAFF_TYPE.staffId. Keep this file private.");
        }
    }

}
