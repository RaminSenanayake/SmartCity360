package lk.urbantechsolutions.smartcity360.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.resident.Complaint;
import lk.urbantechsolutions.smartcity360.model.resident.ComplaintSeverity;
import lk.urbantechsolutions.smartcity360.model.resident.Resident;
import lk.urbantechsolutions.smartcity360.model.role.LocalStaff;
import lk.urbantechsolutions.smartcity360.repository.ComplaintRepository;
import lk.urbantechsolutions.smartcity360.repository.LocalStaffRepository;
import lk.urbantechsolutions.smartcity360.repository.ResidentRepository;
import lk.urbantechsolutions.smartcity360.service.ComplaintHandlingService;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.concurrent.ThreadLocalRandom;

@Service
public class ComplaintHandlingServiceImpl implements ComplaintHandlingService {

    private final ResidentRepository residentRepository;
    private LocalStaffRepository localStaffRepository;
    private ComplaintRepository complaintRepository;

    public ComplaintHandlingServiceImpl(
            LocalStaffRepository localStaffRepository,
            ComplaintRepository complaintRepository,
            ResidentRepository residentRepository) {
        this.localStaffRepository = localStaffRepository;
        this.complaintRepository = complaintRepository;
        this.residentRepository = residentRepository;
    }

    @Override
    @Transactional
    public Complaint handleComplaint(Long residentId, String complaintText, ComplaintSeverity severity) {
        Resident resident = residentRepository.findById(residentId)
                .orElseThrow(() -> new EntityNotFoundException("Resident " + residentId + " not found"));

        // The builder validates resident, text and severity in build()
        Complaint complaint = new Complaint.Builder(resident, complaintText)
                .setSeverity(severity)
                .build();

        long count = localStaffRepository.count();
        if (count == 0) {
            throw new IllegalStateException("No local staff available to handle the complaint");
        }

        int randomIndex = ThreadLocalRandom.current().nextInt((int) count);
        LocalStaff staff = localStaffRepository
                .findAll(PageRequest.of(randomIndex, 1))
                .getContent()
                .get(0);

        Complaint handled = staff.handleComplaint(complaint);
        return complaintRepository.save(handled);
    }
}
