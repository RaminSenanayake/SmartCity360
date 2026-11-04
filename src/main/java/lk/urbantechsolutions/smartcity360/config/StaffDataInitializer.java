package lk.urbantechsolutions.smartcity360.config;

import lk.urbantechsolutions.smartcity360.model.role.CityManager;
import lk.urbantechsolutions.smartcity360.model.role.LocalStaff;
import lk.urbantechsolutions.smartcity360.model.role.Supervisor;
import lk.urbantechsolutions.smartcity360.repository.CityManagerRepository;
import lk.urbantechsolutions.smartcity360.repository.LocalStaffRepository;
import lk.urbantechsolutions.smartcity360.repository.SupervisorRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

@Configuration
public class StaffDataInitializer {
    @Bean
    @Order(1)
    CommandLineRunner seedStaff(CityManagerRepository cityManagerRepository,
                                SupervisorRepository supervisorRepository,
                                LocalStaffRepository localStaffRepository) {
        return args -> {
            if (cityManagerRepository.count() > 0) {
                return;
            }

            // City managers
            CityManager manager1 = createManager(cityManagerRepository, "Nimal Perera");
            CityManager manager2 = createManager(cityManagerRepository, "Chandani Gunawardena");
            CityManager manager3 = createManager(cityManagerRepository, "Roshan Abeysekera");
            CityManager manager4 = createManager(cityManagerRepository, "Priyanka Wijesinghe");

            // Supervisors, two per manager
            Supervisor sup1 = createSupervisor(supervisorRepository, "Kamal Silva", manager1);
            Supervisor sup2 = createSupervisor(supervisorRepository, "Sunil Fernando", manager1);
            Supervisor sup3 = createSupervisor(supervisorRepository, "Malini De Alwis", manager2);
            Supervisor sup4 = createSupervisor(supervisorRepository, "Tharindu Bandara", manager2);
            Supervisor sup5 = createSupervisor(supervisorRepository, "Nadeesha Karunaratne", manager3);
            Supervisor sup6 = createSupervisor(supervisorRepository, "Lasantha Herath", manager3);
            Supervisor sup7 = createSupervisor(supervisorRepository, "Shanika Dissanayake", manager4);
            Supervisor sup8 = createSupervisor(supervisorRepository, "Buddhika Senanayake", manager4);

            // Local staff, two per supervisor
            createLocalStaff(localStaffRepository, "Ayesha Jayasuriya", sup1);
            createLocalStaff(localStaffRepository, "Ruwan Wickramasinghe", sup1);
            createLocalStaff(localStaffRepository, "Dilini Rajapaksa", sup2);
            createLocalStaff(localStaffRepository, "Pasan Liyanage", sup2);
            createLocalStaff(localStaffRepository, "Ishara Gamage", sup3);
            createLocalStaff(localStaffRepository, "Nuwan Ekanayake", sup3);
            createLocalStaff(localStaffRepository, "Hiruni Amarasinghe", sup4);
            createLocalStaff(localStaffRepository, "Chamara Jayawardena", sup4);
            createLocalStaff(localStaffRepository, "Sachini Weerasinghe", sup5);
            createLocalStaff(localStaffRepository, "Dinesh Rathnayake", sup5);
            createLocalStaff(localStaffRepository, "Thilini Samarasinghe", sup6);
            createLocalStaff(localStaffRepository, "Kasun Madushanka", sup6);
            createLocalStaff(localStaffRepository, "Anushka Peiris", sup7);
            createLocalStaff(localStaffRepository, "Mahesh Gunasekara", sup7);
            createLocalStaff(localStaffRepository, "Sewwandi Cooray", sup8);
            createLocalStaff(localStaffRepository, "Janaka Premadasa", sup8);
        };
    }

    private CityManager createManager(CityManagerRepository repo, String name) {
        CityManager manager = new CityManager();
        manager.setName(name);
        return repo.save(manager);
    }

    private Supervisor createSupervisor(SupervisorRepository repo, String name, CityManager manager) {
        Supervisor supervisor = new Supervisor();
        supervisor.setName(name);
        supervisor.setCityManager(manager);
        return repo.save(supervisor);
    }

    private LocalStaff createLocalStaff(LocalStaffRepository repo, String name, Supervisor supervisor) {
        LocalStaff staff = new LocalStaff();
        staff.setName(name);
        staff.setSupervisor(supervisor);
        return repo.save(staff);
    }
}
