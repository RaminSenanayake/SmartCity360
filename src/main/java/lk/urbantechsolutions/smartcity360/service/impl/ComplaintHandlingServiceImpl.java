package lk.urbantechsolutions.smartcity360.service.impl;

import jakarta.persistence.EntityNotFoundException;
import lk.urbantechsolutions.smartcity360.model.resident.*;
import lk.urbantechsolutions.smartcity360.model.role.*;
import lk.urbantechsolutions.smartcity360.repository.*;
import lk.urbantechsolutions.smartcity360.service.ComplaintHandlingService;
import lk.urbantechsolutions.smartcity360.service.StaffAuthenticationService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import java.util.Objects;

@Service
public class ComplaintHandlingServiceImpl implements ComplaintHandlingService {
    private final ResidentRepository residents;
    private final ComplaintRepository complaints;
    private final LocalStaffRepository localStaff;
    private final SupervisorRepository supervisors;
    private final CityManagerRepository managers;
    private final StaffAuthenticationService authentication;

    public ComplaintHandlingServiceImpl(ResidentRepository residents, ComplaintRepository complaints,
                                        LocalStaffRepository localStaff, SupervisorRepository supervisors, CityManagerRepository managers,
                                        StaffAuthenticationService authentication) {
        this.residents = residents; this.complaints = complaints; this.localStaff = localStaff;
        this.supervisors = supervisors; this.managers = managers; this.authentication = authentication;
    }

    @Override
    @Transactional
    public Complaint submitComplaint(Long residentId, String complaintText) {
        if (complaintText == null || complaintText.isBlank() || complaintText.trim().length() > 255) {
            throw new IllegalArgumentException("Describe the issue in 1-255 characters");
        }
        var resident = residents.findById(residentId)
                .orElseThrow(() -> new EntityNotFoundException("Resident " + residentId + " not found"));
        // Submission intentionally has no severity and does not run the handling chain.
        return complaints.save(new Complaint.Builder(resident, complaintText.trim()).build());
    }

    @Override
    @Transactional
    public Complaint assessComplaint(Long complaintId, ComplaintSeverity severity, StaffIdentity reviewer, String token) {
        if (reviewer == null || reviewer.type() == null) {
            throw new AccessDeniedException("Staff authentication is required to set complaint severity");
        }
        try {
            authentication.authenticate(reviewer, token);
        } catch (SecurityException exception) {
            throw new AccessDeniedException("Staff authentication is required", exception);
        }
        Objects.requireNonNull(severity, "Severity is required");
        Staff actor = switch (reviewer.type()) {
            case LOCAL_STAFF -> localStaff.findById(reviewer.id())
                    .orElseThrow(() -> new EntityNotFoundException("Local staff not found"));
            case SUPERVISOR -> supervisors.findById(reviewer.id())
                    .orElseThrow(() -> new EntityNotFoundException("Supervisor not found"));
            case CITY_MANAGER -> managers.findById(reviewer.id())
                    .orElseThrow(() -> new EntityNotFoundException("City manager not found"));
        };
        Staff handler = actor;
        if (reviewer.type() == StaffType.SUPERVISOR && severity == ComplaintSeverity.LOW) {
            handler = localStaff.findBySupervisor_Id(reviewer.id()).stream()
                    .min(Comparator.comparing(LocalStaff::getId))
                    .orElseThrow(() -> new IllegalStateException("Assign local staff to this supervisor before routing a low-severity complaint"));
        }
        if (reviewer.type() == StaffType.CITY_MANAGER && severity != ComplaintSeverity.HIGH) {
            var team = supervisors.findByCityManager_Id(reviewer.id());
            if (severity == ComplaintSeverity.MEDIUM) {
                handler = team.stream().min(Comparator.comparing(Supervisor::getId))
                        .orElseThrow(() -> new IllegalStateException("Assign a supervisor to this city manager before routing a medium-severity complaint"));
            } else {
                handler = team.stream().flatMap(supervisor -> localStaff.findBySupervisor_Id(supervisor.getId()).stream())
                        .min(Comparator.comparing(LocalStaff::getId))
                        .orElseThrow(() -> new IllegalStateException("Assign local staff in this city manager's team before routing a low-severity complaint"));
            }
        }
        Complaint complaint = complaints.findById(complaintId)
                .orElseThrow(() -> new EntityNotFoundException("Complaint " + complaintId + " not found"));
        complaint.assess(severity, actor.getName(), reviewer.type().name().replace('_', ' '), actor.getId());
        return handler.handleComplaint(complaint);
    }
}
