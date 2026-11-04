package lk.urbantechsolutions.smartcity360.config;

import lk.urbantechsolutions.smartcity360.model.resident.ComplaintSeverity;
import lk.urbantechsolutions.smartcity360.model.resident.Resident;
import lk.urbantechsolutions.smartcity360.repository.ComplaintRepository;
import lk.urbantechsolutions.smartcity360.repository.LocalStaffRepository;
import lk.urbantechsolutions.smartcity360.repository.ResidentRepository;
import lk.urbantechsolutions.smartcity360.service.ComplaintHandlingService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Configuration
public class ComplaintDataInitializer {

    private static final int COMPLAINT_COUNT = 30;

    private record Sample(String text, ComplaintSeverity severity) {}

    private static final List<Sample> SAMPLES = List.of(
            // LOW
            new Sample("Streetlight flickering outside my house", ComplaintSeverity.LOW),
            new Sample("Garbage bin was not emptied this week", ComplaintSeverity.LOW),
            new Sample("Pothole forming near the bus stop", ComplaintSeverity.LOW),
            new Sample("Overgrown tree branches blocking the footpath", ComplaintSeverity.LOW),
            // MEDIUM
            new Sample("Noise from a construction site after 10 PM", ComplaintSeverity.MEDIUM),
            new Sample("Blocked drain causing water to pool on the road", ComplaintSeverity.MEDIUM),
            new Sample("Repeated power cuts in our street this month", ComplaintSeverity.MEDIUM),
            new Sample("Illegal parking blocking the building entrance", ComplaintSeverity.MEDIUM),
            // HIGH
            new Sample("Fallen electric pole across the main road", ComplaintSeverity.HIGH),
            new Sample("Sewage overflow flooding several houses", ComplaintSeverity.HIGH),
            new Sample("Exposed live wires near a children's playground", ComplaintSeverity.HIGH)
    );

    @Bean
    @Order(4) // after the staff seeder (1) and the resident seeder (3)
    CommandLineRunner seedComplaints(ResidentRepository residentRepository,
                                     ComplaintRepository complaintRepository,
                                     LocalStaffRepository localStaffRepository,
                                     ComplaintHandlingService complaintHandlingService) {
        return args -> {
            if (complaintRepository.count() > 0) {
                return;
            }

            List<Resident> residents = residentRepository.findAll();
            if (residents.isEmpty() || localStaffRepository.count() == 0) {
                System.out.println("Skipping complaint seeding: residents or local staff missing");
                return;
            }

            ThreadLocalRandom random = ThreadLocalRandom.current();

            for (int i = 0; i < COMPLAINT_COUNT; i++) {
                Resident resident = residents.get(random.nextInt(residents.size()));
                Sample sample = SAMPLES.get(random.nextInt(SAMPLES.size()));

                complaintHandlingService.handleComplaint(
                        resident.getId(), sample.text(), sample.severity());
            }
        };
    }
}