package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.resident.Complaint;
import lk.urbantechsolutions.smartcity360.model.resident.ComplaintSeverity;
import lk.urbantechsolutions.smartcity360.model.role.StaffIdentity;

public interface ComplaintHandlingService {
    Complaint submitComplaint(Long residentId, String complaintText);
    Complaint assessComplaint(Long complaintId, ComplaintSeverity severity, StaffIdentity reviewer, String token);
}
