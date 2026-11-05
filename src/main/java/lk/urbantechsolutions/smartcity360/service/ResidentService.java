package lk.urbantechsolutions.smartcity360.service;

import lk.urbantechsolutions.smartcity360.model.resident.Resident;

import java.util.List;

public interface ResidentService {
    List<Resident> getAllResidents();
    Resident getResident(Long id);
    Resident addResident(String name);
    Resident updateResident(Long id, String name);
    void removeResident(Long id);
}