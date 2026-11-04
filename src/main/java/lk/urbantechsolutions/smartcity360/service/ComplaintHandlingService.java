package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.resident.Complaint;

public interface ComplaintHandlingService {
    Complaint handleComplaint(Complaint complaint);
}
