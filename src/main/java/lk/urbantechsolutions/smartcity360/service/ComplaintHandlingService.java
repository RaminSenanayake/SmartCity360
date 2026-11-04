package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.resident.Complaint;
import lk.urbantechsolutions.smartcity360.model.resident.ComplaintSeverity;

public interface ComplaintHandlingService {
    Complaint handleComplaint(Long residentId, String complaintText, ComplaintSeverity severity);
}
