package lk.urbantechsolutions.smartcity360.config;

import lk.urbantechsolutions.smartcity360.model.resident.Resident;
import lk.urbantechsolutions.smartcity360.repository.ComplaintRepository;
import lk.urbantechsolutions.smartcity360.repository.ResidentRepository;
import lk.urbantechsolutions.smartcity360.service.ComplaintHandlingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
public class ComplaintDataInitializer {

    private static final int COMPLAINT_COUNT = 30;

    private static final List<String> SAMPLES = List.of(
            "Streetlight flickering outside my house",
            "Garbage bin was not emptied this week",
            "Pothole forming near the bus stop",
            "Overgrown tree branches blocking the footpath",
            "Noise from a construction site after 10 PM",
            "Blocked drain causing water to pool on the road",
            "Repeated power cuts in our street this month",
            "Illegal parking blocking the building entrance",
            "Fallen electric pole across the main road",
            "Sewage overflow flooding several houses",
            "Exposed live wires near a children's playground"
    );

    @Bean
    @Order(4) // after the staff seeder (1) and the resident seeder (3)
    CommandLineRunner seedComplaints(ResidentRepository residentRepository,
                                     ComplaintRepository complaintRepository,
                                     ComplaintHandlingService complaintHandlingService,
                                     TransactionTemplate transactionTemplate) {
        return args -> transactionTemplate.executeWithoutResult(status -> {
            if (complaintRepository.count() > 0) {
                return;
            }

            List<Resident> residents = residentRepository.findAll();
            if (residents.isEmpty()) {
                System.out.println("Skipping complaint seeding: residents missing");
                return;
            }

            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (int i = 0; i < COMPLAINT_COUNT; i++) {
                Resident resident = residents.get(random.nextInt(residents.size()));
                String sample = SAMPLES.get(random.nextInt(SAMPLES.size()));

                complaintHandlingService.submitComplaint(resident.getId(), sample);
            }
        });
    }
}
